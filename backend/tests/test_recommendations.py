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
    return {
        recommendation["id"].partition(":")[0]
        for recommendation in response_json["recommendations"]
    }


def _recommendation_for(response_json, rule_id: str) -> dict:
    return next(
        recommendation
        for recommendation in response_json["recommendations"]
        if recommendation["id"].partition(":")[0] == rule_id
    )


class TestRecommendationsApi:
    def test_get_recommendations_returns_no_events_hint(self, client):
        user_id = _register_user(client, "recommendations-empty")

        response = client.get(f"/recommendations?user_id={user_id}")
        data = response.json()

        assert response.status_code == 200
        assert data["recommendations"][0]["id"] == "no_events"
        assert data["recommendations"][0]["severity"] == "info"
        assert data["recommendations"][0]["title"] == "Добавьте первую запись"
        assert "В истории пока нет событий" in data["recommendations"][0]["message"]

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
        recommendation = _recommendation_for(response.json(), "high_fuel_price")
        assert recommendation["id"].startswith("high_fuel_price:")
        assert recommendation["title"] == "Высокая стоимость топлива"
        assert "100 ₽/л" in recommendation["message"]

    def test_recommendation_occurrence_id_changes_for_new_source_events(self, client):
        user_id = _register_user(client, "recommendations-occurrence-id")
        _create_event(
            client,
            user_id,
            type="fuel",
            description="First expensive fuel",
            amount=5000,
            fuel_liters=50,
            mileage=100,
        )

        first_response = client.get(f"/recommendations?user_id={user_id}")
        repeated_response = client.get(f"/recommendations?user_id={user_id}")
        first_id = _recommendation_for(first_response.json(), "high_fuel_price")["id"]
        repeated_id = _recommendation_for(repeated_response.json(), "high_fuel_price")[
            "id"
        ]

        assert first_id == repeated_id

        _create_event(
            client,
            user_id,
            type="fuel",
            description="Second expensive fuel",
            amount=4500,
            fuel_liters=45,
            mileage=200,
        )
        new_response = client.get(f"/recommendations?user_id={user_id}")
        new_id = _recommendation_for(new_response.json(), "high_fuel_price")["id"]

        assert new_id != first_id

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
        recommendation = _recommendation_for(
            response.json(), "high_monthly_repair_cost"
        )
        assert recommendation["title"] == "Расходы на ремонт выросли"
        assert "25 000 ₽" in recommendation["message"]

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
        recommendation = _recommendation_for(response.json(), "recent_breakdown")
        assert recommendation["title"] == "Проверьте недавнюю поломку"
        assert "За последние 30 дней" in recommendation["message"]

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
        recommendation = _recommendation_for(response.json(), "stale_records")
        assert recommendation["title"] == "Обновите историю автомобиля"
        assert "Последняя запись была" in recommendation["message"]

    def test_get_recommendations_flags_long_distance_since_fuel(self, client):
        user_id = _register_user(client, "recommendations-distance-since-fuel")
        _create_event(
            client,
            user_id,
            type="fuel",
            description="Affordable fuel",
            amount=1000,
            fuel_liters=20,
            mileage=100,
        )
        _create_event(
            client,
            user_id,
            type="trip",
            description="Long trip",
            amount=0,
            fuel_liters=0,
            mileage=700,
        )

        response = client.get(f"/recommendations?user_id={user_id}")

        assert response.status_code == 200
        assert "long_distance_since_fuel" in _recommendation_ids(response.json())
        recommendation = _recommendation_for(
            response.json(), "long_distance_since_fuel"
        )
        assert recommendation["title"] == "Проверьте уровень топлива"
        assert "600 км" in recommendation["message"]
