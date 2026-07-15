from io import BytesIO
from pathlib import Path

import pytest
from PIL import Image

from app import main as main_module
from app import photo_processing
from app.models import Event
from app.photo_storage import LocalPhotoStorage


def _image_bytes(image_format: str, size: tuple[int, int] = (80, 60)) -> bytes:
    output = BytesIO()
    Image.new("RGB", size, "red").save(output, format=image_format)
    return output.getvalue()


PNG_BYTES = _image_bytes("PNG")
JPEG_BYTES = _image_bytes("JPEG")
WEBP_BYTES = _image_bytes("WEBP")
LOCAL_STORAGE = main_module.photo_storage


@pytest.fixture(autouse=True)
def clean_photo_dir():
    assert isinstance(LOCAL_STORAGE, LocalPhotoStorage)
    LOCAL_STORAGE.root.mkdir(parents=True, exist_ok=True)
    for photo_file in _photo_files():
        photo_file.unlink(missing_ok=True)
    yield
    for photo_file in _photo_files():
        photo_file.unlink(missing_ok=True)


def _register_user(client, username: str) -> int:
    response = client.post(
        "/auth/register",
        json={"username": username, "password": "password123"},
    )
    assert response.status_code == 201
    return response.json()["user_id"]


def _create_issue(client, user_id: int) -> int:
    response = client.post(
        f"/events?user_id={user_id}",
        json={
            "type": "issue",
            "description": "Breakdown 2026-07-06: Check engine",
            "amount": 0,
            "mileage": 0,
        },
    )
    assert response.status_code == 200
    return response.json()["id"]


def _photo_files() -> list[Path]:
    return list(LOCAL_STORAGE.root.glob("*"))


def _patch_route_storage(client, monkeypatch, storage) -> None:
    upload_route = next(
        route
        for route in client.app.routes
        if getattr(route, "path", None) == "/events/{event_id}/photo"
        and "POST" in getattr(route, "methods", set())
    )
    monkeypatch.setitem(upload_route.endpoint.__globals__, "photo_storage", storage)


def test_upload_photo_returns_metadata_and_url(client):
    user_id = _register_user(client, "photo-upload")
    event_id = _create_issue(client, user_id)

    response = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("issue.png", PNG_BYTES, "image/png")},
    )

    assert response.status_code == 200
    body = response.json()
    assert body["photo_url"] == f"/events/{event_id}/photo?user_id={user_id}"
    assert body["photo_thumbnail_url"] == (
        f"/events/{event_id}/photo/thumbnail?user_id={user_id}"
    )
    assert body["photo_mime_type"] == "image/png"
    assert body["photo_size"] > 0
    assert body["photo_width"] == 80
    assert body["photo_height"] == 60
    assert len(_photo_files()) == 2


def test_upload_replaces_existing_photo(client):
    user_id = _register_user(client, "photo-replace")
    event_id = _create_issue(client, user_id)
    first = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("first.png", PNG_BYTES, "image/png")},
    ).json()

    second_response = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("second.jpg", JPEG_BYTES, "image/jpeg")},
    )

    assert second_response.status_code == 200
    second = second_response.json()
    assert second["photo_url"] == first["photo_url"]
    assert second["photo_mime_type"] == "image/jpeg"
    assert len(_photo_files()) == 2


def test_delete_photo_clears_metadata_and_file(client):
    user_id = _register_user(client, "photo-delete")
    event_id = _create_issue(client, user_id)
    uploaded = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("issue.png", PNG_BYTES, "image/png")},
    ).json()

    response = client.delete(f"/events/{event_id}/photo?user_id={user_id}")
    event_response = client.get(f"/events?user_id={user_id}")

    assert response.status_code == 204
    assert event_response.json()[0]["photo_url"] is None
    assert event_response.json()[0]["photo_mime_type"] is None
    assert event_response.json()[0]["photo_size"] is None
    assert uploaded["photo_url"] is not None
    assert _photo_files() == []


def test_delete_event_removes_photo_file(client):
    user_id = _register_user(client, "photo-event-delete")
    event_id = _create_issue(client, user_id)
    uploaded = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("issue.png", PNG_BYTES, "image/png")},
    ).json()

    response = client.delete(f"/events/{event_id}?user_id={user_id}")

    assert response.status_code == 204
    assert uploaded["photo_url"] is not None
    assert _photo_files() == []


def test_photo_upload_rejects_invalid_mime_and_content(client):
    user_id = _register_user(client, "photo-invalid")
    event_id = _create_issue(client, user_id)

    invalid_mime = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("issue.gif", b"GIF89a", "image/gif")},
    )
    invalid_content = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("issue.png", b"not a png", "image/png")},
    )

    assert invalid_mime.status_code == 415
    assert invalid_content.status_code == 400


