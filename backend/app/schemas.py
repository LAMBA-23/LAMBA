from datetime import datetime
from typing import Literal

from pydantic import BaseModel, ConfigDict, field_validator


EventType = Literal["fuel", "repair", "trip", "issue"]


class LoginRequest(BaseModel):
    username: str
    password: str


class LoginResponse(BaseModel):
    success: bool
    user_id: int | None = None


class CarCreate(BaseModel):
    user_id: int
    brand: str
    model: str
    production_year: int
    current_mileage: int

    @field_validator("brand")
    @classmethod
    def brand_not_empty(cls, v: str) -> str:
        if not v.strip():
            raise ValueError("brand must not be empty")
        return v.strip()

    @field_validator("model")
    @classmethod
    def model_not_empty(cls, v: str) -> str:
        if not v.strip():
            raise ValueError("model must not be empty")
        return v.strip()

    @field_validator("production_year")
    @classmethod
    def valid_year(cls, v: int) -> int:
        if v < 1886 or v > 2100:
            raise ValueError("production_year must be between 1886 and 2100")
        return v

    @field_validator("current_mileage")
    @classmethod
    def non_negative_mileage(cls, v: int) -> int:
        if v < 0:
            raise ValueError("current_mileage must not be negative")
        return v


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
