import os
import tempfile
from pathlib import Path
from uuid import uuid4


TEST_DB_DIR = Path(tempfile.gettempdir()) / "lamba-pytest"
TEST_DB_DIR.mkdir(parents=True, exist_ok=True)
TEST_DB_PATH = TEST_DB_DIR / f"test-{os.getpid()}-{uuid4().hex}.db"
TEST_DATABASE_URL = f"sqlite:///{TEST_DB_PATH.as_posix()}"

os.environ["DATABASE_URL"] = TEST_DATABASE_URL

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from app.database import Base, get_db
from app import database
from app.models import Car

test_engine = create_engine(
    TEST_DATABASE_URL, connect_args={"check_same_thread": False}
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=test_engine)

database.engine = test_engine

from app.main import app  # noqa: E402


@pytest.fixture(scope="function", autouse=True)
def setup_db():
    Base.metadata.create_all(bind=test_engine)
    yield
    Base.metadata.drop_all(bind=test_engine)


@pytest.fixture(scope="function")
def db_session():
    session = TestingSessionLocal()
    try:
        yield session
    finally:
        session.close()


@pytest.fixture(scope="function")
def client(db_session):
    def override_get_db():
        try:
            yield db_session
        finally:
            pass

    app.dependency_overrides[get_db] = override_get_db
    with TestClient(app, raise_server_exceptions=False) as c:
        db_session.query(Car).delete()
        db_session.commit()
        yield c
    app.dependency_overrides.clear()


@pytest.fixture
def demo_user(client):
    response = client.post("/auth/login", json={"username": "demo", "password": "demo"})
    return response.json()


def pytest_sessionfinish(session, exitstatus):
    test_engine.dispose()
    TEST_DB_PATH.unlink(missing_ok=True)
