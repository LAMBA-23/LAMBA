from fastapi import Depends, FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from .database import Base, engine, get_db
from .models import Car, Event, User
from .schemas import (
    CarResponse,
    EventCreate,
    EventResponse,
    LoginRequest,
    LoginResponse,
    RegisterRequest,
    RegisterResponse,
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

    fuel_expenses = db.scalar(
        select(func.coalesce(func.sum(Event.amount), 0)).where(
            Event.car_id == car.id,
            Event.type == "fuel",
        )
    )
    repair_expenses = db.scalar(
        select(func.coalesce(func.sum(Event.amount), 0)).where(
            Event.car_id == car.id,
            Event.type == "repair",
        )
    )
    trip_count = db.scalar(
        select(func.count(Event.id)).where(
            Event.car_id == car.id,
            Event.type == "trip",
        )
    )
    max_mileage = db.scalar(
        select(func.max(Event.mileage)).where(Event.car_id == car.id)
    )

    return StatsResponse(
        fuel_expenses=fuel_expenses or 0,
        repair_expenses=repair_expenses or 0,
        trip_count=trip_count or 0,
        total_recorded_mileage=max_mileage or car.current_mileage,
    )
