from io import BytesIO

import pytest
from botocore.exceptions import ClientError

from app.photo_storage import LocalPhotoStorage, S3PhotoStorage


def test_local_storage_saves_reads_and_deletes_atomically(tmp_path):
    storage = LocalPhotoStorage(tmp_path)

    storage.save("photo.jpg", b"content", "image/jpeg")

    assert storage.read("photo.jpg") == b"content"
    assert list(tmp_path.glob(".upload-*.tmp")) == []
    storage.delete("photo.jpg")
    assert not (tmp_path / "photo.jpg").exists()


@pytest.mark.parametrize("key", ["../photo.jpg", "folder/photo.jpg", ""])
def test_local_storage_rejects_unsafe_keys(tmp_path, key):
    storage = LocalPhotoStorage(tmp_path)

    with pytest.raises(ValueError):
        storage.save(key, b"content", "image/jpeg")


class _FakeS3Client:
    def __init__(self):
        self.objects = {}

    def put_object(self, **kwargs):
        self.objects[(kwargs["Bucket"], kwargs["Key"])] = (
            kwargs["Body"],
            kwargs["ContentType"],
        )

    def get_object(self, **kwargs):
        try:
            content, _ = self.objects[(kwargs["Bucket"], kwargs["Key"])]
        except KeyError as exc:
            raise ClientError(
                {"Error": {"Code": "NoSuchKey", "Message": "missing"}},
                "GetObject",
            ) from exc
        return {"Body": BytesIO(content)}

    def delete_object(self, **kwargs):
        self.objects.pop((kwargs["Bucket"], kwargs["Key"]), None)


def test_s3_storage_uses_private_object_keys(monkeypatch):
    client = _FakeS3Client()
    monkeypatch.setattr("boto3.client", lambda *args, **kwargs: client)
    storage = S3PhotoStorage(
        bucket="photos",
        endpoint_url="https://s3.example.test",
        region_name="ru-1",
        prefix="private/events",
    )

    storage.save("photo.webp", b"image", "image/webp")

    assert client.objects[("photos", "private/events/photo.webp")] == (
        b"image",
        "image/webp",
    )
    assert storage.read("photo.webp") == b"image"
    storage.delete("photo.webp")
    with pytest.raises(FileNotFoundError):
        storage.read("photo.webp")