def test_photo_upload_rejects_oversized_image(client):
    user_id = _register_user(client, "photo-large")
    event_id = _create_issue(client, user_id)
    oversized = b"\x89PNG\r\n\x1a\n" + b"0" * main_module.EVENT_PHOTO_MAX_BYTES

    response = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("issue.png", oversized, "image/png")},
    )

    assert response.status_code == 413


def test_photo_upload_checks_ownership(client):
    owner_id = _register_user(client, "photo-owner")
    other_id = _register_user(client, "photo-other")
    event_id = _create_issue(client, owner_id)

    response = client.post(
        f"/events/{event_id}/photo?user_id={other_id}",
        files={"file": ("issue.png", PNG_BYTES, "image/png")},
    )

    assert response.status_code == 404


def test_old_events_return_null_photo_fields(client):
    user_id = _register_user(client, "photo-compat")
    _create_issue(client, user_id)

    response = client.get(f"/events?user_id={user_id}")

    assert response.status_code == 200
    event = response.json()[0]
    assert event["photo_url"] is None
    assert event["photo_mime_type"] is None
    assert event["photo_size"] is None


def test_upload_photo_rejects_non_issue_event(client):
    user_id = _register_user(client, "photo-non-issue")
    fuel_response = client.post(
        f"/events?user_id={user_id}",
        json={
            "type": "fuel",
            "description": "Fuel 2026-07-06",
            "amount": 1000,
            "fuel_liters": 20,
            "mileage": 100,
        },
    )
    event_id = fuel_response.json()["id"]

    response = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("issue.png", PNG_BYTES, "image/png")},
    )

    assert response.status_code == 400


def test_get_photo_and_thumbnail_returns_private_image_responses(client):
    user_id = _register_user(client, "photo-get")
    event_id = _create_issue(client, user_id)
    large_png = _image_bytes("PNG", (1000, 800))
    uploaded = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("large.png", large_png, "image/png")},
    ).json()

    original = client.get(uploaded["photo_url"])
    thumbnail = client.get(uploaded["photo_thumbnail_url"])

    assert original.status_code == 200
    assert original.headers["content-type"] == "image/png"
    assert original.headers["cache-control"].startswith("private")
    assert original.headers["x-content-type-options"] == "nosniff"
    assert Image.open(BytesIO(original.content)).size == (1000, 800)
    assert thumbnail.status_code == 200
    assert Image.open(BytesIO(thumbnail.content)).size == (512, 410)


def test_photo_routes_reject_another_user(client):
    owner_id = _register_user(client, "photo-private-owner")
    other_id = _register_user(client, "photo-private-other")
    event_id = _create_issue(client, owner_id)
    client.post(
        f"/events/{event_id}/photo?user_id={owner_id}",
        files={"file": ("issue.webp", WEBP_BYTES, "image/webp")},
    )

    assert client.get(f"/events/{event_id}/photo?user_id={other_id}").status_code == 404
    assert (
        client.get(f"/events/{event_id}/photo/thumbnail?user_id={other_id}").status_code
        == 404
    )
    assert (
        client.delete(f"/events/{event_id}/photo?user_id={other_id}").status_code == 404
    )


def test_upload_webp_and_strip_exif_from_oriented_jpeg(client):
    user_id = _register_user(client, "photo-formats")
    event_id = _create_issue(client, user_id)

    webp_response = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("issue.webp", WEBP_BYTES, "image/webp")},
    )
    assert webp_response.status_code == 200
    assert webp_response.json()["photo_mime_type"] == "image/webp"

    exif = Image.Exif()
    exif[274] = 6
    exif[270] = "private metadata"
    output = BytesIO()
    Image.new("RGB", (40, 20), "blue").save(output, format="JPEG", exif=exif)
    jpeg_response = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("oriented.jpg", output.getvalue(), "image/jpeg")},
    )

    assert jpeg_response.status_code == 200
    assert jpeg_response.json()["photo_width"] == 20
    assert jpeg_response.json()["photo_height"] == 40
    stored = client.get(jpeg_response.json()["photo_url"])
    with Image.open(BytesIO(stored.content)) as normalized:
        assert normalized.size == (20, 40)
        assert len(normalized.getexif()) == 0


