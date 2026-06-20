from fastapi import Depends, FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from .chat_parser import parse_chat_message
from .database import Base, engine, get_db
from .models import Car, Event, User
from .schemas import (
    CarResponse,
    ChatParseRequest,
    ChatParseResponse,
    EventCreate,
    EventResponse,
    LoginRequest,
    LoginResponse,
    ParsedEventPayload,
    StatsResponse,
)

DEMO_USERNAME = "demo"
DEMO_PASSWORD = "demo"
DEMO_COMPATIBLE_PASSWORDS = {DEMO_PASSWORD, "password"}

app = FastAPI(title="LAMBA Backend", version="0.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)


def seed_demo_data(db: Session) -> None:
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


def get_demo_user(db: Session) -> User:
    user = db.scalar(select(User).where(User.username == DEMO_USERNAME))
    if user is None:
        raise HTTPException(status_code=500, detail="Demo user is not initialized")
    return user


def get_demo_car(db: Session) -> Car:
    user = get_demo_user(db)
    car = db.scalar(select(Car).where(Car.user_id == user.id))
    if car is None:
        raise HTTPException(status_code=500, detail="Demo car is not initialized")
    return car


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


@app.get("/vehicle", response_model=CarResponse)
def get_vehicle(db: Session = Depends(get_db)) -> Car:
    return get_demo_car(db)


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
                "\u0434\u0435\u0442\u0430\u043b\u0438 \u0437\u0430\u043f\u0438\u0441\u0438."
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
def get_events(db: Session = Depends(get_db)) -> list[Event]:
    car = get_demo_car(db)
    return list(db.scalars(select(Event).where(Event.car_id == car.id).order_by(Event.id)))


@app.post("/events", response_model=EventResponse)
def create_event(payload: EventCreate, db: Session = Depends(get_db)) -> Event:
    car = get_demo_car(db)
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
def get_stats(db: Session = Depends(get_db)) -> StatsResponse:
    car = get_demo_car(db)

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
