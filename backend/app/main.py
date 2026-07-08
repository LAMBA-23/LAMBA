from __future__ import annotations

import re
from datetime import datetime, timedelta, timezone

from fastapi import Depends, FastAPI, HTTPException, Query, Response, status
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import inspect, select, text
from sqlalchemy.orm import Session

from .chat_parser import parse_chat_message
from .database import Base, engine, get_db
from .deepseek_chat import ask_deepseek
from .models import Car, Event, User
from .schemas import (
    CarCreate,
    CarResponse,
    ChatAskRequest,
    ChatAskResponse,
    ChatParseRequest,
    ChatParseResponse,
    EventCreate,
    EventResponse,
    LoginRequest,
    LoginResponse,
    ParsedEventPayload,
    RegisterRequest,
    RegisterResponse,
    StatsPeriodResponse,
    StatsResponse,
)

DEMO_USERNAME = "demo"
DEMO_PASSWORD = "demo"
DEMO_COMPATIBLE_PASSWORDS = {DEMO_PASSWORD, "password"}
DEFAULT_CAR_BRAND = "Not set"
DEFAULT_CAR_MODEL = "Not set"
DEFAULT_CAR_PRODUCTION_YEAR = 0
DEFAULT_CAR_MILEAGE = 0

app = FastAPI(title="LAMBA Backend", version="0.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)


def seed_demo_data(db: Session) -> None:
    # Demo stays available as a normal seeded account for smoke checks.
    user = db.scalar(select(User).where(User.username == DEMO_USERNAME))
    if user is None:
        user = User(username=DEMO_USERNAME, password=DEMO_PASSWORD)
        db.add(user)
        db.flush()

    car = db.scalar(select(Car).where(Car.user_id == user.id))
    if car is None:
        db.add(
            Car(
                user_id=user.id,
                brand="BMW",
                model="M4",
                production_year=2020,
                current_mileage=125000,
            )
        )

    db.commit()


def create_default_car(user_id: int) -> Car:
    return Car(
        user_id=user_id,
        brand=DEFAULT_CAR_BRAND,
        model=DEFAULT_CAR_MODEL,
        production_year=DEFAULT_CAR_PRODUCTION_YEAR,
        current_mileage=DEFAULT_CAR_MILEAGE,
    )


def get_user(db: Session, user_id: int) -> User:
    user = db.get(User, user_id)
    if user is None:
        raise HTTPException(status_code=404, detail="User not found")
    return user


def get_or_create_user_car(db: Session, user: User) -> Car:
    car = db.scalar(select(Car).where(Car.user_id == user.id))
    if car is not None:
        return car

    car = create_default_car(user.id)
    db.add(car)
    db.commit()
    db.refresh(car)
    return car


def get_car_for_user_id(db: Session, user_id: int) -> Car:
    user = get_user(db, user_id)
    return get_or_create_user_car(db, user)


STATISTICS_EVENT_TYPES = {"fuel", "repair", "trip"}
EVENT_TYPES = ("fuel", "repair", "trip", "issue")
EVENT_TYPE_CHECK_CONSTRAINT = "events_type_allowed"


def _coalesce_int(value: int | None) -> int:
    return value if value is not None else 0


def ensure_event_schema() -> None:
    inspector = inspect(engine)
    event_columns = {column["name"] for column in inspector.get_columns("events")}
    with engine.begin() as connection:
        if "fuel_liters" not in event_columns:
            connection.execute(
                text(
                    "ALTER TABLE events "
                    "ADD COLUMN fuel_liters INTEGER NOT NULL DEFAULT 0"
                )
            )
        connection.execute(
            text("UPDATE events SET type = 'issue' WHERE type = 'condition'")
        )

    if engine.dialect.name != "postgresql":
        return

    inspector = inspect(engine)
    check_constraints = {
        constraint["name"] for constraint in inspector.get_check_constraints("events")
    }
    if EVENT_TYPE_CHECK_CONSTRAINT in check_constraints:
        return

    with engine.begin() as connection:
        connection.execute(
            text(
                "ALTER TABLE events "
                f"ADD CONSTRAINT {EVENT_TYPE_CHECK_CONSTRAINT} "
                "CHECK (type IN ('fuel', 'repair', 'trip', 'issue'))"
            )
        )


def _is_statistics_relevant(event: Event) -> bool:
    return event.type in STATISTICS_EVENT_TYPES


def _has_configured_vehicle_record(events: list[Event]) -> bool:
    if not events or events[0].car is None:
        return False

    car = events[0].car
    return (
        car.brand != DEFAULT_CAR_BRAND
        or car.model != DEFAULT_CAR_MODEL
        or car.production_year != DEFAULT_CAR_PRODUCTION_YEAR
        or car.current_mileage != DEFAULT_CAR_MILEAGE
    )


def _event_effective_mileage(event: Event, known_mileage: int) -> int:
    mileage = _coalesce_int(event.mileage)
    if event.type == "trip" and mileage <= known_mileage:
        return known_mileage + mileage
    return mileage


def _sum_trip_distance(events: list[Event], start_at: datetime | None = None) -> int:
    if not events:
        return 0

    initial_mileage = 0
    if events[0].car is not None:
        initial_mileage = _coalesce_int(events[0].car.current_mileage)

    known_mileage = initial_mileage
    total_distance = 0

    for event in events:
        effective_mileage = _event_effective_mileage(event, known_mileage)
        if event.type == "trip":
            trip_delta = max(0, effective_mileage - known_mileage)
            if start_at is None or event.created_at >= start_at:
                total_distance += trip_delta
            known_mileage = max(known_mileage, effective_mileage)

    return total_distance


def _current_total_mileage(events: list[Event], car: Car | None = None) -> int:
    initial_mileage = _coalesce_int(car.current_mileage) if car is not None else 0
    if initial_mileage == 0 and events and events[0].car is not None:
        initial_mileage = _coalesce_int(events[0].car.current_mileage)

    return initial_mileage + _sum_trip_distance(events)


def _current_trip_mileage(car: Car, events: list[Event]) -> int:
    known_mileage = _coalesce_int(car.current_mileage)
    for event in events:
        if event.type != "trip":
            continue
        known_mileage = max(
            known_mileage,
            _event_effective_mileage(event, known_mileage),
        )
    return known_mileage


def _extract_trip_distance_km(text_value: str) -> int | None:
    match = re.search(
        r"\b(\d+)\s*(?:км|км\.|километр|километра|километров)\b",
        text_value.lower(),
    )
    if match is None:
        return None
    return int(match.group(1))


def build_stats_period(
    events: list[Event],
    start_at: datetime | None = None,
    mileage: int | None = None,
    include_vehicle_record: bool = False,
) -> StatsPeriodResponse:
    period_events = [
        event for event in events if start_at is None or event.created_at >= start_at
    ]
    relevant_events = [
        event for event in period_events if _is_statistics_relevant(event)
    ]
    period_mileage = (
        mileage
        if mileage is not None
        else _sum_trip_distance(events, start_at=start_at)
    )
    fuel_expenses = sum(
        event.amount
        for event in relevant_events
        if event.type == "fuel" and event.amount is not None and event.amount > 0
    )
    fuel_liters = sum(
        event.fuel_liters
        for event in relevant_events
        if event.type == "fuel"
        and event.fuel_liters is not None
        and event.fuel_liters > 0
    )
    repair_expenses = sum(
        event.amount
        for event in relevant_events
        if event.type == "repair" and event.amount is not None and event.amount > 0
    )
    total_expenses = fuel_expenses + repair_expenses
    vehicle_record_count = (
        1 if include_vehicle_record and _has_configured_vehicle_record(events) else 0
    )

    return StatsPeriodResponse(
        mileage=period_mileage,
        total_expenses=total_expenses,
        fuel_expenses=fuel_expenses,
        repair_expenses=repair_expenses,
        records_count=len(period_events) + vehicle_record_count,
        avg_fuel_consumption=0,
        avg_expense_consumption=0,
        mileage_km=period_mileage,
        expenses_rub=total_expenses,
        fuel_liters=fuel_liters,
        avg_fuel_consumption_l_per_100km=0,
    )


def build_stats_response(
    events: list[Event], now: datetime, car: Car | None = None
) -> StatsResponse:
    week = build_stats_period(events, start_at=now - timedelta(days=7))
    month = build_stats_period(events, start_at=now - timedelta(days=30))
    all_time = build_stats_period(
        events,
        mileage=_current_total_mileage(events, car),
        include_vehicle_record=True,
    )

    trip_count = sum(1 for event in events if event.type == "trip")

    return StatsResponse(
        fuel_expenses=all_time.fuel_expenses,
        repair_expenses=all_time.repair_expenses,
        trip_count=trip_count,
        total_recorded_mileage=all_time.mileage,
        week=week,
        month=month,
        all_time=all_time,
    )


EXPENSE_CATEGORY_LABELS = {
    "fuel": "Топливо",
    "repair": "Ремонт",
    "trip": "Поездки",
    "issue": "Проблемы",
}
EXPENSE_CATEGORY_ORDER = ("fuel", "repair", "trip", "issue")


def _format_number(value: int) -> str:
    return f"{value:,}".replace(",", " ")


def _normalize_query_text(message: str) -> str:
    return message.strip().lower().replace("ё", "е")


def _detect_last_n_days(message: str) -> int | None:
    normalized = _normalize_query_text(message)
    match = re.search(r"последн\w*\s+(\d+)\s+д", normalized)
    if match is None:
        return None
    days = int(match.group(1))
    return days if days > 0 else None


def _detect_period_label(message: str) -> str | None:
    normalized = _normalize_query_text(message)
    if "недел" in normalized:
        return "week"
    if "месяц" in normalized:
        return "month"
    if "все время" in normalized or "всё время" in normalized:
        return "all_time"
    return None


def _period_start_at(
    period: str | None, now: datetime, last_n_days: int | None = None
) -> datetime | None:
    if last_n_days is not None:
        return now - timedelta(days=last_n_days)
    if period == "week":
        return now - timedelta(days=7)
    if period == "month":
        return now - timedelta(days=30)
    return None


def _format_days_label(days: int) -> str:
    last_two_digits = days % 100
    last_digit = days % 10
    if 11 <= last_two_digits <= 14:
        suffix = "дней"
    elif last_digit == 1:
        suffix = "день"
    elif last_digit in (2, 3, 4):
        suffix = "дня"
    else:
        suffix = "дней"
    return f"последние {days} {suffix}"


def _period_title(period: str | None, last_n_days: int | None = None) -> str:
    if last_n_days is not None:
        return _format_days_label(last_n_days)
    if period == "week":
        return "неделю"
    if period == "month":
        return "месяц"
    if period == "all_time":
        return "всё время"
    return "последние 5 записей"


def _statistics_period_title(period: str | None, last_n_days: int | None = None) -> str:
    if period is None:
        if last_n_days is not None:
            return _period_title(period, last_n_days)
        return "всё время"
    return _period_title(period, last_n_days)


def _detect_expense_category(message: str) -> str | None:
    normalized = _normalize_query_text(message)
    if any(keyword in normalized for keyword in ("топлив", "бензин", "заправ")):
        return "fuel"
    if any(keyword in normalized for keyword in ("ремонт", "сервис")):
        return "repair"
    if any(keyword in normalized for keyword in ("проблем", "полом")):
        return "issue"
    if "поезд" in normalized:
        return "trip"
    return None


def _is_statistics_query(message: str) -> bool:
    normalized = _normalize_query_text(message)
    return "статистик" in normalized


def _is_event_query(message: str) -> bool:
    normalized = _normalize_query_text(message)
    return "событ" in normalized


def _is_expense_query(message: str) -> bool:
    normalized = _normalize_query_text(message)
    return (
        "расход" in normalized
        or "потрат" in normalized
        or _detect_expense_category(normalized) is not None
    )


def _expense_events_for_query(
    events: list[Event], message: str, now: datetime
) -> tuple[list[Event], str | None, int | None]:
    period = _detect_period_label(message)
    last_n_days = _detect_last_n_days(message)
    start_at = _period_start_at(period, now, last_n_days)
    category = _detect_expense_category(message)

    filtered = [
        event
        for event in events
        if event.amount is not None
        and event.amount > 0
        and (start_at is None or event.created_at >= start_at)
        and (category is None or event.type == category)
    ]
    filtered.sort(key=lambda event: (event.created_at, event.id), reverse=True)

    if period is None and last_n_days is None:
        filtered = filtered[:5]

    return filtered, period, last_n_days


def _strip_dates(text_value: str) -> str:
    return re.sub(r"\b\d{2}\.\d{2}\.\d{4}\b:?", "", text_value)


def _clean_event_description(event: Event) -> str:
    description = _strip_dates(event.description).strip(" ,:-")
    return description.strip(" ,:-")


def _format_expense_line(event: Event) -> str:
    if event.type == "fuel":
        line = f"Заправка — {_format_number(event.amount)} ₽"
        if event.fuel_liters is not None and event.fuel_liters > 0:
            line += f", {_format_number(event.fuel_liters)} л"
        return line

    description = _clean_event_description(event)
    if description:
        return f"{description} — {_format_number(event.amount)} ₽"
    return f"{EXPENSE_CATEGORY_LABELS.get(event.type, 'Событие')} — {_format_number(event.amount)} ₽"


def _format_event_line(event: Event) -> str:
    if event.type == "fuel":
        parts = []
        if event.amount is not None and event.amount > 0:
            parts.append(f"{_format_number(event.amount)} ₽")
        if event.fuel_liters is not None and event.fuel_liters > 0:
            parts.append(f"{_format_number(event.fuel_liters)} л")
        suffix = ", ".join(parts)
        return f"Заправка: {suffix}" if suffix else "Заправка"

    if event.type == "trip":
        if event.mileage is not None and event.mileage > 0:
            return f"Поездка: {_format_number(event.mileage)} км"
        return "Поездка"

    if event.type == "repair":
        description = _clean_event_description(event)
        if description and event.amount is not None and event.amount > 0:
            return f"Ремонт: {description}, {_format_number(event.amount)} ₽"
        if description:
            return f"Ремонт: {description}"
        if event.amount is not None and event.amount > 0:
            return f"Ремонт: {_format_number(event.amount)} ₽"
        return "Ремонт"

    description = _clean_event_description(event)
    if description and event.amount is not None and event.amount > 0:
        return f"Проблема: {description}, {_format_number(event.amount)} ₽"
    if description:
        return f"Проблема: {description}"
    if event.amount is not None and event.amount > 0:
        return f"Проблема: {_format_number(event.amount)} ₽"
    return "Проблема"


def _build_trip_distance_map(events: list[Event], car: Car) -> dict[int, int]:
    known_mileage = _coalesce_int(car.current_mileage)
    distances: dict[int, int] = {}

    for event in events:
        if event.type != "trip":
            continue
        effective_mileage = _event_effective_mileage(event, known_mileage)
        trip_distance = max(0, effective_mileage - known_mileage)
        distances[event.id] = trip_distance
        known_mileage = max(known_mileage, effective_mileage)

    return distances


def _build_event_answer(
    events: list[Event], message: str, now: datetime, car: Car
) -> str:
    period = _detect_period_label(message)
    last_n_days = _detect_last_n_days(message)
    start_at = _period_start_at(period, now, last_n_days)
    filtered = [
        event for event in events if start_at is None or event.created_at >= start_at
    ]
    filtered.sort(key=lambda event: (event.created_at, event.id), reverse=True)

    if period is None and last_n_days is None:
        filtered = filtered[:5]
        title = "Последние события:"
    else:
        title = f"События за {_period_title(period, last_n_days)}:"

    if not filtered:
        return "За выбранный период событий не найдено."

    trip_distances = _build_trip_distance_map(events, car)
    lines = [title, ""]
    for index, event in enumerate(filtered, start=1):
        if event.type == "trip":
            distance = trip_distances.get(event.id, _coalesce_int(event.mileage))
            line = f"Поездка: {_format_number(distance)} км"
        else:
            line = _format_event_line(event)
        lines.append(f"{index}. {event.created_at.strftime('%d.%m.%Y')} — {line}")
        if index != len(filtered):
            lines.append("")
    return "\n".join(lines)


def _build_expense_answer(events: list[Event], message: str, now: datetime) -> str:
    expense_events, period, last_n_days = _expense_events_for_query(
        events, message, now
    )
    if not expense_events:
        return "За выбранный период расходов не найдено."

    total_amount = sum(
        event.amount for event in expense_events if event.amount is not None
    )
    lines = [
        f"Расходы за {_period_title(period, last_n_days)}: {_format_number(total_amount)} ₽",
        "",
    ]
    lines.extend(["По категориям:", ""])

    for event_type in EXPENSE_CATEGORY_ORDER:
        category_total = sum(
            event.amount
            for event in expense_events
            if event.type == event_type and event.amount is not None
        )
        if category_total <= 0:
            continue
        lines.append(
            f"* {EXPENSE_CATEGORY_LABELS[event_type]}: {_format_number(category_total)} ₽"
        )

    lines.extend(["", "Последние расходы:", ""])
    for index, event in enumerate(expense_events, start=1):
        lines.append(f"{index}. {_format_expense_line(event)}")
        if index != len(expense_events):
            lines.append("")

    return "\n".join(lines)


def _build_statistics_answer(
    events: list[Event], message: str, now: datetime, car: Car
) -> str:
    period = _detect_period_label(message)
    last_n_days = _detect_last_n_days(message)
    start_at = _period_start_at(period, now, last_n_days)
    period_events = [
        event for event in events if start_at is None or event.created_at >= start_at
    ]
    total_expenses = sum(
        event.amount
        for event in period_events
        if event.amount is not None and event.amount > 0
    )
    fuel_liters = sum(
        event.fuel_liters
        for event in period_events
        if event.type == "fuel"
        and event.fuel_liters is not None
        and event.fuel_liters > 0
    )
    if period in (None, "all_time"):
        mileage = max(
            [_current_total_mileage(events, car), _coalesce_int(car.current_mileage)]
            + [_coalesce_int(event.mileage) for event in events]
        )
    else:
        mileage = _sum_trip_distance(events, start_at=start_at)

    return "\n".join(
        (
            f"Краткая статистика за {_statistics_period_title(period, last_n_days)}:",
            "",
            f"* Расходы: {_format_number(total_expenses)} ₽",
            f"* Пробег: {_format_number(mileage)} км",
            f"* Топливо: {_format_number(fuel_liters)} л",
            f"* Записей: {_format_number(len(period_events))}",
        )
    )


def _build_llm_vehicle_context(car: Car, events: list[Event]) -> str:
    context_lines = []
    has_real_car_data = (
        car.brand != DEFAULT_CAR_BRAND
        and car.production_year != DEFAULT_CAR_PRODUCTION_YEAR
    )
    if has_real_car_data:
        context_lines.append(
            f"Автомобиль: {car.brand} {car.model}, {car.production_year} г., пробег {car.current_mileage} км."
        )

    for ev in events:
        line = f"- [{ev.created_at.isoformat()}] [{ev.type}] {ev.description}"
        if ev.amount is not None and ev.amount > 0:
            line += f", сумма: {ev.amount}"
        if ev.fuel_liters is not None and ev.fuel_liters > 0:
            line += f", литры: {ev.fuel_liters}"
        if ev.mileage is not None and ev.mileage > 0:
            line += f", пробег: {ev.mileage}"
        context_lines.append(line)

    return "\n".join(context_lines)


@app.on_event("startup")
def on_startup() -> None:
    Base.metadata.create_all(bind=engine)
    ensure_event_schema()
    db = next(get_db())
    try:
        seed_demo_data(db)
    finally:
        db.close()


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/auth/login", response_model=LoginResponse, response_model_exclude_none=True)
def login(payload: LoginRequest, db: Session = Depends(get_db)) -> LoginResponse:
    user = db.scalar(select(User).where(User.username == payload.username))
    is_demo_compatible_login = (
        payload.username == DEMO_USERNAME
        and payload.password in DEMO_COMPATIBLE_PASSWORDS
    )
    if user is None or (
        user.password != payload.password and not is_demo_compatible_login
    ):
        return LoginResponse(success=False)
    return LoginResponse(success=True, user_id=user.id)


@app.post("/auth/register", response_model=RegisterResponse, status_code=201)
def register(
    payload: RegisterRequest, db: Session = Depends(get_db)
) -> RegisterResponse:
    existing_user = db.scalar(select(User).where(User.username == payload.username))
    if existing_user is not None:
        raise HTTPException(status_code=400, detail="Username is already registered")

    user = User(username=payload.username, password=payload.password)
    db.add(user)
    db.flush()
    db.add(create_default_car(user.id))
    db.commit()
    db.refresh(user)
    return RegisterResponse(success=True, user_id=user.id)


@app.get("/vehicle", response_model=CarResponse)
def get_vehicle(user_id: int = Query(...), db: Session = Depends(get_db)) -> Car:
    return get_car_for_user_id(db, user_id)


@app.post("/vehicle", response_model=CarResponse, status_code=201)
def create_vehicle(payload: CarCreate, db: Session = Depends(get_db)) -> Car:
    user = db.scalar(select(User).where(User.id == payload.user_id))
    if user is None:
        raise HTTPException(status_code=404, detail="User not found")

    existing_car = db.scalar(select(Car).where(Car.user_id == payload.user_id))
    if existing_car is not None:
        existing_car.brand = payload.brand
        existing_car.model = payload.model
        existing_car.production_year = payload.production_year
        existing_car.current_mileage = payload.current_mileage
        db.commit()
        db.refresh(existing_car)
        return existing_car

    car = Car(
        user_id=payload.user_id,
        brand=payload.brand,
        model=payload.model,
        production_year=payload.production_year,
        current_mileage=payload.current_mileage,
    )
    db.add(car)
    db.commit()
    db.refresh(car)
    return car


@app.post("/chat/parse-event", response_model=ChatParseResponse)
def parse_event_from_chat(payload: ChatParseRequest) -> ChatParseResponse:
    parsed = parse_chat_message(payload.message)

    if parsed.needs_clarification:
        return ChatParseResponse(
            status="clarification_needed",
            clarification_question=parsed.clarification_question
            or (
                "\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435, "
                "\u043f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430, "
                "\u0434\u0435\u0442\u0430\u043b\u0438 "
                "\u0437\u0430\u043f\u0438\u0441\u0438."
            ),
        )

    if (
        parsed.type is None
        or parsed.description is None
        or not parsed.description.strip()
    ):
        return ChatParseResponse(
            status="clarification_needed",
            clarification_question=(
                "\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435, "
                "\u043f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430, "
                "\u0442\u0438\u043f \u0441\u043e\u0431\u044b\u0442\u0438\u044f "
                "\u0438 \u0434\u0435\u0442\u0430\u043b\u0438 \u0437\u0430\u043f\u0438\u0441\u0438."
            ),
        )

    if parsed.amount is not None and parsed.amount < 0:
        return ChatParseResponse(
            status="clarification_needed",
            clarification_question=(
                "\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435, "
                "\u043f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430, "
                "\u0441\u0443\u043c\u043c\u0443 \u0437\u0430\u043f\u0438\u0441\u0438."
            ),
        )

    if parsed.fuel_liters is not None and parsed.fuel_liters < 0:
        return ChatParseResponse(
            status="clarification_needed",
            clarification_question=(
                "\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435, "
                "\u043f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430, "
                "\u043e\u0431\u044a\u0435\u043c "
                "\u0442\u043e\u043f\u043b\u0438\u0432\u0430."
            ),
        )

    if parsed.mileage is not None and parsed.mileage < 0:
        return ChatParseResponse(
            status="clarification_needed",
            clarification_question=(
                "\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435, "
                "\u043f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430, "
                "\u043f\u0440\u043e\u0431\u0435\u0433 "
                "\u0430\u0432\u0442\u043e\u043c\u043e\u0431\u0438\u043b\u044f."
            ),
        )

    return ChatParseResponse(
        status="parsed",
        parsed_event=ParsedEventPayload(
            type=parsed.type,
            description=parsed.description.strip(),
            amount=parsed.amount,
            fuel_liters=parsed.fuel_liters,
            mileage=parsed.mileage,
        ),
    )


@app.post("/chat/ask", response_model=ChatAskResponse)
def chat_ask(
    payload: ChatAskRequest,
    user_id: int = Query(...),
    db: Session = Depends(get_db),
) -> ChatAskResponse:
    MAX_CONTEXT_EVENTS = 30
    now = datetime.now(timezone.utc).replace(tzinfo=None)
    car = get_car_for_user_id(db, user_id)
    all_events = list(
        db.scalars(
            select(Event)
            .where(Event.car_id == car.id)
            .order_by(Event.created_at, Event.id)
        )
    )
    if _is_statistics_query(payload.message):
        return ChatAskResponse(
            answer=_build_statistics_answer(all_events, payload.message, now, car)
        )
    if _is_expense_query(payload.message):
        return ChatAskResponse(
            answer=_build_expense_answer(all_events, payload.message, now)
        )
    if _is_event_query(payload.message):
        return ChatAskResponse(
            answer=_build_event_answer(all_events, payload.message, now, car)
        )

    events = list(reversed(all_events[-MAX_CONTEXT_EVENTS:]))

    context_lines = []
    has_real_car_data = (
        car.brand != DEFAULT_CAR_BRAND
        and car.production_year != DEFAULT_CAR_PRODUCTION_YEAR
    )
    if has_real_car_data:
        context_lines.append(
            f"Автомобиль: {car.brand} {car.model}, {car.production_year} г., пробег {car.current_mileage} км."
        )
    for ev in events:
        line = f"- [{ev.type}] {ev.description}"
        if ev.amount:
            line += f", сумма: {ev.amount}"
        if ev.mileage:
            line += f", пробег: {ev.mileage}"
        context_lines.append(line)

    vehicle_context = _build_llm_vehicle_context(car, events)
    try:
        answer = ask_deepseek(message=payload.message, vehicle_context=vehicle_context)
    except Exception:
        answer = "Не удалось получить ответ от AI-ассистента. Попробуйте позже."
    return ChatAskResponse(answer=answer)


@app.get("/events", response_model=list[EventResponse])
def get_events(user_id: int = Query(...), db: Session = Depends(get_db)) -> list[Event]:
    car = get_car_for_user_id(db, user_id)
    return list(
        db.scalars(
            select(Event)
            .where(Event.car_id == car.id, Event.type.in_(EVENT_TYPES))
            .order_by(Event.id)
        )
    )


@app.post("/events", response_model=EventResponse)
def create_event(
    payload: EventCreate,
    user_id: int = Query(...),
    db: Session = Depends(get_db),
) -> Event:
    car = get_car_for_user_id(db, user_id)
    existing_events = list(
        db.scalars(
            select(Event)
            .where(Event.car_id == car.id)
            .order_by(Event.created_at, Event.id)
        )
    )
    previous_mileage = _current_trip_mileage(car, existing_events)

    event_mileage = payload.mileage
    if payload.type == "trip" and event_mileage is None:
        event_mileage = _extract_trip_distance_km(payload.description)
    if event_mileage is None:
        event_mileage = 0

    if payload.type == "trip":
        event_mileage = _event_effective_mileage(
            Event(
                car_id=car.id,
                type=payload.type,
                description=payload.description,
                amount=payload.amount if payload.amount is not None else 0,
                fuel_liters=payload.fuel_liters
                if payload.fuel_liters is not None
                else 0,
                mileage=event_mileage,
            ),
            previous_mileage,
        )

    event = Event(
        car_id=car.id,
        type=payload.type,
        description=payload.description,
        amount=payload.amount if payload.amount is not None else 0,
        fuel_liters=payload.fuel_liters if payload.fuel_liters is not None else 0,
        mileage=event_mileage,
    )
    db.add(event)
    db.commit()
    db.refresh(event)
    return event


@app.put("/events/{event_id}", response_model=EventResponse)
def update_event(
    event_id: int,
    payload: EventCreate,
    user_id: int = Query(...),
    db: Session = Depends(get_db),
) -> Event:
    car = get_car_for_user_id(db, user_id)
    event = db.scalar(select(Event).where(Event.id == event_id, Event.car_id == car.id))
    if event is None:
        raise HTTPException(status_code=404, detail="Event not found")

    existing_events = list(
        db.scalars(
            select(Event)
            .where(Event.car_id == car.id, Event.id != event_id)
            .order_by(Event.created_at, Event.id)
        )
    )
    previous_mileage = _current_trip_mileage(car, existing_events)

    event_mileage = payload.mileage
    if payload.type == "trip" and event_mileage is None:
        event_mileage = _extract_trip_distance_km(payload.description)
    if event_mileage is None:
        event_mileage = 0

    if payload.type == "trip":
        event_mileage = _event_effective_mileage(
            Event(
                car_id=car.id,
                type=payload.type,
                description=payload.description,
                amount=payload.amount if payload.amount is not None else 0,
                fuel_liters=payload.fuel_liters
                if payload.fuel_liters is not None
                else 0,
                mileage=event_mileage,
            ),
            previous_mileage,
        )

    event.type = payload.type
    event.description = payload.description
    event.amount = payload.amount if payload.amount is not None else 0
    event.fuel_liters = payload.fuel_liters if payload.fuel_liters is not None else 0
    event.mileage = event_mileage

    db.commit()
    db.refresh(event)
    return event


@app.delete(
    "/events/{event_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    response_model=None,
    response_class=Response,
)
def delete_event(
    event_id: int,
    user_id: int = Query(...),
    db: Session = Depends(get_db),
) -> None:
    car = get_car_for_user_id(db, user_id)
    event = db.scalar(select(Event).where(Event.id == event_id, Event.car_id == car.id))
    if event is None:
        raise HTTPException(status_code=404, detail="Event not found")

    db.delete(event)
    db.commit()


@app.get("/stats", response_model=StatsResponse)
def get_stats(
    user_id: int = Query(...), db: Session = Depends(get_db)
) -> StatsResponse:
    car = get_car_for_user_id(db, user_id)
    now = datetime.now(timezone.utc).replace(tzinfo=None)
    events = list(
        db.scalars(
            select(Event)
            .where(Event.car_id == car.id)
            .order_by(Event.created_at, Event.id)
        )
    )
    return build_stats_response(events, now, car)
