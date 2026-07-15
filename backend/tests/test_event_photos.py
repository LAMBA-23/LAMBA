from pathlib import Path

import pytest

from app import main as main_module


PNG_BYTES = (
    b"\x89PNG\r\n\x1a\n"
    b"\x00\x00\x00\rIHDR"
    b"\x00\x00\x00\x01\x00\x00\x00\x01"
    b"\x08\x02\x00\x00\x00\x90wS\xde"
    b"\x00\x00\x00\x00IEND\xaeB`\x82"
)
JPEG_BYTES = b"\xff\xd8\xff\xe0\x00\x10JFIF\x00\x01\x02\xff\xd9"


@pytest.fixture(autouse=True)
def clean_photo_dir():
    main_module.EVENT_PHOTO_DIR.mkdir(parents=True, exist_ok=True)
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
    return list(main_module.EVENT_PHOTO_DIR.glob("*"))


def test_upload_photo_returns_metadata_and_url(client):
    user_id = _register_user(client, "photo-upload")
    event_id = _create_issue(client, user_id)

    response = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("issue.png", PNG_BYTES, "image/png")},
    )

    assert response.status_code == 200
    body = response.json()
    assert body["photo_url"].startswith("/uploads/event_photos/")
    assert body["photo_mime_type"] == "image/png"
    assert body["photo_size"] == len(PNG_BYTES)
    assert (main_module.EVENT_PHOTO_DIR / Path(body["photo_url"]).name).is_file()


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
    assert second["photo_url"] != first["photo_url"]
    assert not (main_module.EVENT_PHOTO_DIR / Path(first["photo_url"]).name).exists()
    assert (main_module.EVENT_PHOTO_DIR / Path(second["photo_url"]).name).is_file()


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
    assert not (main_module.EVENT_PHOTO_DIR / Path(uploaded["photo_url"]).name).exists()


def test_delete_event_removes_photo_file(client):
    user_id = _register_user(client, "photo-event-delete")
    event_id = _create_issue(client, user_id)
    uploaded = client.post(
        f"/events/{event_id}/photo?user_id={user_id}",
        files={"file": ("issue.png", PNG_BYTES, "image/png")},
    ).json()

    response = client.delete(f"/events/{event_id}?user_id={user_id}")

    assert response.status_code == 204
    assert not (main_module.EVENT_PHOTO_DIR / Path(uploaded["photo_url"]).name).exists()


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
