from __future__ import annotations

from datetime import datetime
from typing import Any, Literal

from pydantic import BaseModel, ConfigDict, Field, field_validator


EventType = Literal["fuel", "repair", "trip", "issue"]
ChatParseStatus = Literal["parsed", "clarification_needed"]


class LoginRequest(BaseModel):
    username: str
    password: str


class LoginResponse(BaseModel):
    success: bool
    user_id: int | None = None


class RegisterRequest(BaseModel):
    username: str = Field(min_length=1, max_length=64)
    password: str = Field(min_length=8, max_length=128)

    @field_validator("username", mode="before")
    @classmethod
    def username_must_not_be_blank(cls, value: Any) -> Any:
        if not isinstance(value, str):
            return value
        username = value.strip()
        if not username:
            raise ValueError("Username must not be blank")
        return username


class RegisterResponse(BaseModel):
    success: bool
    user_id: int


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
    fuel_liters: int | None = None
    mileage: int | None = None

    @field_validator("description")
    @classmethod
    def description_not_empty(cls, v: str) -> str:
        description = v.strip()
        if not description:
            raise ValueError("description must not be empty")
        return description

    @field_validator("amount")
    @classmethod
    def non_negative_amount(cls, v: int | None) -> int | None:
        if v is not None and v < 0:
            raise ValueError("amount must not be negative")
        return v

    @field_validator("fuel_liters")
    @classmethod
    def non_negative_fuel_liters(cls, v: int | None) -> int | None:
        if v is not None and v < 0:
            raise ValueError("fuel_liters must not be negative")
        return v

    @field_validator("mileage")
    @classmethod
    def non_negative_event_mileage(cls, v: int | None) -> int | None:
        if v is not None and v < 0:
            raise ValueError("mileage must not be negative")
        return v


class EventResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    type: EventType
    description: str
    amount: int
    fuel_liters: int
    mileage: int
    created_at: datetime


class ChatParseRequest(BaseModel):
    message: str


class ParsedEventPayload(BaseModel):
    type: EventType
    description: str
    amount: int | None = None
    mileage: int | None = None


class ParsedChatEvent(BaseModel):
    type: EventType | None = None
    description: str | None = None
    amount: int | None = None
    mileage: int | None = None
    needs_clarification: bool
    clarification_question: str | None = None


class ChatParseResponse(BaseModel):
    status: ChatParseStatus
    parsed_event: ParsedEventPayload | None = None
    clarification_question: str | None = None


class StatsPeriodResponse(BaseModel):
    mileage: int
    total_expenses: int
    fuel_expenses: int
    repair_expenses: int
    records_count: int
    avg_fuel_consumption: int
    avg_expense_consumption: int
    mileage_km: int
    expenses_rub: int
    fuel_liters: int
    avg_fuel_consumption_l_per_100km: int


class StatsResponse(BaseModel):
    fuel_expenses: int
    repair_expenses: int
    trip_count: int
    total_recorded_mileage: int
    week: StatsPeriodResponse
    month: StatsPeriodResponse
    all_time: StatsPeriodResponse


class ChatAskRequest(BaseModel):
    message: str = Field(min_length=1)

    @field_validator("message")
    @classmethod
    def message_not_blank(cls, v: str) -> str:
        trimmed = v.strip()
        if not trimmed:
            raise ValueError("message must not be empty or blank")
        return trimmed


class ChatAskResponse(BaseModel):
    answer: str
