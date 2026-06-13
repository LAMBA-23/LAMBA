from datetime import datetime
from typing import Literal

from pydantic import BaseModel, ConfigDict


EventType = Literal["fuel", "repair", "trip", "issue"]


class LoginRequest(BaseModel):
    username: str
    password: str


class LoginResponse(BaseModel):
    success: bool
    user_id: int | None = None


class CarResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    brand: str
    model: str
    production_year: int
    current_mileage: int
    created_at: datetime


class EventCreate(BaseModel):
    type: EventType
    description: str
    amount: int | None = None
    mileage: int | None = None


class EventResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    type: EventType
    description: str
    amount: int
    mileage: int
    created_at: datetime


class StatsResponse(BaseModel):
    fuel_expenses: int
    repair_expenses: int
    trip_count: int
    total_recorded_mileage: int
