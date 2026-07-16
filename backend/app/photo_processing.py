from __future__ import annotations

from dataclasses import dataclass
from io import BytesIO

from PIL import Image, ImageOps, UnidentifiedImageError


SUPPORTED_MIME_FORMATS = {
    "image/jpeg": ("JPEG", ".jpg"),
    "image/png": ("PNG", ".png"),
    "image/webp": ("WEBP", ".webp"),
}
FORMAT_MIME_TYPES = {value[0]: key for key, value in SUPPORTED_MIME_FORMATS.items()}
MAX_IMAGE_PIXELS = 40_000_000
MAX_IMAGE_SIDE = 8_000
THUMBNAIL_MAX_SIDE = 512


class InvalidPhotoError(ValueError):
    pass


@dataclass(frozen=True)
class ProcessedPhoto:
    content: bytes
    thumbnail: bytes
    mime_type: str
    extension: str
    width: int
    height: int


def process_photo(content: bytes, declared_mime_type: str) -> ProcessedPhoto:
    if declared_mime_type not in SUPPORTED_MIME_FORMATS:
        raise InvalidPhotoError("Unsupported image MIME type")

    try:
        with Image.open(BytesIO(content)) as source:
            detected_mime_type = FORMAT_MIME_TYPES.get(source.format or "")
            if detected_mime_type != declared_mime_type:
                raise InvalidPhotoError("Image content does not match its MIME type")
            source.verify()

        with Image.open(BytesIO(content)) as source:
            width, height = source.size
            if (
                width <= 0
                or height <= 0
                or width > MAX_IMAGE_SIDE
                or height > MAX_IMAGE_SIDE
                or width * height > MAX_IMAGE_PIXELS
            ):
                raise InvalidPhotoError("Image dimensions are too large")

            normalized = ImageOps.exif_transpose(source)
            normalized.load()
            normalized = _compatible_mode(normalized, declared_mime_type)
            width, height = normalized.size
            encoded = _encode(normalized, declared_mime_type)

            thumbnail = normalized.copy()
            thumbnail.thumbnail(
                (THUMBNAIL_MAX_SIDE, THUMBNAIL_MAX_SIDE), Image.Resampling.LANCZOS
            )
            encoded_thumbnail = _encode(thumbnail, declared_mime_type)
    except InvalidPhotoError:
        raise
    except (
        Image.DecompressionBombError,
        UnidentifiedImageError,
        OSError,
        ValueError,
    ) as exc:
        raise InvalidPhotoError("Invalid or corrupt image") from exc

    return ProcessedPhoto(
        content=encoded,
        thumbnail=encoded_thumbnail,
        mime_type=declared_mime_type,
        extension=SUPPORTED_MIME_FORMATS[declared_mime_type][1],
        width=width,
        height=height,
    )


def _compatible_mode(image: Image.Image, mime_type: str) -> Image.Image:
    if mime_type == "image/jpeg":
        if image.mode in {"RGBA", "LA"}:
            background = Image.new("RGB", image.size, "white")
            background.paste(image, mask=image.getchannel("A"))
            return background
        return image.convert("RGB")
    if image.mode not in {"RGB", "RGBA", "L", "LA"}:
        return image.convert("RGBA" if "transparency" in image.info else "RGB")
    return image.copy()


def _encode(image: Image.Image, mime_type: str) -> bytes:
    output = BytesIO()
    image_format = SUPPORTED_MIME_FORMATS[mime_type][0]
    options: dict[str, object] = {"format": image_format}
    if image_format == "JPEG":
        options.update(quality=88, optimize=True)
    elif image_format == "PNG":
        options.update(optimize=True)
    else:
        options.update(quality=85, method=6)
    image.save(output, **options)
    return output.getvalue()
