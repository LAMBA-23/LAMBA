from datetime import datetime
from decimal import Decimal

from sqlalchemy import ForeignKey, Integer, Numeric, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from .database import Base


class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    username: Mapped[str] = mapped_column(
        String(64), unique=True, index=True, nullable=False
    )
    password: Mapped[str] = mapped_column(String(128), nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        default=datetime.utcnow, nullable=False
    )

    car: Mapped["Car"] = relationship(back_populates="user", uselist=False)


class Car(Base):
    __tablename__ = "cars"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    user_id: Mapped[int] = mapped_column(
        ForeignKey("users.id"), unique=True, nullable=False
    )
    brand: Mapped[str] = mapped_column(String(64), nullable=False)
    model: Mapped[str] = mapped_column(String(64), nullable=False)
    production_year: Mapped[int] = mapped_column(Integer, nullable=False)
    current_mileage: Mapped[int] = mapped_column(Integer, nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        default=datetime.utcnow, nullable=False
    )

    user: Mapped[User] = relationship(back_populates="car")
    events: Mapped[list["Event"]] = relationship(back_populates="car")


class Event(Base):
    __tablename__ = "events"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    car_id: Mapped[int] = mapped_column(ForeignKey("cars.id"), nullable=False)
    type: Mapped[str] = mapped_column(String(32), nullable=False)
    description: Mapped[str] = mapped_column(String(500), nullable=False)
    amount: Mapped[Decimal] = mapped_column(Numeric(12, 3), default=0, nullable=False)
    fuel_liters: Mapped[Decimal] = mapped_column(
        Numeric(12, 3), default=0, nullable=False
    )
    mileage: Mapped[Decimal] = mapped_column(Numeric(12, 3), nullable=False)
    odometer_start: Mapped[int | None] = mapped_column(Integer, nullable=True)
    odometer_end: Mapped[int | None] = mapped_column(Integer, nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        default=datetime.utcnow, nullable=False
    )

    car: Mapped[Car] = relationship(back_populates="events")

    @property
    def trip_distance(self) -> Decimal | None:
        if (
            self.type == "trip"
            and self.odometer_start is not None
            and self.odometer_end is not None
        ):
            return max(Decimal("0"), Decimal(self.odometer_end - self.odometer_start))
        return None
