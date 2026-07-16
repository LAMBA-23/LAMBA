# Voice input design

## Scope

Implement issue #53: let a signed-in user dictate a chat message. The feature is
available from the existing chat input and does not change the existing typed-message
workflow.

## User flow

1. The chat input has a microphone button to the right of the text field, before the
   existing send button.
2. A tap requests Android's microphone permission when necessary. If granted, it starts
   recording; the button visibly indicates that recording is in progress.
3. A second tap stops recording and uploads the recorded audio to the backend.
4. The backend returns a cleaned transcript. Android inserts it into the current text
   field without sending it, so the user can review or edit it and then use the normal
   send button.
5. A denied permission, recording failure, invalid audio, network error, transcription
   failure, or exhausted provider quota displays a Russian-language error. Typed input
   and its send button remain available in every case.

There is no application-imposed recording-duration limit. The client releases recorder
resources when recording stops or the activity is destroyed.

## Android components

- Add `RECORD_AUDIO` to the manifest and implement the runtime-permission flow in
  `ChatActivity`.
- Reuse the existing microphone drawable for an accessible `ImageButton` placed inside
  the input control; keep the existing send control unchanged.
- Record to an app-private temporary audio file, upload it as multipart data through
  `LambaApiService`, then delete the temporary file after the request completes.
- Disable only the microphone while transcription is pending. The text box and send
  button remain usable.
- Add isolated helpers/tests for the recording state and request/result handling where
  they can run as Android JVM tests.

## Backend endpoint and provider integration

Add an authenticated, multipart `POST /chat/transcribe` endpoint with the same
`user_id` query convention as the chat endpoints. It accepts one audio file and returns
`{ "text": "..." }`.

The backend validates that a non-empty audio upload is present, forwards it to Mistral's
speech-to-text endpoint using the configurable transcription model (default
`voxtral-mini-latest`), and does not save audio or transcripts. Language is not forced;
Mistral performs automatic language recognition.

The backend then requests a conservative text cleanup from a configurable Mistral text
model. The instruction removes fillers and hesitation sounds, collapses accidental
repetitions, fixes punctuation and clearly obvious slips, and must preserve meaning,
numbers, names, measurements, vehicle facts, and uncertainty. If cleanup fails after a
successful transcription, the endpoint returns the original transcript rather than
discarding the user's speech.

## Key rotation and failure handling

`MISTRAL_API_KEYS` is a comma-separated ordered list supplied only via backend runtime
environment variables. It is never exposed to Android, logs, committed configuration,
or error responses. A shared provider client tries the current key and moves to the next
key only for provider quota/rate-limit responses. It does not retry client validation
errors or arbitrary provider failures with every key. The same rotation logic is used
for both transcription and cleanup. If no usable key remains, the endpoint returns a
stable service-unavailable error that Android translates into a user-readable message.

Optional environment variables select model identifiers and timeout values, allowing
Mistral model changes without Android releases. Docker Compose documents variable names
with empty defaults and contains no secrets.

## Tests and documentation

- Backend tests mock the Mistral transport and cover successful transcription and
  cleanup, cleanup fallback, key rotation on a quota response, all-keys-exhausted,
  missing audio, and unsupported/empty audio.
- Android JVM tests cover state transitions and mapping of successful and failed
  responses; manual verification covers permission denial and a physical-device
  recording.
- Update the API contract, README runtime configuration, testing evidence, UAT scenario,
  changelog, and issue #53 traceability as required by repository policy.

## Non-goals

- No automatic sending of voice transcripts.
- No persistent storage, playback, attachments, diarization, timestamps, or live
  streaming transcription.
- No modifications to the existing text-chat endpoint or chat history model.
