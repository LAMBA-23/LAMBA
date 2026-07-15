from __future__ import annotations

import os
import re
import uuid
from datetime import datetime, timedelta, timezone
from decimal import Decimal, InvalidOperation
from pathlib import Path

from fastapi import (
    Depends,
    FastAPI,
    File,
    HTTPException,
    Query,
    Request,
    Response,
    UploadFile,
    status,
)
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from sqlalchemy import inspect, select, text
from sqlalchemy.orm import Session

from .chat_parser import parse_chat_message
from .database import Base, engine, get_db
from .deepseek_chat import ask_deepseek, generate_chat_title
from .models import Car, Event, User
from .rate_limit import FixedWindowRateLimiter
from .schemas import (
    CarCreate,
    CarResponse,
    ChatAskRequest,
    ChatAskResponse,
    ChatContextMessage,
    ChatParseRequest,
    ChatParseResponse,
    ChatTitleRequest,
    ChatTitleResponse,
    EventCreate,
    EventResponse,
    LoginRequest,
    LoginResponse,
    ParsedEventPayload,
    RecommendationItem,
    RecommendationsResponse,
    RegisterRequest,
    RegisterResponse,
    StatsPeriodResponse,
    StatsResponse,
)
from .security import hash_password, is_password_hashed, verify_password

DEFAULT_CAR_BRAND = "Not set"
DEFAULT_CAR_MODEL = "Not set"
DEFAULT_CAR_PRODUCTION_YEAR = 0
DEFAULT_CAR_MILEAGE = 0
EVENT_TYPE_CHECK_CONSTRAINT = "events_type_check"
LOGIN_RATE_LIMIT = int(os.getenv("LOGIN_RATE_LIMIT", "5"))
LOGIN_RATE_LIMIT_WINDOW_SECONDS = int(
    os.getenv("LOGIN_RATE_LIMIT_WINDOW_SECONDS", "60")
)
CHAT_RATE_LIMIT = int(os.getenv("CHAT_RATE_LIMIT", "20"))
CHAT_RATE_LIMIT_WINDOW_SECONDS = int(os.getenv("CHAT_RATE_LIMIT_WINDOW_SECONDS", "60"))
CORS_ALLOWED_ORIGINS = [
    origin.strip()
    for origin in os.getenv("CORS_ALLOWED_ORIGINS", "").split(",")
    if origin.strip()
]
BACKEND_DIR = Path(__file__).resolve().parents[1]
EVENT_PHOTO_DIR = Path(
    os.getenv("EVENT_PHOTO_DIR", str(BACKEND_DIR / "uploads" / "event_photos"))
)
EVENT_PHOTO_MAX_BYTES = 5 * 1024 * 1024
EVENT_PHOTO_MIME_EXTENSIONS = {
    "image/jpeg": ".jpg",
    "image/png": ".png",
    "image/webp": ".webp",
}

app = FastAPI(title="LAMBA Backend", version="0.1.0")
rate_limiter = FixedWindowRateLimiter()

app.add_middleware(
    CORSMiddleware,
    allow_origins=CORS_ALLOWED_ORIGINS,
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)
EVENT_PHOTO_DIR.mkdir(parents=True, exist_ok=True)
app.mount(
    "/uploads/event_photos",
    StaticFiles(directory=str(EVENT_PHOTO_DIR)),
    name="event_photos",
)


def remove_demo_account(db: Session) -> None:
    user = db.scalar(select(User).where(User.username == "demo"))
    if user is None:
        return

    car = db.scalar(select(Car).where(Car.user_id == user.id))
    if car is not None:
        for event in db.scalars(select(Event).where(Event.car_id == car.id)):
            db.delete(event)
        db.delete(car)

    db.delete(user)
    db.commit()


def upgrade_legacy_passwords(db: Session) -> None:
    users = list(db.scalars(select(User)))
    changed = False

    for user in users:
        if is_password_hashed(user.password):
            continue
        user.password = hash_password(user.password)
        changed = True

    if changed:
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


