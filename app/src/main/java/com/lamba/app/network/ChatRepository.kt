package com.lamba.app.network

import kotlinx.coroutines.CancellationException

interface ChatBackend {
    suspend fun parseMessage(message: String): ChatParseResponse

    suspend fun saveEvent(event: ParsedEventPayload): Event
}

class RetrofitChatBackend(
    private val api: LambaApiService,
) : ChatBackend {

    override suspend fun parseMessage(message: String): ChatParseResponse {
        val response = api.parseChatEvent(ChatParseRequest(message = message))
        if (!response.isSuccessful) {
            throw ChatBackendException("Chat parsing failed with HTTP ${response.code()}")
        }
        return response.body()
            ?: throw ChatBackendException("Chat parsing returned an empty response")
    }

    override suspend fun saveEvent(event: ParsedEventPayload): Event {
        val response = api.createEventFromChat(
            EventCreateRequest(
                type = event.type,
                description = event.description,
                amount = event.amount,
                mileage = event.mileage,
            )
        )
        if (!response.isSuccessful) {
            throw ChatBackendException("Event saving failed with HTTP ${response.code()}")
        }
        return response.body()
            ?: throw ChatBackendException("Event saving returned an empty response")
    }
}

class ChatBackendException(message: String) : Exception(message)

class ChatRepository(
    private val backend: ChatBackend,
) {

    suspend fun sendMessage(message: String): ChatSendResult {
        val parseResponse = try {
            backend.parseMessage(message)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            return ChatSendResult.Failure(ChatFailureStage.PARSING)
        }

        if (parseResponse.status == STATUS_CLARIFICATION_NEEDED) {
            return ChatSendResult.Clarification(
                parseResponse.clarificationQuestion ?: DEFAULT_CLARIFICATION
            )
        }

        val parsedEvent = parseResponse.parsedEvent
        if (parseResponse.status != STATUS_PARSED || parsedEvent == null) {
            return ChatSendResult.Failure(ChatFailureStage.PARSING)
        }

        val savedEvent = try {
            backend.saveEvent(parsedEvent)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            return ChatSendResult.Failure(ChatFailureStage.SAVING)
        }

        return ChatSendResult.Saved(savedEvent)
    }

    private companion object {
        const val STATUS_PARSED = "parsed"
        const val STATUS_CLARIFICATION_NEEDED = "clarification_needed"
        const val DEFAULT_CLARIFICATION =
            "\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435, \u043f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430, \u0434\u0435\u0442\u0430\u043b\u0438 \u0437\u0430\u043f\u0438\u0441\u0438."
    }
}

sealed class ChatSendResult {
    data class Saved(val event: Event) : ChatSendResult()

    data class Clarification(val question: String) : ChatSendResult()

    data class Failure(val stage: ChatFailureStage) : ChatSendResult()
}

enum class ChatFailureStage {
    PARSING,
    SAVING,
}
