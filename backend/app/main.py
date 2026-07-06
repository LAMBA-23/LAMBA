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
    MAX_CONTEXT_EVENTS = 50

    car = get_car_for_user_id(db, user_id)
    all_events = list(
        db.scalars(select(Event).where(Event.car_id == car.id).order_by(Event.id))
    )
    events = all_events[-MAX_CONTEXT_EVENTS:]

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

    vehicle_context = "\n".join(context_lines)
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
    event = db.scalar(
        select(Event).where(Event.id == event_id, Event.car_id == car.id)
    )
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
    event = db.scalar(
        select(Event).where(Event.id == event_id, Event.car_id == car.id)
    )
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