def get_event_for_user(db: Session, event_id: int, user_id: int) -> Event:
    car = get_car_for_user_id(db, user_id)
    event = db.scalar(select(Event).where(Event.id == event_id, Event.car_id == car.id))
    if event is None:
        raise HTTPException(status_code=404, detail="Event not found")
    return event


def _client_identifier(request: Request) -> str:
    forwarded_for = request.headers.get("x-forwarded-for")
    if forwarded_for:
        return forwarded_for.split(",")[0].strip()
    if request.client is not None and request.client.host:
        return request.client.host
    return "unknown"


def enforce_rate_limit(
    request: Request,
    *,
    scope: str,
    limit: int,
    window_seconds: int,
    detail: str,
) -> None:
    key = f"{scope}:{_client_identifier(request)}"
    if rate_limiter.allow(key, limit=limit, window_seconds=window_seconds):
        return
    raise HTTPException(status_code=429, detail=detail)


STATISTICS_EVENT_TYPES = {"fuel", "repair", "trip"}
EVENT_TYPES = ("fuel", "repair", "trip", "issue")
EVENT_TYPE_CHECK_CONSTRAINT = "events_type_allowed"
ZERO_DECIMAL = Decimal("0")
DECIMAL_SCALE = Decimal("0.001")


def _coalesce_int(value: int | None) -> int:
    return value if value is not None else 0


def _to_decimal(value: object) -> Decimal:
    if value is None:
        return ZERO_DECIMAL
    if isinstance(value, Decimal):
        return value
    try:
        return Decimal(str(value))
    except InvalidOperation:
        return ZERO_DECIMAL


def _coalesce_decimal(value: object) -> Decimal:
    return _to_decimal(value)


def _safe_photo_extension(filename: str | None, mime_type: str) -> str:
    fallback = EVENT_PHOTO_MIME_EXTENSIONS[mime_type]
    suffix = Path(filename or "").suffix.lower()
    if suffix in {".jpg", ".jpeg"} and mime_type == "image/jpeg":
        return suffix
    if suffix in {".png", ".webp"} and EVENT_PHOTO_MIME_EXTENSIONS[mime_type] == suffix:
        return suffix
    return fallback


def _detect_image_mime(content: bytes) -> str | None:
    if content.startswith(b"\xff\xd8\xff"):
        return "image/jpeg"
    if content.startswith(b"\x89PNG\r\n\x1a\n"):
        return "image/png"
    if len(content) >= 12 and content[:4] == b"RIFF" and content[8:12] == b"WEBP":
        return "image/webp"
    return None


async def _read_valid_photo(file: UploadFile) -> tuple[bytes, str]:
    mime_type = (file.content_type or "").split(";")[0].strip().lower()
    if mime_type not in EVENT_PHOTO_MIME_EXTENSIONS:
        raise HTTPException(status_code=415, detail="Unsupported image MIME type")

    content = await file.read(EVENT_PHOTO_MAX_BYTES + 1)
    if not content:
        raise HTTPException(status_code=400, detail="Image file is empty")
    if len(content) > EVENT_PHOTO_MAX_BYTES:
        raise HTTPException(status_code=413, detail="Image file is too large")
    detected_mime_type = _detect_image_mime(content)
    if detected_mime_type != mime_type:
        raise HTTPException(status_code=400, detail="Invalid image content")
    return content, mime_type


def _delete_event_photo_file(event: Event) -> None:
    if not event.photo_path:
        return
    photo_file = EVENT_PHOTO_DIR / Path(event.photo_path).name
    if photo_file.is_file():
        photo_file.unlink(missing_ok=True)


def _clear_event_photo(event: Event) -> None:
    _delete_event_photo_file(event)
    event.photo_path = None
    event.photo_original_name = None
    event.photo_mime_type = None
    event.photo_size = None


