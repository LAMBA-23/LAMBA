from __future__ import annotations

import os
import tempfile
from pathlib import Path
from typing import Protocol


class PhotoStorage(Protocol):
    def save(self, key: str, content: bytes, content_type: str) -> None: ...

    def read(self, key: str) -> bytes: ...

    def delete(self, key: str) -> None: ...


class LocalPhotoStorage:
    def __init__(self, root: Path):
        self.root = root
        self.root.mkdir(parents=True, exist_ok=True)

    def _path(self, key: str) -> Path:
        filename = Path(key).name
        if not filename or filename != key:
            raise ValueError("Invalid photo storage key")
        return self.root / filename

    def save(self, key: str, content: bytes, content_type: str) -> None:
        del content_type
        destination = self._path(key)
        file_descriptor, temporary_name = tempfile.mkstemp(
            dir=self.root, prefix=".upload-", suffix=".tmp"
        )
        temporary_path = Path(temporary_name)
        try:
            with os.fdopen(file_descriptor, "wb") as temporary_file:
                temporary_file.write(content)
                temporary_file.flush()
                os.fsync(temporary_file.fileno())
            temporary_path.replace(destination)
        except Exception:
            temporary_path.unlink(missing_ok=True)
            raise

    def read(self, key: str) -> bytes:
        return self._path(key).read_bytes()

    def delete(self, key: str) -> None:
        self._path(key).unlink(missing_ok=True)


class S3PhotoStorage:
    def __init__(
        self,
        bucket: str,
        endpoint_url: str | None = None,
        region_name: str | None = None,
        prefix: str = "event-photos",
    ):
        if not bucket:
            raise RuntimeError("PHOTO_S3_BUCKET is required for S3 photo storage")

        import boto3

        self.bucket = bucket
        self.prefix = prefix.strip("/")
        self.client = boto3.client(
            "s3", endpoint_url=endpoint_url or None, region_name=region_name or None
        )

    def _object_key(self, key: str) -> str:
        filename = Path(key).name
        if not filename or filename != key:
            raise ValueError("Invalid photo storage key")
        return f"{self.prefix}/{filename}" if self.prefix else filename

    def save(self, key: str, content: bytes, content_type: str) -> None:
        self.client.put_object(
            Bucket=self.bucket,
            Key=self._object_key(key),
            Body=content,
            ContentType=content_type,
        )

    def read(self, key: str) -> bytes:
        from botocore.exceptions import ClientError

        try:
            response = self.client.get_object(
                Bucket=self.bucket, Key=self._object_key(key)
            )
        except ClientError as exc:
            error_code = exc.response.get("Error", {}).get("Code")
            if error_code in {"NoSuchKey", "404", "NotFound"}:
                raise FileNotFoundError(key) from exc
            raise
        return response["Body"].read()

    def delete(self, key: str) -> None:
        self.client.delete_object(Bucket=self.bucket, Key=self._object_key(key))


def create_photo_storage() -> PhotoStorage:
    backend = os.getenv("PHOTO_STORAGE_BACKEND", "local").strip().lower()
    if backend == "local":
        default_root = Path(__file__).resolve().parents[1] / "uploads" / "event_photos"
        root = Path(os.getenv("EVENT_PHOTO_DIR", str(default_root)))
        return LocalPhotoStorage(root)
    if backend == "s3":
        return S3PhotoStorage(
            bucket=os.getenv("PHOTO_S3_BUCKET", ""),
            endpoint_url=os.getenv("PHOTO_S3_ENDPOINT"),
            region_name=os.getenv("PHOTO_S3_REGION"),
            prefix=os.getenv("PHOTO_S3_PREFIX", "event-photos"),
        )
    raise RuntimeError("PHOTO_STORAGE_BACKEND must be 'local' or 's3'")
