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
        "description": "Full tank",
        "amount": 60,
        "mileage": 125000,
        "fuel_liters": 0,
    }
    payload.update(overrides)
    return payload


class TestEventsApi:
    def test_get_events_returns_only_user_events_in_stable_order(self, client):
        first_user_id = _register_user(client, "events-user-1")
        second_user_id = _register_user(client, "events-user-2")

        first_event = client.post(
            f"/events?user_id={first_user_id}",
            json=_event_payload(description="First user fuel", mileage=100),
        )
        other_user_event = client.post(
            f"/events?user_id={second_user_id}",
            json=_event_payload(description="Second user fuel", mileage=200),
        )
        second_event = client.post(
            f"/events?user_id={first_user_id}",
            json=_event_payload(
                type="repair", description="First user repair", mileage=300
            ),
        )

        response = client.get(f"/events?user_id={first_user_id}")

        assert first_event.status_code == 200
        assert other_user_event.status_code == 200
        assert second_event.status_code == 200
        assert response.status_code == 200
        data = response.json()
        assert [event["description"] for event in data] == [
            "First user fuel",
            "First user repair",
        ]
        assert [event["id"] for event in data] == sorted(event["id"] for event in data)
        assert "Second user fuel" not in [event["description"] for event in data]

    def test_post_event_is_saved_and_uses_default_amount_and_zero_mileage(self, client):
        user_id = _register_user(client, "events-defaults")
        vehicle_response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "Toyota",
                "model": "Camry",
                "production_year": 2023,
                "current_mileage": 12345,
            },
        )

        create_response = client.post(
            f"/events?user_id={user_id}",
            json={"type": "condition", "description": "Car condition is good"},
        )
        timeline_response = client.get(f"/events?user_id={user_id}")

        assert vehicle_response.status_code == 201
        assert create_response.status_code == 200
        created_event = create_response.json()
        assert created_event["type"] == "condition"
        assert created_event["description"] == "Car condition is good"
        assert created_event["amount"] == 0
        assert created_event["mileage"] == 0
        assert created_event["fuel_liters"] == 0
        assert timeline_response.status_code == 200
        assert timeline_response.json() == [created_event]

    def test_post_fuel_without_mileage_after_trips_does_not_reuse_initial_mileage(
        self, client
    ):
        user_id = _register_user(client, "events-fuel-without-mileage-after-trips")
        vehicle_response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "Toyota",
                "model": "Camry",
                "production_year": 2023,
                "current_mileage": 64,
            },
        )

        first_trip = client.post(
            f"/events?user_id={user_id}",
            json={
                "type": "trip",
                "description": "Drove 10 km",
                "amount": 0,
                "mileage": 10,
            },
        )
        second_trip = client.post(
            f"/events?user_id={user_id}",
            json={
                "type": "trip",
                "description": "Drove 10 km again",
                "amount": 0,
                "mileage": 10,
            },
        )
        fuel_response = client.post(
            f"/events?user_id={user_id}",
            json={
                "type": "fuel",
                "description": "Fuel 10 liters",
                "fuel_liters": 10,
            },
        )

        assert vehicle_response.status_code == 201
        assert first_trip.status_code == 200
        assert first_trip.json()["mileage"] == 74
        assert second_trip.status_code == 200
        assert second_trip.json()["mileage"] == 84
        assert fuel_response.status_code == 200
        assert fuel_response.json()["fuel_liters"] == 10
        assert fuel_response.json()["amount"] == 0
        assert fuel_response.json()["mileage"] == 0

    def test_manual_add_flow_starts_empty_then_returns_created_event(self, client):
        user_id = _register_user(client, "events-manual-add")

        empty_timeline_response = client.get(f"/events?user_id={user_id}")
        create_response = client.post(
            f"/events?user_id={user_id}",
            json={
                "type": "fuel",
                "description": "  Manual fuel record  ",
                "amount": 75,
                "mileage": 126000,
            },
        )
        timeline_response = client.get(f"/events?user_id={user_id}")

        assert empty_timeline_response.status_code == 200
        assert empty_timeline_response.json() == []
        assert create_response.status_code == 200
        created_event = create_response.json()
        assert created_event["description"] == "Manual fuel record"
        assert created_event["amount"] == 75
        assert created_event["mileage"] == 126000
        assert created_event["fuel_liters"] == 0
        assert timeline_response.status_code == 200
        assert timeline_response.json() == [created_event]

    def test_post_trip_distance_stores_new_odometer_and_keeps_timeline_response(
        self, client
    ):
        user_id = _register_user(client, "events-trip-distance")
        vehicle_response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "Toyota",
                "model": "Camry",
                "production_year": 2023,
                "current_mileage": 125000,
            },
        )

        create_response = client.post(
            f"/events?user_id={user_id}",
            json={
                "type": "trip",
                "description": "Drove 100 km",
                "amount": 0,
                "mileage": 100,
            },
        )
        timeline_response = client.get(f"/events?user_id={user_id}")

        assert vehicle_response.status_code == 201
        assert create_response.status_code == 200
        created_event = create_response.json()
        assert created_event["mileage"] == 125100
        assert created_event["fuel_liters"] == 0
        assert timeline_response.status_code == 200
        assert timeline_response.json() == [created_event]

    def test_post_trip_extracts_distance_from_description_when_mileage_is_missing(
        self, client
    ):
        user_id = _register_user(client, "events-trip-description-distance")
        vehicle_response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "Mercedes",
                "model": "G3",
                "production_year": 2023,
                "current_mileage": 0,
            },
        )

        create_response = client.post(
            f"/events?user_id={user_id}",
            json={
                "type": "trip",
                "description": "Поездка на 100 км",
                "amount": 0,
                "mileage": None,
            },
        )
        timeline_response = client.get(f"/events?user_id={user_id}")

        assert vehicle_response.status_code == 201
        assert create_response.status_code == 200
        created_event = create_response.json()
        assert created_event["mileage"] == 100
        assert timeline_response.status_code == 200
        assert timeline_response.json() == [created_event]

    def test_post_trip_distance_uses_previous_trip_not_latest_non_trip_mileage(
        self, client
    ):
        user_id = _register_user(client, "events-trip-after-condition")
        vehicle_response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "Mercedes",
                "model": "G3",
                "production_year": 2023,
                "current_mileage": 76000,
            },
        )

        first_trip = client.post(
            f"/events?user_id={user_id}",
            json={
                "type": "trip",
                "description": "First trip 100 km",
                "amount": 0,
                "mileage": 100,
            },
        )
        condition = client.post(
            f"/events?user_id={user_id}",
            json={
                "type": "condition",
                "description": "Condition note with stale mileage",
                "amount": 0,
                "mileage": 61004,
            },
        )
        second_trip = client.post(
            f"/events?user_id={user_id}",
            json={
                "type": "trip",
                "description": "Second trip 100 km",
                "amount": 0,
                "mileage": 100,
            },
        )
        timeline_response = client.get(f"/events?user_id={user_id}")

        assert vehicle_response.status_code == 201
        assert first_trip.status_code == 200
        assert condition.status_code == 200
        assert second_trip.status_code == 200
        assert first_trip.json()["mileage"] == 76100
        assert condition.json()["mileage"] == 61004
        assert second_trip.json()["mileage"] == 76200
        assert [event["mileage"] for event in timeline_response.json()] == [
            76100,
            61004,
            76200,
        ]

    def test_events_return_expected_errors_for_missing_or_unknown_user(self, client):
        valid_payload = _event_payload()

        assert client.get("/events").status_code == 422
        assert client.get("/events?user_id=99999").status_code == 404
        assert client.post("/events", json=valid_payload).status_code == 422
        assert (
            client.post("/events?user_id=99999", json=valid_payload).status_code == 404
        )

    def test_post_event_rejects_invalid_payload(self, client):
        user_id = _register_user(client, "events-invalid")

        invalid_payloads = [
            _event_payload(type="unknown"),
            _event_payload(description="   "),
            _event_payload(amount=-1),
            _event_payload(mileage=-1),
        ]

        for payload in invalid_payloads:
            response = client.post(f"/events?user_id={user_id}", json=payload)
            assert response.status_code == 422
