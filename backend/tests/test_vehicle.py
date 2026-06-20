import pytest


class TestCreateVehicle:
    def test_create_vehicle_success(self, client, demo_user):
        user_id = demo_user["user_id"]
        response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "Toyota",
                "model": "Camry",
                "production_year": 2023,
                "current_mileage": 10000,
            },
        )
        assert response.status_code == 201
        data = response.json()
        assert data["brand"] == "Toyota"
        assert data["model"] == "Camry"
        assert data["production_year"] == 2023
        assert data["current_mileage"] == 10000
        assert "id" in data
        assert "created_at" in data

    def test_create_vehicle_duplicate_prevention(self, client, demo_user):
        user_id = demo_user["user_id"]
        vehicle_data = {
            "user_id": user_id,
            "brand": "Toyota",
            "model": "Camry",
            "production_year": 2023,
            "current_mileage": 10000,
        }
        response1 = client.post("/vehicle", json=vehicle_data)
        assert response1.status_code == 201

        response2 = client.post("/vehicle", json=vehicle_data)
        assert response2.status_code == 409
        assert "already has a vehicle" in response2.json()["detail"]

    def test_create_vehicle_user_not_found(self, client):
        response = client.post(
            "/vehicle",
            json={
                "user_id": 99999,
                "brand": "Toyota",
                "model": "Camry",
                "production_year": 2023,
                "current_mileage": 10000,
            },
        )
        assert response.status_code == 404
        assert "User not found" in response.json()["detail"]

    def test_create_vehicle_empty_brand(self, client, demo_user):
        user_id = demo_user["user_id"]
        response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "",
                "model": "Camry",
                "production_year": 2023,
                "current_mileage": 10000,
            },
        )
        assert response.status_code == 422

    def test_create_vehicle_empty_model(self, client, demo_user):
        user_id = demo_user["user_id"]
        response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "Toyota",
                "model": "",
                "production_year": 2023,
                "current_mileage": 10000,
            },
        )
        assert response.status_code == 422

    def test_create_vehicle_invalid_year_too_low(self, client, demo_user):
        user_id = demo_user["user_id"]
        response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "Toyota",
                "model": "Camry",
                "production_year": 1800,
                "current_mileage": 10000,
            },
        )
        assert response.status_code == 422

    def test_create_vehicle_invalid_year_too_high(self, client, demo_user):
        user_id = demo_user["user_id"]
        response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "Toyota",
                "model": "Camry",
                "production_year": 2200,
                "current_mileage": 10000,
            },
        )
        assert response.status_code == 422

    def test_create_vehicle_negative_mileage(self, client, demo_user):
        user_id = demo_user["user_id"]
        response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "Toyota",
                "model": "Camry",
                "production_year": 2023,
                "current_mileage": -100,
            },
        )
        assert response.status_code == 422

    def test_create_vehicle_whitespace_brand_rejected(self, client, demo_user):
        user_id = demo_user["user_id"]
        response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "   ",
                "model": "Camry",
                "production_year": 2023,
                "current_mileage": 10000,
            },
        )
        assert response.status_code == 422

    def test_create_vehicle_missing_fields(self, client, demo_user):
        response = client.post("/vehicle", json={})
        assert response.status_code == 422


class TestGetVehicle:
    def test_get_vehicle_success(self, client, demo_user):
        user_id = demo_user["user_id"]
        create_response = client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "Toyota",
                "model": "Camry",
                "production_year": 2023,
                "current_mileage": 10000,
            },
        )
        assert create_response.status_code == 201

        response = client.get(f"/vehicle/{user_id}")
        assert response.status_code == 200
        data = response.json()
        assert data["brand"] == "Toyota"
        assert data["model"] == "Camry"
        assert data["production_year"] == 2023
        assert data["current_mileage"] == 10000

    def test_get_vehicle_not_found(self, client):
        response = client.get("/vehicle/99999")
        assert response.status_code == 404
        assert "Vehicle not found" in response.json()["detail"]
