from sqlalchemy import text


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
    def test_get_events_ignores_legacy_invalid_event_types(self, client, db_session):
        user_id = _register_user(client, "events-legacy-invalid")
        client.post(
            f"/events?user_id={user_id}",
            json=_event_payload(description="Visible fuel"),
        )
        car_id = client.get(f"/vehicle?user_id={user_id}").json()["id"]
        db_session.execute(
            text(
                "INSERT INTO events "
                "(car_id, type, description, amount, fuel_liters, mileage, created_at) "
                "VALUES (:car_id, 'condition', 'Legacy condition check', 0, 0, 0, "
                "CURRENT_TIMESTAMP)"
            ),
            {"car_id": car_id},
        )
        db_session.commit()

        response = client.get(f"/events?user_id={user_id}")

        assert response.status_code == 200
        assert [event["type"] for event in response.json()] == ["fuel"]

    def test_get_events_returns_only_user_events_newest_first(self, client):
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
            "First user repair",
            "First user fuel",
        ]
        assert [event["id"] for event in data] == sorted(
            (event["id"] for event in data),
            reverse=True,
        )
        assert "Second user fuel" not in [event["description"] for event in data]

    def test_get_events_orders_existing_records_by_created_at_desc(
        self, client, db_session
    ):
        user_id = _register_user(client, "events-existing-newest-first")
        car_id = client.get(f"/vehicle?user_id={user_id}").json()["id"]
        db_session.execute(
            text(
                "INSERT INTO events "
                "(car_id, type, description, amount, fuel_liters, mileage, created_at) "
                "VALUES "
                "(:car_id, 'fuel', 'Old fuel', 10, 5, 100, '2026-07-01 10:00:00'), "
                "(:car_id, 'repair', 'Newest repair', 20, 0, 200, '2026-07-03 10:00:00'), "
                "(:car_id, 'issue', 'Middle issue', 0, 0, 300, '2026-07-02 10:00:00')"
            ),
            {"car_id": car_id},
        )
        db_session.commit()

        response = client.get(f"/events?user_id={user_id}")

        assert response.status_code == 200
        data = response.json()
        descriptions = [event["description"] for event in data]
        assert len(data) == 3
        assert descriptions[0] == "Newest repair"
        assert descriptions[-1] == "Old fuel"
        assert descriptions == [
            "Newest repair",
            "Middle issue",
            "Old fuel",
        ]
        assert {event["description"] for event in data} == {
            "Old fuel",
            "Newest repair",
            "Middle issue",
        }

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
            json={"type": "fuel", "description": "Manual fuel record"},
        )
        timeline_response = client.get(f"/events?user_id={user_id}")

        assert vehicle_response.status_code == 201
        assert create_response.status_code == 200
        created_event = create_response.json()
        assert created_event["type"] == "fuel"
        assert created_event["description"] == "Manual fuel record"
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

    def test_post_fuel_event_accepts_decimal_liters(self, client):
        user_id = _register_user(client, "events-decimal-fuel-liters")

        create_response = client.post(
            f"/events?user_id={user_id}",
            json=_event_payload(
                description="Fuel with decimal liters",
                fuel_liters=35.7,
            ),
        )
        timeline_response = client.get(f"/events?user_id={user_id}")

        assert create_response.status_code == 200
        created_event = create_response.json()
        assert created_event["type"] == "fuel"
        assert created_event["fuel_liters"] == 35.7
        assert timeline_response.status_code == 200
        assert timeline_response.json() == [created_event]

    def test_post_fuel_event_accepts_decimal_liters_and_amount(self, client):
        user_id = _register_user(client, "events-decimal-fuel-cost")

        create_response = client.post(
            f"/events?user_id={user_id}",
            json=_event_payload(
                description="Fuel with decimal liters and cost",
                amount=2500.125,
                fuel_liters=35.75,
            ),
        )
        timeline_response = client.get(f"/events?user_id={user_id}")

        assert create_response.status_code == 200
        created_event = create_response.json()
        assert created_event["amount"] == 2500.125
        assert created_event["fuel_liters"] == 35.75
        assert timeline_response.status_code == 200
        assert timeline_response.json() == [created_event]

    def test_post_repair_event_accepts_decimal_amount(self, client):
        user_id = _register_user(client, "events-decimal-repair-cost")

        response = client.post(
            f"/events?user_id={user_id}",
            json=_event_payload(
                type="repair",
                description="Repair with decimal cost",
                amount=7000.5,
                mileage=0,
            ),
        )

        assert response.status_code == 200
        assert response.json()["amount"] == 7000.5

    def test_post_trip_event_accepts_decimal_distance(self, client):
        user_id = _register_user(client, "events-decimal-trip-distance")
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

        response = client.post(
            f"/events?user_id={user_id}",
            json={
                "type": "trip",
                "description": "Drove 100.5 km",
                "amount": 0,
                "mileage": 100.5,
            },
        )

        assert vehicle_response.status_code == 201
        assert response.status_code == 200
        assert response.json()["mileage"] == 125100.5

    def test_post_event_rejects_more_than_three_decimal_places(self, client):
        user_id = _register_user(client, "events-invalid-decimal-places")

        response = client.post(
            f"/events?user_id={user_id}",
            json=_event_payload(
                amount=12.3456,
                fuel_liters=10,
            ),
        )

        assert response.status_code == 422

    def test_post_event_supports_manual_form_event_types_and_fields(self, client):
        user_id = _register_user(client, "events-manual-form-types")

        created_events = []
        for event_type in ("fuel", "repair", "trip", "issue"):
            response = client.post(
                f"/events?user_id={user_id}",
                json=_event_payload(
                    type=event_type,
                    description=f"Manual {event_type}",
                    amount=100,
                    mileage=10,
                    fuel_liters=5,
                ),
            )

            assert response.status_code == 200
            created = response.json()
            assert created["type"] == event_type
            assert created["description"] == f"Manual {event_type}"
            assert created["amount"] == 100
            assert created["fuel_liters"] == 5
            created_events.append(created)

        timeline_response = client.get(f"/events?user_id={user_id}")

        assert timeline_response.status_code == 200
        assert timeline_response.json() == list(reversed(created_events))

    def test_post_repair_and_breakdown_records_are_saved_distinctly(self, client):
        user_id = _register_user(client, "events-repair-breakdown")

        repair_response = client.post(
            f"/events?user_id={user_id}",
            json={
                "type": "repair",
                "description": "Oil service",
                "amount": 7000,
                "mileage": 125000,
            },
        )
        issue_response = client.post(
            f"/events?user_id={user_id}",
            json={
                "type": "issue",
                "description": "Engine warning light",
                "mileage": 125050,
            },
        )
        timeline_response = client.get(f"/events?user_id={user_id}")

        assert repair_response.status_code == 200
        repair = repair_response.json()
        assert repair["type"] == "repair"
        assert repair["description"] == "Oil service"
        assert repair["amount"] == 7000
        assert repair["mileage"] == 125000

        assert issue_response.status_code == 200
        issue = issue_response.json()
        assert issue["type"] == "issue"
        assert issue["description"] == "Engine warning light"
        assert issue["amount"] == 0
        assert issue["mileage"] == 125050

        assert timeline_response.status_code == 200
        assert timeline_response.json() == [issue, repair]

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

    def test_post_trip_with_odometer_start_and_end_stores_trip_details(self, client):
        user_id = _register_user(client, "events-trip-odometer-range")
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
                "description": "Trip by odometer",
                "amount": 0,
                "odometer_start": 125000,
                "odometer_end": 125150,
            },
        )
        timeline_response = client.get(f"/events?user_id={user_id}")

        assert vehicle_response.status_code == 201
        assert create_response.status_code == 200
        created_event = create_response.json()
        assert created_event["type"] == "trip"
        assert created_event["mileage"] == 125150
        assert created_event["odometer_start"] == 125000
        assert created_event["odometer_end"] == 125150
        assert created_event["trip_distance"] == 150
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
        user_id = _register_user(client, "events-trip-after-issue")
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
        issue_note = client.post(
            f"/events?user_id={user_id}",
            json={
                "type": "issue",
                "description": "Issue note with stale mileage",
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
        assert issue_note.status_code == 200
        assert second_trip.status_code == 200
        assert first_trip.json()["mileage"] == 76100
        assert issue_note.json()["mileage"] == 61004
        assert second_trip.json()["mileage"] == 76200
        assert [event["mileage"] for event in timeline_response.json()] == [
            76200,
            61004,
            76100,
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
            _event_payload(type="condition"),
            _event_payload(description="   "),
            _event_payload(amount=-1),
            _event_payload(mileage=-1),
            _event_payload(fuel_liters=-1),
            _event_payload(
                type="trip",
                odometer_start=125150,
                odometer_end=125000,
            ),
            _event_payload(
                type="trip",
                odometer_start=125000,
                odometer_end=None,
            ),
            _event_payload(
                type="fuel",
                odometer_start=125000,
                odometer_end=125150,
            ),
        ]

        for payload in invalid_payloads:
            response = client.post(f"/events?user_id={user_id}", json=payload)
            assert response.status_code == 422

    def test_put_event_updates_existing_user_event(self, client):
        user_id = _register_user(client, "events-update")
        created_response = client.post(
            f"/events?user_id={user_id}",
            json=_event_payload(
                type="fuel",
                description="Manual fuel",
                amount=2500,
                mileage=0,
                fuel_liters=40,
            ),
        )
        event_id = created_response.json()["id"]

        update_response = client.put(
            f"/events/{event_id}?user_id={user_id}",
            json=_event_payload(
                type="repair",
                description="Updated repair",
                amount=7000,
                mileage=0,
                fuel_liters=0,
            ),
        )
        timeline_response = client.get(f"/events?user_id={user_id}")

        assert created_response.status_code == 200
        assert update_response.status_code == 200
        updated_event = update_response.json()
        assert updated_event["id"] == event_id
        assert updated_event["type"] == "repair"
        assert updated_event["description"] == "Updated repair"
        assert updated_event["amount"] == 7000
        assert updated_event["fuel_liters"] == 0
        assert timeline_response.status_code == 200
        assert timeline_response.json() == [updated_event]

    def test_delete_event_removes_existing_user_event(self, client):
        user_id = _register_user(client, "events-delete")
        created_response = client.post(
            f"/events?user_id={user_id}",
            json=_event_payload(description="Delete me"),
        )
        event_id = created_response.json()["id"]

        delete_response = client.delete(f"/events/{event_id}?user_id={user_id}")
        timeline_response = client.get(f"/events?user_id={user_id}")

        assert created_response.status_code == 200
        assert delete_response.status_code == 204
        assert timeline_response.status_code == 200
        assert timeline_response.json() == []

    def test_update_and_delete_event_return_404_for_other_user_event(self, client):
        owner_user_id = _register_user(client, "events-owner")
        other_user_id = _register_user(client, "events-other-user")
        created_response = client.post(
            f"/events?user_id={owner_user_id}",
            json=_event_payload(description="Owned event"),
        )
        event_id = created_response.json()["id"]

        update_response = client.put(
            f"/events/{event_id}?user_id={other_user_id}",
            json=_event_payload(description="Wrong user update"),
        )
        delete_response = client.delete(f"/events/{event_id}?user_id={other_user_id}")
        owner_timeline_response = client.get(f"/events?user_id={owner_user_id}")

        assert created_response.status_code == 200
        assert update_response.status_code == 404
        assert delete_response.status_code == 404
        assert owner_timeline_response.status_code == 200
        assert owner_timeline_response.json()[0]["description"] == "Owned event"