def ensure_event_schema() -> None:
    inspector = inspect(engine)
    event_columns = {
        column["name"]: column for column in inspector.get_columns("events")
    }

    with engine.begin() as connection:
        if engine.dialect.name == "postgresql":
            for column_name in ("amount", "fuel_liters", "mileage"):
                if column_name in event_columns:
                    connection.execute(
                        text(
                            f"ALTER TABLE events ALTER COLUMN {column_name} "
                            f"TYPE NUMERIC(12, 3) USING {column_name}::numeric"
                        )
                    )

        fuel_liters_column = event_columns.get("fuel_liters")
        if fuel_liters_column is None:
            connection.execute(
                text(
                    "ALTER TABLE events "
                    "ADD COLUMN fuel_liters NUMERIC(12, 3) NOT NULL DEFAULT 0"
                )
            )

        if "odometer_start" not in event_columns:
            connection.execute(
                text("ALTER TABLE events ADD COLUMN odometer_start INTEGER")
            )
        if "odometer_end" not in event_columns:
            connection.execute(
                text("ALTER TABLE events ADD COLUMN odometer_end INTEGER")
            )
        if "photo_path" not in event_columns:
            connection.execute(
                text("ALTER TABLE events ADD COLUMN photo_path VARCHAR(255)")
            )
        if "photo_original_name" not in event_columns:
            connection.execute(
                text("ALTER TABLE events ADD COLUMN photo_original_name VARCHAR(255)")
            )
        if "photo_mime_type" not in event_columns:
            connection.execute(
                text("ALTER TABLE events ADD COLUMN photo_mime_type VARCHAR(64)")
            )
        if "photo_size" not in event_columns:
            connection.execute(text("ALTER TABLE events ADD COLUMN photo_size INTEGER"))
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


def _event_effective_mileage(event: Event, known_mileage: Decimal) -> Decimal:
    if event.type == "trip" and event.odometer_end is not None:
        return Decimal(event.odometer_end)

    mileage = _coalesce_decimal(event.mileage)
    if event.type == "trip" and mileage <= known_mileage:
        return known_mileage + mileage
    return mileage


def _event_trip_distance(event: Event, known_mileage: Decimal) -> Decimal:
    if (
        event.type == "trip"
        and event.odometer_start is not None
        and event.odometer_end is not None
    ):
        return max(ZERO_DECIMAL, Decimal(event.odometer_end - event.odometer_start))

    effective_mileage = _event_effective_mileage(event, known_mileage)
    return max(ZERO_DECIMAL, effective_mileage - known_mileage)


def _sum_trip_distance(
    events: list[Event], start_at: datetime | None = None
) -> Decimal:
    if not events:
        return ZERO_DECIMAL

    initial_mileage = ZERO_DECIMAL
    if events[0].car is not None:
        initial_mileage = Decimal(_coalesce_int(events[0].car.current_mileage))

    known_mileage = initial_mileage
    total_distance = ZERO_DECIMAL

    for event in events:
        effective_mileage = _event_effective_mileage(event, known_mileage)
        if event.type == "trip":
            trip_delta = _event_trip_distance(event, known_mileage)
            if start_at is None or event.created_at >= start_at:
                total_distance += trip_delta
            known_mileage = max(known_mileage, effective_mileage)

    return total_distance


def _current_total_mileage(events: list[Event], car: Car | None = None) -> Decimal:
    initial_mileage = (
        Decimal(_coalesce_int(car.current_mileage)) if car is not None else ZERO_DECIMAL
    )
    if initial_mileage == 0 and events and events[0].car is not None:
        initial_mileage = Decimal(_coalesce_int(events[0].car.current_mileage))

    return initial_mileage + _sum_trip_distance(events)


def _current_trip_mileage(car: Car, events: list[Event]) -> Decimal:
    known_mileage = Decimal(_coalesce_int(car.current_mileage))
    for event in events:
        if event.type != "trip":
            continue
        known_mileage = max(
            known_mileage,
            _event_effective_mileage(event, known_mileage),
        )
    return known_mileage


