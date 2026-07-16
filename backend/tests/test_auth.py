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


class TestChangePassword:
    def test_change_password_rejects_incorrect_current_password(
        self, client, db_session
    ):
        user_id = client.post(
            "/auth/register",
            json={"username": "change-password-wrong", "password": "password123"},
        ).json()["user_id"]

        response = client.post(
            f"/auth/change-password?user_id={user_id}",
            json={
                "current_password": "incorrect-password",
                "new_password": "new-password123",
                "new_password_confirmation": "new-password123",
            },
        )

        assert response.status_code == 400
        assert (
            client.post(
                "/auth/login",
                json={"username": "change-password-wrong", "password": "password123"},
            ).json()["success"]
            is True
        )

    def test_change_password_hashes_and_accepts_new_password(self, client, db_session):
        user_id = client.post(
            "/auth/register",
            json={"username": "change-password-success", "password": "password123"},
        ).json()["user_id"]

        response = client.post(
            f"/auth/change-password?user_id={user_id}",
            json={
                "current_password": "password123",
                "new_password": "new-password123",
                "new_password_confirmation": "new-password123",
            },
        )

        assert response.status_code == 204
        assert (
            client.post(
                "/auth/login",
                json={"username": "change-password-success", "password": "password123"},
                headers={"X-Forwarded-For": "203.0.113.99"},
            ).json()["success"]
            is False
        )
        assert (
            client.post(
                "/auth/login",
                json={
                    "username": "change-password-success",
                    "password": "new-password123",
                },
                headers={"X-Forwarded-For": "203.0.113.99"},
            ).json()["success"]
            is True
        )

    def test_change_password_accepts_same_password(self, client):
        user_id = client.post(
            "/auth/register",
            json={"username": "change-password-same", "password": "password123"},
        ).json()["user_id"]

        response = client.post(
            f"/auth/change-password?user_id={user_id}",
            json={
                "current_password": "password123",
                "new_password": "password123",
                "new_password_confirmation": "password123",
            },
        )

        assert response.status_code == 204

    def test_change_password_validates_length_and_confirmation(self, client):
        user_id = client.post(
            "/auth/register",
            json={"username": "change-password-validation", "password": "password123"},
        ).json()["user_id"]

        short_password_response = client.post(
            f"/auth/change-password?user_id={user_id}",
            json={
                "current_password": "password123",
                "new_password": "short",
                "new_password_confirmation": "short",
            },
        )
        mismatch_response = client.post(
            f"/auth/change-password?user_id={user_id}",
            json={
                "current_password": "password123",
                "new_password": "new-password123",
                "new_password_confirmation": "different-password123",
            },
        )

        assert short_password_response.status_code == 422
        assert mismatch_response.status_code == 422


class TestHealth:
    def test_health_check(self, client):
        response = client.get("/health")
        assert response.status_code == 200
        assert response.json() == {"status": "ok"}
