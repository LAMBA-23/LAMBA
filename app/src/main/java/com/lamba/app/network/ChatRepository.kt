package com.lamba.app.network

import kotlinx.coroutines.CancellationException

interface ChatBackend {
    suspend fun parseMessage(message: String): ChatParseResponse

    suspend fun saveEvent(event: ParsedEventPayload): Event

    suspend fun askQuestion(message: String, chatContext: List<ChatContextMessage>): String
}

class RetrofitChatBackend(
    private val api: LambaApiService,
    private val userId: Int,
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
                fuelLiters = event.fuelLiters,
                mileage = event.mileage,
            ),
            userId,
        )
        if (!response.isSuccessful) {
            throw ChatBackendException("Event saving failed with HTTP ${response.code()}")
        }
        return response.body()
            ?: throw ChatBackendException("Event saving returned an empty response")
    }

    override suspend fun askQuestion(
        message: String,
        chatContext: List<ChatContextMessage>,
    ): String {
        val response = api.chatAsk(
            ChatAskRequest(message = message, chatContext = chatContext),
            userId,
        )
        if (!response.isSuccessful) {
            throw ChatBackendException("Chat ask failed with HTTP ${response.code()}")
        }
        return response.body()?.answer
            ?: throw ChatBackendException("Chat ask returned an empty response")
    }
}

class ChatBackendException(message: String) : Exception(message)

class ChatRepository(
    private val backend: ChatBackend,
) {

    suspend fun sendMessage(
        message: String,
        chatContext: List<ChatContextMessage> = emptyList(),
    ): ChatSendResult {
        if (isQuestion(message)) {
            return handleQuestion(message, chatContext)
        }
        return handleEventParsing(message)
    }

    private fun isQuestion(message: String): Boolean {
        val lower = message.trim().lowercase()
        val hasGreeting = lower.startsWith("привет") ||
            lower.startsWith("здравствуй") ||
            lower.startsWith("добрый") ||
            lower.startsWith("хай") ||
            lower.startsWith("хей") ||
            lower.startsWith("йо") ||
            lower.startsWith("дарова") ||
            lower.startsWith("здорово") ||
            lower.startsWith("салют") ||
            lower.startsWith("спасибо") ||
            lower.startsWith("благодарю") ||
            lower.startsWith("пока") ||
            lower.startsWith("до свидания") ||
            lower.startsWith("как сам") ||
            lower.startsWith("как дела")
        val hasEventKeywords = lower.contains("заправ") ||
            lower.contains("топлив") ||
            lower.contains("бензин") ||
            lower.contains("дизель") ||
            lower.contains("ремонт") ||
            lower.contains("поменял") ||
            lower.contains("заменил") ||
            lower.contains("сервис") ||
            lower.contains("масло") ||
            lower.contains("пробег") ||
            lower.contains("поездк") ||
            lower.contains("проехал") ||
            lower.contains("поехал") ||
            lower.contains("съездил") ||
            lower.contains("маршрут") ||
            lower.contains("дорог") ||
            lower.contains("путь") ||
            lower.contains("чек") ||
            lower.contains("ошибк") ||
            lower.contains("не завод") ||
            lower.contains("загорел") ||
            lower.contains("ламп") ||
            lower.contains("стук") ||
            lower.contains("скрип") ||
            lower.contains("проблем") ||
            lower.contains("полом")

        if (hasGreeting && hasEventKeywords) return false

        return lower.endsWith("?") ||
            lower.startsWith("как") ||
            lower.startsWith("что") ||
            lower.startsWith("где") ||
            lower.startsWith("когда") ||
            lower.startsWith("сколько") ||
            lower.startsWith("почему") ||
            lower.startsWith("покаж") ||
            lower.startsWith("провер") ||
            lower.startsWith("расскаж") ||
            lower.startsWith("опис") ||
            hasGreeting ||
            lower.contains("последн") ||
            lower.contains("истори") ||
            lower.contains("статистик") ||
            lower.contains("расход") ||
            lower.contains("пробег") ||
            lower.contains("добавить запись") ||
            lower.contains("последние расходы") ||
            lower.contains("новая запись") ||
            lower.contains("записать")
    }

    private suspend fun handleQuestion(
        message: String,
        chatContext: List<ChatContextMessage>,
    ): ChatSendResult {
        val answer = try {
            backend.askQuestion(message, chatContext)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            return ChatSendResult.Failure(ChatFailureStage.PARSING)
        }
        return ChatSendResult.Answer(answer)
    }

    private suspend fun handleEventParsing(message: String): ChatSendResult {
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

        if (!isTimelineEventType(parsedEvent.type)) {
            return ChatSendResult.Clarification(NON_TIMELINE_EVENT_MESSAGE)
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
        const val NON_TIMELINE_EVENT_MESSAGE =
            "\u042d\u0442\u043e \u0437\u0430\u043f\u0440\u043e\u0441 \u043a \u0430\u0441\u0441\u0438\u0441\u0442\u0435\u043d\u0442\u0443, \u0430 \u043d\u0435 \u0441\u043e\u0431\u044b\u0442\u0438\u0435 \u0434\u043b\u044f \u0438\u0441\u0442\u043e\u0440\u0438\u0438."

        fun isTimelineEventType(type: String): Boolean {
            return type.lowercase() in setOf("fuel", "repair", "trip", "issue")
        }
    }
}

sealed class ChatSendResult {
    data class Saved(val event: Event) : ChatSendResult()

    data class Clarification(val question: String) : ChatSendResult()

    data class Answer(val text: String) : ChatSendResult()

    data class Failure(val stage: ChatFailureStage) : ChatSendResult()
}

enum class ChatFailureStage {
    PARSING,
    SAVING,
}
