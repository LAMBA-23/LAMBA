import gc
import importlib
import os
import tempfile
import time
import warnings
from pathlib import Path
from uuid import uuid4

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import close_all_sessions, sessionmaker
from sqlalchemy.pool import NullPool


TEST_DB_DIR = Path(tempfile.gettempdir()) / "lamba-pytest"
TEST_DB_DIR.mkdir(parents=True, exist_ok=True)
TEST_DB_PATH = TEST_DB_DIR / f"test-{os.getpid()}-{uuid4().hex}.db"
TEST_DATABASE_URL = f"sqlite:///{TEST_DB_PATH.as_posix()}"
TEST_PHOTO_DIR = TEST_DB_DIR / f"photos-{os.getpid()}-{uuid4().hex}"

os.environ["DATABASE_URL"] = TEST_DATABASE_URL
os.environ["PHOTO_STORAGE_BACKEND"] = "local"
os.environ["EVENT_PHOTO_DIR"] = str(TEST_PHOTO_DIR)

database = importlib.import_module("app.database")
Base = database.Base
get_db = database.get_db
Car = importlib.import_module("app.models").Car

test_engine = create_engine(
    TEST_DATABASE_URL,
    connect_args={"check_same_thread": False},
    poolclass=NullPool,
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=test_engine)

database.engine = test_engine

app = importlib.import_module("app.main").app


@pytest.fixture(scope="function", autouse=True)
def setup_db():
    Base.metadata.create_all(bind=test_engine)
    yield
    Base.metadata.drop_all(bind=test_engine)


@pytest.fixture(scope="function", autouse=True)
def reset_rate_limiter():
    importlib.import_module("app.main").rate_limiter.reset()
    yield


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
    close_all_sessions()
    test_engine.dispose()
    gc.collect()
    _unlink_sqlite_artifacts_with_retries(TEST_DB_PATH)
    for photo_file in TEST_PHOTO_DIR.glob("*"):
        photo_file.unlink(missing_ok=True)
    TEST_PHOTO_DIR.rmdir()


def _unlink_sqlite_artifacts_with_retries(
    path: Path, retries: int = 10, delay_seconds: float = 0.1
):
    locked_paths: list[Path] = []
    for candidate in _sqlite_artifact_paths(path):
        if _unlink_with_retries(
            candidate, retries=retries, delay_seconds=delay_seconds
        ):
            continue
        locked_paths.append(candidate)

    if locked_paths:
        warnings.warn(
            "Could not remove temporary SQLite files during pytest cleanup: "
            + ", ".join(str(candidate) for candidate in locked_paths)
            + ". They will be retried on a later run.",
            RuntimeWarning,
        )


def _unlink_with_retries(
    path: Path, retries: int = 10, delay_seconds: float = 0.1
) -> bool:
    for attempt in range(retries):
        try:
            path.unlink(missing_ok=True)
            return True
        except PermissionError:
            if attempt == retries - 1:
                return False
            time.sleep(delay_seconds)
    return False


def _sqlite_artifact_paths(path: Path) -> list[Path]:
    return [
        path,
        path.with_name(f"{path.name}-shm"),
        path.with_name(f"{path.name}-wal"),
    ]


def _cleanup_stale_test_dbs(directory: Path, max_age_seconds: int) -> None:
    now = time.time()
    for candidate in directory.glob("test-*.db*"):
        if now - candidate.stat().st_mtime < max_age_seconds:
            continue
        _unlink_with_retries(candidate, retries=3, delay_seconds=0.05)


_cleanup_stale_test_dbs(TEST_DB_DIR, max_age_seconds=3600)
