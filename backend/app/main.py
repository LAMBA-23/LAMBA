from datetime import UTC, datetime, timedelta

from fastapi import Depends, FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import select
from sqlalchemy.orm import Session

from .chat_parser import parse_chat_message
from .database import Base, engine, get_db
from .models import Car, Event, User
from .schemas import (
    CarCreate,
    CarResponse,
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


def build_stats_period(events: list[Event], start_at: datetime | None = None) -> StatsPeriodResponse:
    period_events = [
        event for event in events if start_at is None or event.created_at >= start_at
    ]
    mileages = [event.mileage for event in period_events]
    mileage_km = max(mileages) - min(mileages) if len(mileages) >= 2 else 0

    return StatsPeriodResponse(
        mileage_km=mileage_km,
        expenses_rub=sum(event.amount for event in period_events),
        fuel_liters=0,
        records_count=len(period_events),
        avg_fuel_consumption_l_per_100km=0,
    )


@app.on_event("startup")
def on_startup() -> None:
    Base.metadata.create_all(bind=engine)
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
    if user is None or (user.password != payload.password and not is_demo_compatible_login):
        return LoginResponse(success=False)
    return LoginResponse(success=True, user_id=user.id)


@app.post("/auth/register", response_model=RegisterResponse, status_code=201)
def register(payload: RegisterRequest, db: Session = Depends(get_db)) -> RegisterResponse:
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

    if parsed.type is None or parsed.description is None or not parsed.description.strip():
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
            mileage=parsed.mileage,
        ),
    )


@app.get("/events", response_model=list[EventResponse])
def get_events(user_id: int = Query(...), db: Session = Depends(get_db)) -> list[Event]:
    car = get_car_for_user_id(db, user_id)
    return list(db.scalars(select(Event).where(Event.car_id == car.id).order_by(Event.id)))


@app.post("/events", response_model=EventResponse)
def create_event(
    payload: EventCreate,
    user_id: int = Query(...),
    db: Session = Depends(get_db),
) -> Event:
    car = get_car_for_user_id(db, user_id)
    event = Event(
        car_id=car.id,
        type=payload.type,
        description=payload.description,
        amount=payload.amount if payload.amount is not None else 0,
        mileage=payload.mileage if payload.mileage is not None else car.current_mileage,
    )
    db.add(event)
    db.commit()
    db.refresh(event)
    return event


@app.get("/stats", response_model=StatsResponse)
def get_stats(user_id: int = Query(...), db: Session = Depends(get_db)) -> StatsResponse:
    car = get_car_for_user_id(db, user_id)
    now = datetime.now(UTC).replace(tzinfo=None)
    events = list(
        db.scalars(select(Event).where(Event.car_id == car.id).order_by(Event.created_at, Event.id))
    )

    return StatsResponse(
        week=build_stats_period(events, start_at=now - timedelta(days=7)),
        month=build_stats_period(events, start_at=now - timedelta(days=30)),
        all_time=build_stats_period(events),
    )