def test_photo_upload_rejects_mime_mismatch_and_excessive_dimensions(
    client, monkeypatch
):
    user_id = _register_user(client, "photo-validation")
    event_id = _create_issue(client, user_id)

    mismatch = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("wrong.png", JPEG_BYTES, "image/png")},
    )
    monkeypatch.setattr(photo_processing, "MAX_IMAGE_SIDE", 50)
    excessive = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("large.png", PNG_BYTES, "image/png")},
    )

    assert mismatch.status_code == 400
    assert excessive.status_code == 400
    assert _photo_files() == []


class _FailSecondSaveStorage:
    def __init__(self, delegate: LocalPhotoStorage):
        self.delegate = delegate
        self.save_calls = 0

    def save(self, key: str, content: bytes, content_type: str) -> None:
        self.save_calls += 1
        if self.save_calls == 2:
            raise OSError("simulated storage failure")
        self.delegate.save(key, content, content_type)

    def read(self, key: str) -> bytes:
        return self.delegate.read(key)

    def delete(self, key: str) -> None:
        self.delegate.delete(key)


class _FailDeleteStorage:
    def __init__(self, delegate: LocalPhotoStorage):
        self.delegate = delegate

    def save(self, key: str, content: bytes, content_type: str) -> None:
        self.delegate.save(key, content, content_type)

    def read(self, key: str) -> bytes:
        return self.delegate.read(key)

    def delete(self, key: str) -> None:
        raise OSError("simulated cleanup failure")


def test_failed_replacement_keeps_previous_photo(client, monkeypatch):
    user_id = _register_user(client, "photo-replace-failure")
    event_id = _create_issue(client, user_id)
    uploaded = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("first.png", PNG_BYTES, "image/png")},
    ).json()
    previous_files = {path.name for path in _photo_files()}
    _patch_route_storage(client, monkeypatch, _FailSecondSaveStorage(LOCAL_STORAGE))

    replacement = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("second.jpg", JPEG_BYTES, "image/jpeg")},
    )

    assert replacement.status_code == 503
    assert {path.name for path in _photo_files()} == previous_files
    assert client.get(uploaded["photo_url"]).status_code == 200


def test_database_failure_removes_new_photo_files(client, db_session, monkeypatch):
    user_id = _register_user(client, "photo-database-failure")
    event_id = _create_issue(client, user_id)

    with monkeypatch.context() as context:
        context.setattr(
            db_session, "commit", lambda: (_ for _ in ()).throw(RuntimeError("db"))
        )
        response = client.post(
            f"/events/{event_id}/photo?user_id={user_id}",
            files={"file": ("issue.png", PNG_BYTES, "image/png")},
        )

    assert response.status_code == 500
    assert _photo_files() == []
    event = db_session.get(Event, event_id)
    db_session.refresh(event)
    assert event.photo_path is None


def test_refresh_failure_after_commit_keeps_committed_photo(
    client, db_session, monkeypatch
):
    user_id = _register_user(client, "photo-refresh-failure")
    event_id = _create_issue(client, user_id)

    with monkeypatch.context() as context:
        context.setattr(
            db_session,
            "refresh",
            lambda instance: (_ for _ in ()).throw(RuntimeError("refresh")),
        )
        response = client.post(
            f"/events/{event_id}/photo?user_id={user_id}",
            files={"file": ("issue.png", PNG_BYTES, "image/png")},
        )

    assert response.status_code == 500
    assert len(_photo_files()) == 2
    db_session.expire_all()
    event = db_session.get(Event, event_id)
    assert event.photo_path is not None
    assert event.photo_thumbnail_path is not None


def test_cleanup_failure_does_not_undo_photo_metadata_deletion(client, monkeypatch):
    user_id = _register_user(client, "photo-cleanup-failure")
    event_id = _create_issue(client, user_id)
    client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("issue.png", PNG_BYTES, "image/png")},
    )
    _patch_route_storage(client, monkeypatch, _FailDeleteStorage(LOCAL_STORAGE))

    response = client.delete(f"/events/{event_id}/photo?user_id={user_id}")
    events = client.get(f"/events?user_id={user_id}").json()

    assert response.status_code == 204
    assert events[0]["photo_url"] is None


def test_changing_issue_to_another_event_type_removes_photo(client):
    user_id = _register_user(client, "photo-type-change")
    event_id = _create_issue(client, user_id)
    client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("issue.png", PNG_BYTES, "image/png")},
    )

    response = client.put(
        f"/events/{event_id}?user_id={user_id}",
        json={
            "type": "repair",
            "description": "Issue repaired",
            "amount": 500,
            "mileage": 100,
        },
    )

    assert response.status_code == 200
    assert response.json()["photo_url"] is None
    assert _photo_files() == []
