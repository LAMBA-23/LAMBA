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
        "fuel_liters": 0,
    }
    payload.update(overrides)
    return payload


def _create_event(client, user_id: int, **overrides):
    response = client.post(
        f"/events?user_id={user_id}",
        json=_event_payload(**overrides),
    )
    assert response.status_code == 200
    return response


def _set_event_created_at(db_session, description: str, created_at: datetime) -> None:
    event = db_session.query(Event).filter_by(description=description).one()
    event.created_at = created_at
    db_session.commit()


def _recommendation_ids(response_json) -> set[str]:
    return {recommendation["id"] for recommendation in response_json["recommendations"]}


class TestRecommendationsApi:
    def test_get_recommendations_returns_no_events_hint(self, client):
        user_id = _register_user(client, "recommendations-empty")

        response = client.get(f"/recommendations?user_id={user_id}")
        data = response.json()

        assert response.status_code == 200
        assert data["recommendations"][0]["id"] == "no_events"
        assert data["recommendations"][0]["severity"] == "info"

    def test_get_recommendations_flags_high_fuel_price(self, client):
        user_id = _register_user(client, "recommendations-fuel-price")

        _create_event(
            client,
            user_id,
            type="fuel",
            description="Expensive fuel",
            amount=5000,
            fuel_liters=50,
            mileage=100,
        )

        response = client.get(f"/recommendations?user_id={user_id}")

        assert response.status_code == 200
        assert "high_fuel_price" in _recommendation_ids(response.json())

    def test_get_recommendations_flags_high_monthly_repair_cost(self, client):
        user_id = _register_user(client, "recommendations-repair-cost")

        _create_event(
            client,
            user_id,
            type="repair",
            description="Major repair",
            amount=25000,
            mileage=100,
        )

        response = client.get(f"/recommendations?user_id={user_id}")

        assert response.status_code == 200
        assert "high_monthly_repair_cost" in _recommendation_ids(response.json())

    def test_get_recommendations_flags_recent_breakdown(self, client):
        user_id = _register_user(client, "recommendations-breakdown")

        _create_event(
            client,
            user_id,
            type="issue",
            description="Check engine",
            amount=0,
            mileage=100,
        )

        response = client.get(f"/recommendations?user_id={user_id}")

        assert response.status_code == 200
        assert "recent_breakdown" in _recommendation_ids(response.json())

    def test_get_recommendations_flags_stale_records(self, client, db_session):
        user_id = _register_user(client, "recommendations-stale")
        now = datetime.now(UTC).replace(tzinfo=None)

        _create_event(
            client,
            user_id,
            type="fuel",
            description="Old fuel",
            amount=1000,
            fuel_liters=20,
            mileage=100,
        )
        _set_event_created_at(db_session, "Old fuel", now - timedelta(days=20))

        response = client.get(f"/recommendations?user_id={user_id}")

        assert response.status_code == 200
        assert "stale_records" in _recommendation_ids(response.json())
