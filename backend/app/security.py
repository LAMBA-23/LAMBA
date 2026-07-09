from __future__ import annotations

import hashlib
import hmac
import secrets


PASSWORD_HASH_PREFIX = "pbkdf2_sha256"
PBKDF2_ITERATIONS = 390000
SALT_BYTES = 16


def hash_password(password: str) -> str:
    salt = secrets.token_hex(SALT_BYTES)
    digest = hashlib.pbkdf2_hmac(
        "sha256",
        password.encode("utf-8"),
        salt.encode("utf-8"),
        PBKDF2_ITERATIONS,
    )
    return (
        f"{PASSWORD_HASH_PREFIX}${PBKDF2_ITERATIONS}"
        f"${salt}${digest.hex()}"
    )


def is_password_hashed(password_value: str) -> bool:
    return password_value.startswith(f"{PASSWORD_HASH_PREFIX}$")


def verify_password(password: str, password_value: str) -> bool:
    if not is_password_hashed(password_value):
        return hmac.compare_digest(password_value, password)

    try:
        _, iterations, salt, expected_digest = password_value.split("$", maxsplit=3)
    except ValueError:
        return False

    digest = hashlib.pbkdf2_hmac(
        "sha256",
        password.encode("utf-8"),
        salt.encode("utf-8"),
        int(iterations),
    )
    return hmac.compare_digest(digest.hex(), expected_digest)