def _extract_trip_distance_km(text_value: str) -> Decimal | None:
    match = re.search(
        r"\b(\d+(?:[.,]\d{1,3})?)\s*(?:км|км\.|километр|километра|километров)\b",
        text_value.lower(),
    )
    if match is None:
        return None
    return Decimal(match.group(1).replace(",", "."))


def _event_mileage_from_payload(
    payload: EventCreate, previous_mileage: Decimal
) -> Decimal:
    if payload.type == "trip" and payload.odometer_end is not None:
        return Decimal(payload.odometer_end)

    event_mileage = payload.mileage
    if payload.type == "trip" and event_mileage is None:
        event_mileage = _extract_trip_distance_km(payload.description)
    if event_mileage is None:
        event_mileage = ZERO_DECIMAL

    if payload.type != "trip":
        return event_mileage

    return _event_effective_mileage(
        Event(
            car_id=0,
            type=payload.type,
            description=payload.description,
            amount=payload.amount if payload.amount is not None else 0,
            fuel_liters=payload.fuel_liters if payload.fuel_liters is not None else 0,
            mileage=event_mileage,
        ),
        previous_mileage,
    )


def build_stats_period(
    events: list[Event],
    start_at: datetime | None = None,
    mileage: Decimal | None = None,
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
        (
            event.amount
            for event in relevant_events
            if event.type == "fuel" and event.amount is not None and event.amount > 0
        ),
        ZERO_DECIMAL,
    )
    fuel_liters = sum(
        (
            event.fuel_liters
            for event in relevant_events
            if event.type == "fuel"
            and event.fuel_liters is not None
            and event.fuel_liters > 0
        ),
        ZERO_DECIMAL,
    )
    repair_expenses = sum(
        (
            event.amount
            for event in relevant_events
            if event.type == "repair" and event.amount is not None and event.amount > 0
        ),
        ZERO_DECIMAL,
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
        avg_fuel_consumption=ZERO_DECIMAL,
        avg_expense_consumption=ZERO_DECIMAL,
        mileage_km=period_mileage,
        expenses_rub=total_expenses,
        fuel_liters=fuel_liters,
        avg_fuel_consumption_l_per_100km=ZERO_DECIMAL,
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
FUEL_PRICE_WARNING_RUB_PER_LITER = 80
REPAIR_MONTH_WARNING_RUB = 20000
STALE_RECORD_DAYS = 14
MILEAGE_SINCE_FUEL_WARNING_KM = 500


def _format_number(value: int | float | Decimal) -> str:
    if isinstance(value, Decimal):
        normalized = value.quantize(DECIMAL_SCALE).normalize()
        if normalized == normalized.to_integral_value():
            return f"{int(normalized):,}".replace(",", " ")
        return format(normalized, "f").replace(".", ",")
    if isinstance(value, float) and value.is_integer():
        value = int(value)
    return f"{value:,}".replace(",", " ")


def _build_recommendations(
    events: list[Event], now: datetime, car: Car
) -> list[RecommendationItem]:
    recommendations: list[RecommendationItem] = []
    if not events:
        return [
            RecommendationItem(
                id="no_events",
                severity="info",
                title="Add first vehicle record",
                message=(
                    "No events are saved yet. Add fuel, trip, repair, or breakdown "
                    "records to unlock rule-based maintenance recommendations."
                ),
                source="events_count == 0",
            )
        ]

    latest_event = max(events, key=lambda event: event.created_at)
    days_since_latest = (now - latest_event.created_at).days
    if days_since_latest >= STALE_RECORD_DAYS:
        recommendations.append(
            RecommendationItem(
                id="stale_records",
                severity="info",
                title="Update vehicle history",
                message=(
                    f"No new records for {days_since_latest} days. Add recent "
                    "fuel, trip, or service data so statistics stay reliable."
                ),
                source=f"days_since_latest_event >= {STALE_RECORD_DAYS}",
            )
        )

    fuel_events = [
        event
        for event in events
        if event.type == "fuel"
        and event.amount is not None
        and event.amount > 0
        and event.fuel_liters is not None
        and event.fuel_liters > 0
    ]
    recent_fuel_events = sorted(
        fuel_events, key=lambda event: event.created_at, reverse=True
    )[:3]
    recent_fuel_liters = sum(event.fuel_liters for event in recent_fuel_events)
    recent_fuel_amount = sum(event.amount for event in recent_fuel_events)
    if recent_fuel_liters > 0:
        average_price = recent_fuel_amount / recent_fuel_liters
        if average_price > FUEL_PRICE_WARNING_RUB_PER_LITER:
            recommendations.append(
                RecommendationItem(
                    id="high_fuel_price",
                    severity="warning",
                    title="Fuel price looks high",
                    message=(
                        "Average fuel price in the latest refuels is "
                        f"{_format_number(average_price)} RUB/L. Compare stations "
                        "or check if the entered amount and liters are correct."
                    ),
                    source=(
                        "sum(last_3_fuel.amount) / sum(last_3_fuel.fuel_liters) "
                        f"> {FUEL_PRICE_WARNING_RUB_PER_LITER}"
                    ),
                )
            )

    month_start = now - timedelta(days=30)
    monthly_repair_expenses = sum(
        event.amount
        for event in events
        if event.type == "repair"
        and event.amount is not None
        and event.created_at >= month_start
    )
    if monthly_repair_expenses > REPAIR_MONTH_WARNING_RUB:
        recommendations.append(
            RecommendationItem(
                id="high_monthly_repair_cost",
                severity="warning",
                title="Repair expenses increased",
                message=(
                    "Repair expenses in the last 30 days are "
                    f"{_format_number(monthly_repair_expenses)} RUB. Review repeated "
                    "service work and plan a diagnostic check if needed."
                ),
                source=f"repair_expenses_30d > {REPAIR_MONTH_WARNING_RUB}",
            )
        )

    recent_issue = next(
        (
            event
            for event in sorted(events, key=lambda item: item.created_at, reverse=True)
            if event.type == "issue" and event.created_at >= month_start
        ),
        None,
    )
    if recent_issue is not None:
        recommendations.append(
            RecommendationItem(
                id="recent_breakdown",
                severity="warning",
                title="Follow up on recent breakdown",
                message=(
                    "A breakdown/problem was recorded in the last 30 days. Check "
                    "whether it was diagnosed or repaired before longer trips."
                ),
                source="latest_issue.created_at >= now - 30 days",
            )
        )

    latest_fuel = next(
        (
            event
            for event in sorted(
                fuel_events,
                key=lambda item: item.created_at,
                reverse=True,
            )
            if event.mileage is not None and event.mileage > 0
        ),
        None,
    )
    current_mileage = _current_total_mileage(events, car)
    if latest_fuel is not None:
        mileage_since_fuel = max(0, current_mileage - latest_fuel.mileage)
        if mileage_since_fuel >= MILEAGE_SINCE_FUEL_WARNING_KM:
            recommendations.append(
                RecommendationItem(
                    id="long_distance_since_fuel",
                    severity="info",
                    title="Check fuel level",
                    message=(
                        f"About {_format_number(mileage_since_fuel)} km were recorded "
                        "since the latest fuel event. Check fuel level before the "
                        "next trip."
                    ),
                    source=(
                        "current_mileage - latest_fuel.mileage "
                        f">= {MILEAGE_SINCE_FUEL_WARNING_KM}"
                    ),
                )
            )

    return recommendations


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


def _build_trip_distance_map(events: list[Event], car: Car) -> dict[int, Decimal]:
    known_mileage = Decimal(_coalesce_int(car.current_mileage))
    distances: dict[int, Decimal] = {}

    for event in events:
        if event.type != "trip":
            continue
        effective_mileage = _event_effective_mileage(event, known_mileage)
        trip_distance = _event_trip_distance(event, known_mileage)
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
            distance = trip_distances.get(event.id, _coalesce_decimal(event.mileage))
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
        (event.amount for event in expense_events if event.amount is not None),
        ZERO_DECIMAL,
    )
    lines = [
        f"Расходы за {_period_title(period, last_n_days)}: {_format_number(total_amount)} ₽",
        "",
    ]
    lines.extend(["По категориям:", ""])

    for event_type in EXPENSE_CATEGORY_ORDER:
        category_total = sum(
            (
                event.amount
                for event in expense_events
                if event.type == event_type and event.amount is not None
            ),
            ZERO_DECIMAL,
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
        (
            event.amount
            for event in period_events
            if event.amount is not None and event.amount > 0
        ),
        ZERO_DECIMAL,
    )
    fuel_liters = sum(
        (
            event.fuel_liters
            for event in period_events
            if event.type == "fuel"
            and event.fuel_liters is not None
            and event.fuel_liters > 0
        ),
        ZERO_DECIMAL,
    )
    if period in (None, "all_time"):
        mileage = max(
            [
                _current_total_mileage(events, car),
                Decimal(_coalesce_int(car.current_mileage)),
            ]
            + [_coalesce_decimal(event.mileage) for event in events]
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


def _build_chat_context(messages: list[ChatContextMessage] | None) -> str | None:
    if not messages:
        return None
    lines = [f"{message.sender}: {message.text.strip()}" for message in messages]
    lines = [line for line in lines if line.strip()]
    return "\n".join(lines) if lines else None


@app.on_event("startup")
def on_startup() -> None:
    Base.metadata.create_all(bind=engine)
    ensure_event_schema()
    db = next(get_db())
    try:
        remove_demo_account(db)
        upgrade_legacy_passwords(db)
    finally:
        db.close()


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/auth/login", response_model=LoginResponse, response_model_exclude_none=True)
def login(
    payload: LoginRequest,
    request: Request,
    db: Session = Depends(get_db),
) -> LoginResponse:
    enforce_rate_limit(
        request,
        scope="login",
        limit=LOGIN_RATE_LIMIT,
        window_seconds=LOGIN_RATE_LIMIT_WINDOW_SECONDS,
        detail="Too many login attempts",
    )
    user = db.scalar(select(User).where(User.username == payload.username))
    if user is None or not verify_password(payload.password, user.password):
        return LoginResponse(success=False)

    if not is_password_hashed(user.password):
        user.password = hash_password(payload.password)
        db.commit()

    return LoginResponse(success=True, user_id=user.id)


@app.post("/auth/register", response_model=RegisterResponse, status_code=201)
def register(
    payload: RegisterRequest, db: Session = Depends(get_db)
) -> RegisterResponse:
    existing_user = db.scalar(select(User).where(User.username == payload.username))
    if existing_user is not None:
        raise HTTPException(status_code=400, detail="Username is already registered")

    user = User(username=payload.username, password=hash_password(payload.password))
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
        if parsed.clarification_question:
            return ChatParseResponse(
                status="clarification_needed",
                clarification_question=parsed.clarification_question,
            )
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
    request: Request,
    user_id: int = Query(...),
    db: Session = Depends(get_db),
) -> ChatAskResponse:
    enforce_rate_limit(
        request,
        scope="chat",
        limit=CHAT_RATE_LIMIT,
        window_seconds=CHAT_RATE_LIMIT_WINDOW_SECONDS,
        detail="Too many chat requests",
    )
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
    chat_context = _build_chat_context(payload.chat_context)
    try:
        try:
            answer = ask_deepseek(
                message=payload.message,
                vehicle_context=vehicle_context,
                chat_context=chat_context,
            )
        except TypeError:
            answer = ask_deepseek(
                message=payload.message,
                vehicle_context=vehicle_context,
            )
    except Exception:
        answer = "Не удалось получить ответ от AI-ассистента. Попробуйте позже."
    return ChatAskResponse(answer=answer)


