from __future__ import annotations

from datetime import datetime
from decimal import Decimal, InvalidOperation
from typing import Any, Literal

from pydantic import (
    BaseModel,
    ConfigDict,
    Field,
    field_serializer,
    field_validator,
    model_validator,
)


EventType = Literal["fuel", "repair", "trip", "issue"]
ChatParseStatus = Literal["parsed", "clarification_needed"]
DECIMAL_PLACES = Decimal("0.001")


def _parse_decimal(value: Any) -> Decimal | None:
    if value is None:
        return None
    if isinstance(value, Decimal):
        decimal_value = value
    elif isinstance(value, str):
        normalized = value.strip().replace(" ", "").replace(",", ".")
        if not normalized:
            return None
        try:
            decimal_value = Decimal(normalized)
        except InvalidOperation as exc:
            raise ValueError("value must be a valid decimal") from exc
    else:
        try:
            decimal_value = Decimal(str(value))
        except InvalidOperation as exc:
            raise ValueError("value must be a valid decimal") from exc
    if not decimal_value.is_finite():
        raise ValueError("value must be a finite decimal")
    return decimal_value


def _fraction_digits(value: Decimal) -> int:
    normalized = value.normalize()
    return max(0, -normalized.as_tuple().exponent)


def _non_negative_decimal(value: Any) -> Decimal | None:
    decimal_value = _parse_decimal(value)
    if decimal_value is None:
        return None
    if _fraction_digits(decimal_value) > 3:
        raise ValueError("value must have no more than 3 decimal places")
    if decimal_value < 0:
        raise ValueError("value must not be negative")
    return decimal_value


def _decimal_with_max_places(value: Any) -> Decimal | None:
    decimal_value = _parse_decimal(value)
    if decimal_value is None:
        return None
    if _fraction_digits(decimal_value) > 3:
        raise ValueError("value must have no more than 3 decimal places")
    return decimal_value


def _decimal_json(value: Decimal | None) -> int | float | None:
    if value is None:
        return None
    normalized = value.quantize(DECIMAL_PLACES).normalize()
    if normalized == normalized.to_integral_value():
        return int(normalized)
    return float(format(normalized, "f"))


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
    amount: Decimal | None = None
    fuel_liters: Decimal | None = None
    mileage: Decimal | None = None
    odometer_start: int | None = None
    odometer_end: int | None = None

    @field_validator("description")
    @classmethod
    def description_not_empty(cls, v: str) -> str:
        description = v.strip()
        if not description:
            raise ValueError("description must not be empty")
        return description

    @field_validator("amount", "fuel_liters", "mileage", mode="before")
    @classmethod
    def valid_decimal_value(cls, v: Any) -> Decimal | None:
        return _non_negative_decimal(v)

    @field_validator("odometer_start", "odometer_end")
    @classmethod
    def non_negative_odometer_value(cls, v: int | None) -> int | None:
        if v is not None and v < 0:
            raise ValueError("odometer values must not be negative")
        return v

    @model_validator(mode="after")
    def valid_trip_odometer_range(self) -> "EventCreate":
        has_any_odometer = (
            self.odometer_start is not None or self.odometer_end is not None
        )
        has_full_odometer = (
            self.odometer_start is not None and self.odometer_end is not None
        )
        if has_any_odometer and self.type != "trip":
            raise ValueError("odometer_start and odometer_end are only for trip events")
        if has_any_odometer and not has_full_odometer:
            raise ValueError(
                "odometer_start and odometer_end must be provided together"
            )
        if (
            has_full_odometer
            and self.odometer_end is not None
            and self.odometer_start is not None
            and self.odometer_end < self.odometer_start
        ):
            raise ValueError(
                "odometer_end must be greater than or equal to odometer_start"
            )
        return self


class EventResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    type: EventType
    description: str
    amount: Decimal
    fuel_liters: Decimal
    mileage: Decimal
    odometer_start: int | None = None
    odometer_end: int | None = None
    trip_distance: Decimal | None = None
    created_at: datetime

    @field_serializer("amount", "fuel_liters", "mileage", "trip_distance")
    def serialize_decimal(self, value: Decimal | None) -> int | float | None:
        return _decimal_json(value)


