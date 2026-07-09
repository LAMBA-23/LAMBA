class TestLogin:
    def test_register_stores_password_hashed(self, client, db_session):
        response = client.post(
            "/auth/register",
            json={"username": "hash-user", "password": "password123"},
        )
        assert response.status_code == 201

        from app.models import User

        user = db_session.query(User).filter(User.username == "hash-user").one()
        assert user.password != "password123"
        assert user.password.startswith("pbkdf2_sha256$")

    def test_login_success(self, client):
        register_response = client.post(
            "/auth/register",
            json={"username": "login-user", "password": "password123"},
        )
        user_id = register_response.json()["user_id"]

        response = client.post(
            "/auth/login", json={"username": "login-user", "password": "password123"}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["user_id"] == user_id

    def test_removed_demo_credentials_fail(self, client):
        response = client.post(
            "/auth/login", json={"username": "demo", "password": "demo"}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is False

    def test_login_wrong_password(self, client):
        client.post(
            "/auth/register",
            json={"username": "wrong-password-user", "password": "password123"},
        )

        response = client.post(
            "/auth/login",
            json={"username": "wrong-password-user", "password": "wrong"},
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is False

    def test_login_is_rate_limited(self, client):
        for _ in range(5):
            response = client.post(
                "/auth/login",
                json={"username": "nonexistent", "password": "pass"},
                headers={"X-Forwarded-For": "203.0.113.10"},
            )
            assert response.status_code == 200
            assert response.json()["success"] is False

        rate_limited_response = client.post(
            "/auth/login",
            json={"username": "nonexistent", "password": "pass"},
            headers={"X-Forwarded-For": "203.0.113.10"},
        )
        assert rate_limited_response.status_code == 429
        assert rate_limited_response.json()["detail"] == "Too many login attempts"

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
