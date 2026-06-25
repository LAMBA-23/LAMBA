import importlib
import os
import sys
from pathlib import Path

from fastapi.testclient import TestClient


TEST_DB_PATH = Path(__file__).resolve().parent / "test_vehicle.db"
if TEST_DB_PATH.exists():
    TEST_DB_PATH.unlink()

os.environ["DATABASE_URL"] = f"sqlite:///{TEST_DB_PATH.as_posix()}"

for module_name in ["app.main", "app.database", "app.models", "app.chat_parser"]:
    sys.modules.pop(module_name, None)

main_module = importlib.import_module("app.main")
database_module = importlib.import_module("app.database")
models_module = importlib.import_module("app.models")

database_module.Base.metadata.create_all(bind=database_module.engine)

client = TestClient(main_module.app)

Car = models_module.Car


def teardown_module() -> None:
    client.close()
    database_module.engine.dispose()
    if TEST_DB_PATH.exists():
        TEST_DB_PATH.unlink()


def _register_and_clean(username: str) -> int:
    user_id = client.post(
        "/auth/register",
        json={"username": username, "password": "password123"},
    ).json()["user_id"]
    session = database_module.SessionLocal()
    try:
        session.query(Car).delete()
        session.commit()
    finally:
        session.close()
    return user_id


def _register_user(username: str) -> int:
    return client.post(
        "/auth/register",
        json={"username": username, "password": "password123"},
    ).json()["user_id"]


class TestCreateVehicle:
    def test_create_vehicle_success(self) -> None:
        user_id = _register_and_clean("v1")
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

    def test_create_vehicle_updates_existing_default(self) -> None:
        user_id = _register_user("v2")
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

    def test_create_vehicle_user_not_found(self) -> None:
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

    def test_create_vehicle_empty_brand(self) -> None:
        user_id = _register_and_clean("v3")
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

    def test_create_vehicle_empty_model(self) -> None:
        user_id = _register_and_clean("v4")
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

    def test_create_vehicle_invalid_year(self) -> None:
        user_id = _register_and_clean("v5")
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

    def test_create_vehicle_negative_mileage(self) -> None:
        user_id = _register_and_clean("v6")
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

    def test_create_vehicle_whitespace_brand_rejected(self) -> None:
        user_id = _register_and_clean("v7")
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


class TestGetVehicle:
    def test_get_vehicle_returns_created(self) -> None:
        user_id = _register_and_clean("g1")
        client.post(
            "/vehicle",
            json={
                "user_id": user_id,
                "brand": "Toyota",
                "model": "Camry",
                "production_year": 2023,
                "current_mileage": 10000,
            },
        )
        response = client.get(f"/vehicle?user_id={user_id}")
        assert response.status_code == 200
        data = response.json()
        assert data["brand"] == "Toyota"
        assert data["model"] == "Camry"