class ChatParseRequest(BaseModel):
    message: str


class ParsedEventPayload(BaseModel):
    type: EventType
    description: str
    amount: Decimal | None = None
    fuel_liters: Decimal | None = None
    mileage: Decimal | None = None

    @field_validator("amount", "fuel_liters", "mileage", mode="before")
    @classmethod
    def valid_decimal_value(cls, v: Any) -> Decimal | None:
        return _non_negative_decimal(v)

    @field_serializer("amount", "fuel_liters", "mileage")
    def serialize_decimal(self, value: Decimal | None) -> int | float | None:
        return _decimal_json(value)


class ParsedChatEvent(BaseModel):
    type: EventType | None = None
    description: str | None = None
    amount: Decimal | None = None
    fuel_liters: Decimal | None = None
    mileage: Decimal | None = None
    needs_clarification: bool
    clarification_question: str | None = None

    @field_validator("amount", "fuel_liters", "mileage", mode="before")
    @classmethod
    def valid_decimal_value(cls, v: Any) -> Decimal | None:
        return _decimal_with_max_places(v)

    @field_serializer("amount", "fuel_liters", "mileage")
    def serialize_decimal(self, value: Decimal | None) -> int | float | None:
        return _decimal_json(value)


class ChatParseResponse(BaseModel):
    status: ChatParseStatus
    parsed_event: ParsedEventPayload | None = None
    clarification_question: str | None = None


class StatsPeriodResponse(BaseModel):
    mileage: Decimal
    total_expenses: Decimal
    fuel_expenses: Decimal
    repair_expenses: Decimal
    records_count: int
    avg_fuel_consumption: Decimal
    avg_expense_consumption: Decimal
    mileage_km: Decimal
    expenses_rub: Decimal
    fuel_liters: Decimal
    avg_fuel_consumption_l_per_100km: Decimal

    @field_serializer(
        "mileage",
        "total_expenses",
        "fuel_expenses",
        "repair_expenses",
        "avg_fuel_consumption",
        "avg_expense_consumption",
        "mileage_km",
        "expenses_rub",
        "fuel_liters",
        "avg_fuel_consumption_l_per_100km",
    )
    def serialize_decimal(self, value: Decimal) -> int | float | None:
        return _decimal_json(value)


class StatsResponse(BaseModel):
    fuel_expenses: Decimal
    repair_expenses: Decimal
    trip_count: int
    total_recorded_mileage: Decimal
    week: StatsPeriodResponse
    month: StatsPeriodResponse
    all_time: StatsPeriodResponse

    @field_serializer("fuel_expenses", "repair_expenses", "total_recorded_mileage")
    def serialize_decimal(self, value: Decimal) -> int | float | None:
        return _decimal_json(value)


class ChatAskRequest(BaseModel):
    message: str = Field(min_length=1)
    chat_context: list["ChatContextMessage"] | None = None

    @field_validator("message")
    @classmethod
    def message_not_blank(cls, v: str) -> str:
        trimmed = v.strip()
        if not trimmed:
            raise ValueError("message must not be empty or blank")
        return trimmed


class ChatAskResponse(BaseModel):
    answer: str


class ChatContextMessage(BaseModel):
    sender: Literal["user", "assistant", "system"]
    text: str = Field(min_length=1)

    @field_validator("text")
    @classmethod
    def text_not_blank(cls, v: str) -> str:
        trimmed = v.strip()
        if not trimmed:
            raise ValueError("text must not be empty or blank")
        return trimmed


class ChatTitleRequest(BaseModel):
    first_user_message: str = Field(min_length=1)
    first_assistant_reply: str = Field(min_length=1)

    @field_validator("first_user_message", "first_assistant_reply")
    @classmethod
    def title_fields_not_blank(cls, v: str) -> str:
        trimmed = v.strip()
        if not trimmed:
            raise ValueError("title fields must not be empty or blank")
        return trimmed


class ChatTitleResponse(BaseModel):
    title: str
