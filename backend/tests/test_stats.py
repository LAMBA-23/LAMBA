from datetime import UTC, datetime, timedelta

from app.main import build_stats_period
from app.models import Event


def _register_user(client, username: str) -> int:
    response = client.post(
        "/auth/register",
        json={"username": username, "password": "password123"},
    )
    assert response.status_code == 201
    return response.json()["user_id"]


def _event_payload(**overrides):
    payload = {
        "type": "fuel",
        "description": "Event",
        "amount": 0,
        "mileage": 0,
        "fuel_liters": 0,
    }
    payload.update(overrides)
    return payload


def _create_event(client, user_id: int, **overrides):
    response = client.post(
        f"/events?user_id={user_id}", json=_event_payload(**overrides)
    )
    assert response.status_code == 200
    return response


def _set_event_created_at(db_session, description: str, created_at: datetime) -> None:
    event = db_session.query(Event).filter_by(description=description).one()
    event.created_at = created_at
    db_session.commit()


class TestStatsApi:
    def test_get_stats_returns_zeroes_when_user_has_no_events(self, client):
        user_id = _register_user(client, "stats-empty-user")

        response = client.get(f"/stats?user_id={user_id}")
        data = response.json()

        assert response.status_code == 200
        assert data["fuel_expenses"] == 0
        assert data["repair_expenses"] == 0
        assert data["trip_count"] == 0
        assert data["total_recorded_mileage"] == 0
        assert data["week"]["mileage"] == 0
        assert data["week"]["total_expenses"] == 0
        assert data["week"]["records_count"] == 0
        assert data["week"]["fuel_liters"] == 0
        assert data["month"]["mileage"] == 0
        assert data["month"]["total_expenses"] == 0
        assert data["month"]["records_count"] == 0
        assert data["month"]["fuel_liters"] == 0
        assert data["all_time"]["mileage"] == 0
        assert data["all_time"]["total_expenses"] == 0
        assert data["all_time"]["records_count"] == 0
        assert data["all_time"]["fuel_liters"] == 0

    def test_get_stats_counts_trip_distance_fuel_liters_and_all_period_records(
        self, client
    ):
        user_id = _register_user(client, "stats-relevant-events")
        vehicle_response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "BMW",
                "model": "M4",
                "production_year": 2020,
                "current_mileage": 125000,
            },
        )

        _create_event(
            client,
            user_id,
            type="fuel",
            description="fuel expense",
            amount=2500,
            mileage=125000,
            fuel_liters=40,
        )
        _create_event(
            client,
            user_id,
            type="repair",
            description="repair expense",
            amount=7000,
            mileage=125050,
        )
        _create_event(
            client,
            user_id,
            type="trip",
            description="trip mileage",
            amount=0,
            mileage=125100,
        )
        _create_event(
            client,
            user_id,
            type="issue",
            description="ignored issue",
            amount=999,
            mileage=888,
        )
        response = client.get(f"/stats?user_id={user_id}")
        data = response.json()

        assert vehicle_response.status_code == 201
        assert response.status_code == 200
        assert data["all_time"]["mileage"] == 100
        assert data["all_time"]["total_expenses"] == 9500
        assert data["all_time"]["fuel_expenses"] == 2500
        assert data["all_time"]["repair_expenses"] == 7000
        assert data["all_time"]["records_count"] == 5
        assert data["all_time"]["fuel_liters"] == 40
        assert data["all_time"]["avg_fuel_consumption"] == 0
        assert data["all_time"]["avg_expense_consumption"] == 0
        assert data["fuel_expenses"] == 2500
        assert data["repair_expenses"] == 7000
        assert data["trip_count"] == 1
        assert data["total_recorded_mileage"] == 100

    def test_manual_issue_event_counts_as_record_without_expenses_or_mileage(
        self, client
    ):
        user_id = _register_user(client, "stats-manual-issue")

        issue_response = _create_event(
            client,
            user_id,
            type="issue",
            description="Manual issue note",
            amount=5000,
            mileage=250,
            fuel_liters=10,
        )
        stats_response = client.get(f"/stats?user_id={user_id}")
        timeline_response = client.get(f"/events?user_id={user_id}")
        data = stats_response.json()

        assert issue_response.status_code == 200
        assert stats_response.status_code == 200
        assert timeline_response.status_code == 200
        assert len(timeline_response.json()) == 1
        assert data["all_time"]["records_count"] == 1
        assert data["all_time"]["total_expenses"] == 0
        assert data["all_time"]["fuel_expenses"] == 0
        assert data["all_time"]["repair_expenses"] == 0
        assert data["all_time"]["mileage"] == 0
        assert data["all_time"]["fuel_liters"] == 0
        assert data["trip_count"] == 0
        assert data["total_recorded_mileage"] == 0

    def test_get_stats_uses_trip_distance_payload_to_store_and_report_delta(
        self, client
    ):
        user_id = _register_user(client, "stats-trip-distance-payload")
        vehicle_response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "BMW",
                "model": "M4",
                "production_year": 2020,
                "current_mileage": 125000,
            },
        )

        trip_response = _create_event(
            client,
            user_id,
            type="trip",
            description="drove 100 km",
            amount=0,
            mileage=100,
        )
        stats_response = client.get(f"/stats?user_id={user_id}")

        assert vehicle_response.status_code == 201
        assert trip_response.status_code == 200
        assert trip_response.json()["mileage"] == 125100
        assert stats_response.status_code == 200
        assert stats_response.json()["all_time"]["mileage"] == 100
        assert stats_response.json()["total_recorded_mileage"] == 100

    def test_get_stats_sums_multiple_trip_deltas_from_initial_mileage(self, client):
        user_id = _register_user(client, "stats-multiple-trips")
        vehicle_response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "BMW",
                "model": "M4",
                "production_year": 2020,
                "current_mileage": 125000,
            },
        )

        first_trip = _create_event(
            client,
            user_id,
            type="trip",
            description="first trip",
            amount=0,
            mileage=125100,
        )
        second_trip = _create_event(
            client,
            user_id,
            type="trip",
            description="second trip",
            amount=0,
            mileage=125250,
        )
        stats_response = client.get(f"/stats?user_id={user_id}")

        assert vehicle_response.status_code == 201
        assert first_trip.status_code == 200
        assert second_trip.status_code == 200
        assert stats_response.status_code == 200
        assert stats_response.json()["all_time"]["mileage"] == 250
        assert stats_response.json()["total_recorded_mileage"] == 250

    def test_get_stats_filters_week_month_and_all_time_by_created_at(
        self, client, db_session
    ):
        user_id = _register_user(client, "stats-periods-user")
        now = datetime.now(UTC).replace(tzinfo=None)

        _create_event(
            client,
            user_id,
            type="fuel",
            description="week fuel",
            amount=2500,
            mileage=100,
        )
        _create_event(
            client,
            user_id,
            type="repair",
            description="month repair",
            amount=7000,
            mileage=0,
        )
        _create_event(
            client,
            user_id,
            type="trip",
            description="old trip",
            amount=0,
            mileage=300,
        )

        _set_event_created_at(db_session, "week fuel", now - timedelta(days=3))
        _set_event_created_at(db_session, "month repair", now - timedelta(days=10))
        _set_event_created_at(db_session, "old trip", now - timedelta(days=40))

        response = client.get(f"/stats?user_id={user_id}")
        data = response.json()

        assert response.status_code == 200
        assert data["fuel_expenses"] == 2500
        assert data["repair_expenses"] == 7000
        assert data["trip_count"] == 1
        assert data["total_recorded_mileage"] == 300
        assert data["week"]["mileage"] == 0
        assert data["week"]["total_expenses"] == 2500
        assert data["week"]["fuel_expenses"] == 2500
        assert data["week"]["repair_expenses"] == 0
        assert data["week"]["records_count"] == 1
        assert data["week"]["avg_fuel_consumption"] == 0
        assert data["week"]["avg_expense_consumption"] == 0
        assert data["month"]["mileage"] == 0
        assert data["month"]["total_expenses"] == 9500
        assert data["month"]["fuel_expenses"] == 2500
        assert data["month"]["repair_expenses"] == 7000
        assert data["month"]["records_count"] == 2
        assert data["month"]["avg_fuel_consumption"] == 0
        assert data["month"]["avg_expense_consumption"] == 0
        assert data["all_time"]["mileage"] == 300
        assert data["all_time"]["total_expenses"] == 9500
        assert data["all_time"]["fuel_expenses"] == 2500
        assert data["all_time"]["repair_expenses"] == 7000
        assert data["all_time"]["records_count"] == 3
        assert data["all_time"]["avg_fuel_consumption"] == 0
        assert data["all_time"]["avg_expense_consumption"] == 0

    def test_get_stats_is_updated_after_creating_event(self, client):
        user_id = _register_user(client, "stats-updated-user")

        empty_response = client.get(f"/stats?user_id={user_id}")
        create_response = _create_event(
            client,
            user_id,
            type="fuel",
            description="new fuel",
            amount=4200,
            mileage=0,
        )
        updated_response = client.get(f"/stats?user_id={user_id}")

        assert empty_response.status_code == 200
        assert create_response.status_code == 200
        assert updated_response.status_code == 200
        assert empty_response.json()["all_time"]["total_expenses"] == 0
        assert updated_response.json()["all_time"]["fuel_expenses"] == 4200
        assert updated_response.json()["all_time"]["total_expenses"] == 4200
        assert updated_response.json()["fuel_expenses"] == 4200

    def test_build_stats_period_treats_null_amount_and_mileage_as_zero(self):
        period = build_stats_period(
            [
                Event(
                    car_id=1,
                    type="fuel",
                    description="fuel without amount",
                    amount=None,
                    mileage=None,
                ),
                Event(
                    car_id=1,
                    type="trip",
                    description="trip without mileage",
                    amount=0,
                    mileage=None,
                ),
            ]
        )

        assert period.mileage == 0
        assert period.total_expenses == 0
        assert period.fuel_expenses == 0
        assert period.repair_expenses == 0
        assert period.records_count == 2
        assert period.avg_fuel_consumption == 0
        assert period.avg_expense_consumption == 0
