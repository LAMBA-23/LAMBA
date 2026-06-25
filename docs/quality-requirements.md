# Quality Requirements

This document defines measurable quality requirements for the LAMBA backend.
Each requirement uses a different ISO/IEC 25010 sub-characteristic.

## QR-01: API Response Time

**ISO/IEC 25010 sub-characteristic:** Performance Efficiency — Time Behaviour

**Description:** The `/chat/ask` endpoint must return a response within 30 seconds under normal load when the external AI API is available.

**Rationale:** Users expect quick answers from the AI assistant. Long response times degrade the conversational experience and may cause the mobile client to time out.

**Measurable scenario:**
- Given the DeepSeek API responds within 20 seconds,
- When a user sends a valid question via `POST /chat/ask`,
- Then the endpoint returns a response within 30 seconds.

**Traceability:** Supports US-06 (Ask AI assistant, #49).

**Linked QRT:** QRT-01.

---

## QR-02: Fault Tolerance on External API Failure

**ISO/IEC 25010 sub-characteristic:** Reliability — Fault Tolerance

**Description:** The system must return a meaningful fallback response when the external AI API is unreachable, returns an error, or returns an invalid response. The endpoint must not crash or return a 500 error to the client.

**Rationale:** The DeepSeek API is an external dependency. Network issues, rate limits, or API outages must not break the user-facing chat experience.

**Measurable scenario:**
- Given the DeepSeek API is unreachable or returns an HTTP error,
- When a user sends a question via `POST /chat/ask`,
- Then the endpoint returns a 200 response with a fallback answer message.

**Traceability:** Supports US-06 (Ask AI assistant, #49).

**Linked QRT:** QRT-02.

---

## QR-03: API Key Confidentiality

**ISO/IEC 25010 sub-characteristic:** Security — Confidentiality

**Description:** The `DEEPSEEK_API_KEY` must never be included in any API response body, error message, or log output accessible to the client.

**Rationale:** Exposing API keys allows unauthorized usage and billing abuse. The key must remain server-side only.

**Measurable scenario:**
- Given the `DEEPSEEK_API_KEY` is configured in the environment,
- When any endpoint is called (including error paths),
- Then the response body does not contain the API key value.

**Traceability:** General security requirement for all AI-integrated endpoints.

**Linked QRT:** QRT-03.