@app.post("/chat/title", response_model=ChatTitleResponse)
def chat_title(
    payload: ChatTitleRequest,
    request: Request,
    user_id: int = Query(...),
    db: Session = Depends(get_db),
) -> ChatTitleResponse:
    enforce_rate_limit(
        request,
        scope="chat",
        limit=CHAT_RATE_LIMIT,
        window_seconds=CHAT_RATE_LIMIT_WINDOW_SECONDS,
        detail="Too many chat requests",
    )
    get_user(db, user_id)
    return ChatTitleResponse(
        title=generate_chat_title(
            first_user_message=payload.first_user_message,
            first_assistant_reply=payload.first_assistant_reply,
        )
    )


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

    event_mileage = _event_mileage_from_payload(payload, previous_mileage)

    event = Event(
        car_id=car.id,
        type=payload.type,
        description=payload.description,
        amount=payload.amount if payload.amount is not None else 0,
        fuel_liters=payload.fuel_liters if payload.fuel_liters is not None else 0,
        mileage=event_mileage,
        odometer_start=payload.odometer_start,
        odometer_end=payload.odometer_end,
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

    event_mileage = _event_mileage_from_payload(payload, previous_mileage)

    event.type = payload.type
    event.description = payload.description
    event.amount = payload.amount if payload.amount is not None else 0
    event.fuel_liters = payload.fuel_liters if payload.fuel_liters is not None else 0
    event.mileage = event_mileage
    event.odometer_start = payload.odometer_start
    event.odometer_end = payload.odometer_end

    db.commit()
    db.refresh(event)
    return event


@app.post("/events/{event_id}/photo", response_model=EventResponse)
async def upload_event_photo(
    event_id: int,
    file: UploadFile = File(...),
    user_id: int = Query(...),
    db: Session = Depends(get_db),
) -> Event:
    event = get_event_for_user(db, event_id, user_id)
    if event.type != "issue":
        raise HTTPException(
            status_code=400, detail="Photos are supported only for issue events"
        )

    content, mime_type = await _read_valid_photo(file)
    extension = _safe_photo_extension(file.filename, mime_type)
    filename = f"{uuid.uuid4().hex}{extension}"
    destination = EVENT_PHOTO_DIR / filename
    destination.write_bytes(content)

    previous_photo_path = event.photo_path
    event.photo_path = filename
    event.photo_original_name = Path(file.filename or filename).name
    event.photo_mime_type = mime_type
    event.photo_size = len(content)
    db.commit()
    db.refresh(event)

    if previous_photo_path and previous_photo_path != filename:
        previous_file = EVENT_PHOTO_DIR / Path(previous_photo_path).name
        if previous_file.is_file():
            previous_file.unlink(missing_ok=True)
    return event


@app.delete(
    "/events/{event_id}/photo",
    status_code=status.HTTP_204_NO_CONTENT,
    response_model=None,
    response_class=Response,
)
def delete_event_photo(
    event_id: int,
    user_id: int = Query(...),
    db: Session = Depends(get_db),
) -> None:
    event = get_event_for_user(db, event_id, user_id)
    _clear_event_photo(event)
    db.commit()


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
    event = get_event_for_user(db, event_id, user_id)
    _delete_event_photo_file(event)
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


@app.get("/recommendations", response_model=RecommendationsResponse)
def get_recommendations(
    user_id: int = Query(...), db: Session = Depends(get_db)
) -> RecommendationsResponse:
    car = get_car_for_user_id(db, user_id)
    now = datetime.now(timezone.utc).replace(tzinfo=None)
    events = list(
        db.scalars(
            select(Event)
            .where(Event.car_id == car.id)
            .order_by(Event.created_at, Event.id)
        )
    )
    return RecommendationsResponse(
        recommendations=_build_recommendations(events, now, car)
    )
