import pytest


class TestLogin:
    def test_login_success(self, client):
        response = client.post(
            "/auth/login", json={"username": "demo", "password": "demo"}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "user_id" in data

    def test_login_compatible_password(self, client):
        response = client.post(
            "/auth/login", json={"username": "demo", "password": "password"}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True

    def test_login_wrong_password(self, client):
        response = client.post(
            "/auth/login", json={"username": "demo", "password": "wrong"}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is False

    def test_login_nonexistent_user(self, client):
        response = client.post(
            "/auth/login", json={"username": "nonexistent", "password": "pass"}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is False


class TestHealth:
    def test_health_check(self, client):
        response = client.get("/health")
        assert response.status_code == 200
        assert response.json() == {"status": "ok"}
