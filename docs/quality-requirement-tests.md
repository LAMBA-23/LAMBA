# Quality Requirement Tests

This document maps each quality requirement to its automated test.
Each QRT is implemented as a pytest test in the normal test location.

## QRT-01: API Response Time

**Linked quality requirement:** QR-01 (Performance Efficiency — Time Behaviour)

**Test file:** [`backend/tests/test_quality_requirements.py::test_chat_ask_responds_within_timeout`](../backend/tests/test_quality_requirements.py)

**Evidence type:** Automated unit test

**Description:** Verifies that the `/chat/ask` endpoint returns a response within 30 seconds when the external API mock responds promptly.

**How it works:** Mocks `deepseek_chat.ask_deepseek` to return immediately, measures wall-clock time of the HTTP request through FastAPI TestClient, asserts elapsed time is under 30 seconds.

---

## QRT-02: Fault Tolerance on External API Failure

**Linked quality requirement:** QR-02 (Reliability — Fault Tolerance)

**Test file:** [`backend/tests/test_quality_requirements.py::test_chat_ask_returns_fallback_on_api_failure`](../backend/tests/test_quality_requirements.py)

**Evidence type:** Automated unit test

**Description:** Verifies that when the DeepSeek API raises an HTTP error, the `/chat/ask` endpoint returns HTTP 200 with a fallback answer instead of crashing with a 500 error.

**How it works:** Mocks `deepseek_chat.ask_deepseek` to raise `ValueError`, asserts the endpoint returns status 200 and the response body contains a fallback message.

---

## QRT-03: API Key Confidentiality

**Linked quality requirement:** QR-03 (Security — Confidentiality)

**Test file:** [`backend/tests/test_quality_requirements.py::test_api_key_not_exposed_in_chat_ask_response`](../backend/tests/test_quality_requirements.py)

**Evidence type:** Automated unit test

**Description:** Verifies that the `DEEPSEEK_API_KEY` value is never included in any response body returned by the `/chat/ask` endpoint.

**How it works:** Sets a known test API key, mocks the AI to return a normal answer, asserts the response body does not contain the key string. Also tests the error fallback path to ensure the key is not leaked there either.
