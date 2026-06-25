from datetime import UTC, datetime, timedelta

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
    }
    payload.update(overrides)
    return payload


class TestStatsApi:
    def test_get_stats_returns_periods_with_aggregated_values(self, client, db_session):
        user_id = _register_user(client, "stats-user")
        now = datetime.now(UTC).replace(tzinfo=None)

        created_events = [
            client.post(
                f"/events?user_id={user_id}",
                json=_event_payload(
                    type="fuel",
                    description="week fuel",
                    amount=2500,
                    mileage=10000,
                ),
            ),
            client.post(
                f"/events?user_id={user_id}",
                json=_event_payload(
                    type="repair",
                    description="month repair",
                    amount=7000,
                    mileage=10200,
                ),
            ),
            client.post(
                f"/events?user_id={user_id}",
                json=_event_payload(
                    type="trip",
                    description="old trip",
                    amount=0,
                    mileage=10500,
                ),
            ),
        ]

        for response in created_events:
            assert response.status_code == 200

        events = {
            event.description: event
            for event in db_session.query(Event).all()
        }
        events["week fuel"].created_at = now - timedelta(days=3)
        events["month repair"].created_at = now - timedelta(days=10)
        events["old trip"].created_at = now - timedelta(days=40)
        db_session.commit()

        response = client.get(f"/stats?user_id={user_id}")

        assert response.status_code == 200
        assert response.json() == {
            "week": {
                "mileage_km": 0,
                "expenses_rub": 2500,
                "fuel_liters": 0,
                "records_count": 1,
                "avg_fuel_consumption_l_per_100km": 0,
            },
            "month": {
                "mileage_km": 200,
                "expenses_rub": 9500,
                "fuel_liters": 0,
                "records_count": 2,
                "avg_fuel_consumption_l_per_100km": 0,
            },
            "all_time": {
                "mileage_km": 500,
                "expenses_rub": 9500,
                "fuel_liters": 0,
                "records_count": 3,
                "avg_fuel_consumption_l_per_100km": 0,
            },
        }

    def test_get_stats_returns_zeroes_when_user_has_no_events(self, client):
        user_id = _register_user(client, "stats-empty-user")

        response = client.get(f"/stats?user_id={user_id}")

        assert response.status_code == 200
        assert response.json() == {
            "week": {
                "mileage_km": 0,
                "expenses_rub": 0,
                "fuel_liters": 0,
                "records_count": 0,
                "avg_fuel_consumption_l_per_100km": 0,
            },
            "month": {
                "mileage_km": 0,
                "expenses_rub": 0,
                "fuel_liters": 0,
                "records_count": 0,
                "avg_fuel_consumption_l_per_100km": 0,
            },
            "all_time": {
                "mileage_km": 0,
                "expenses_rub": 0,
                "fuel_liters": 0,
                "records_count": 0,
                "avg_fuel_consumption_l_per_100km": 0,
            },
        }
