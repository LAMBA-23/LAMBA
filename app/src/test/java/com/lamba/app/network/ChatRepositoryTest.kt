package com.lamba.app.network

import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatRepositoryTest {

    @Test
    fun parsedMessageIsSavedAndReturnsBackendEvent() = runBlocking {
        val parsedEvent = ParsedEventPayload(
            type = "fuel",
            description = "Fuel refill",
            amount = 2500.5,
            mileage = 125300.0,
        )
        val savedEvent = Event(
            id = 42,
            type = "fuel",
            description = "Fuel refill",
            amount = 2500.5,
            mileage = 125300.0,
            createdAt = "2026-06-21T12:00:00",
        )
        val backend = FakeChatBackend(
            parseResponse = ChatParseResponse(
                status = "parsed",
                parsedEvent = parsedEvent,
            ),
            savedEvent = savedEvent,
        )

        val result = ChatRepository(backend).sendMessage("Refilled fuel for 2500")

        assertEquals(ChatSendResult.Saved(savedEvent), result)
        assertEquals(listOf(parsedEvent), backend.savedEvents)
    }

    @Test
    fun parsedFuelLitersAreSavedWithEvent() = runBlocking {
        val parsedEvent = ParsedEventPayload(
            type = "fuel",
            description = "Заправка на 40 литров",
            amount = 2500.25,
            fuelLiters = 40.5,
            mileage = null,
        )
        val savedEvent = Event(
            id = 44,
            type = "fuel",
            description = "Заправка на 40 литров",
            amount = 2500.25,
            fuelLiters = 40.5,
            mileage = 0.0,
            createdAt = "2026-06-21T12:00:00",
        )
        val backend = FakeChatBackend(
            parseResponse = ChatParseResponse(
                status = "parsed",
                parsedEvent = parsedEvent,
            ),
            savedEvent = savedEvent,
        )

        val result = ChatRepository(backend).sendMessage("Заправился на 40 литров за 2500 рублей")

        assertEquals(ChatSendResult.Saved(savedEvent), result)
        assertEquals(listOf(parsedEvent), backend.savedEvents)
    }

    @Test
    fun clarificationIsShownWithoutSavingEvent() = runBlocking {
        val question = "Уточните тип события"
        val backend = FakeChatBackend(
            parseResponse = ChatParseResponse(
                status = "clarification_needed",
                clarificationQuestion = question,
            ),
        )
        val repo = ChatRepository(backend)
        val result = repo.sendMessage("Загадочное сообщение")

        assertTrue(result is ChatSendResult.Clarification)
        assertEquals(question, (result as ChatSendResult.Clarification).question)
        assertTrue(backend.savedEvents.isEmpty())
    }

    @Test
    fun greetingRoutesToAssistantNotParser() = runBlocking {
        val backend = FakeChatBackend(
            parseResponse = ChatParseResponse(
                status = "parsed",
                parsedEvent = null,
            ),
        )
        val repo = ChatRepository(backend)
        val result = repo.sendMessage("привет")

        assertTrue(result is ChatSendResult.Answer)
        assertTrue(backend.askQuestionCalled)
    }

    @Test
    fun thanksRoutesToAssistantNotParser() = runBlocking {
        val backend = FakeChatBackend(
            parseResponse = ChatParseResponse(
                status = "parsed",
                parsedEvent = null,
            ),
        )
        val repo = ChatRepository(backend)
        val result = repo.sendMessage("спасибо")

        assertTrue(result is ChatSendResult.Answer)
        assertTrue(backend.askQuestionCalled)
    }

    @Test
    fun greetingWithEventRoutesToParser() = runBlocking {
        val parsedEvent = ParsedEventPayload(
            type = "fuel",
            description = "Заправка на 40 литров",
            amount = null,
            fuelLiters = 40.0,
            mileage = 125000,
        )
        val savedEvent = Event(
            id = 1,
            type = "fuel",
            description = "Заправка на 40 литров",
            amount = 0,
            mileage = 125000,
            createdAt = "2026-07-14T12:00:00",
        )
        val backend = FakeChatBackend(
            parseResponse = ChatParseResponse(
                status = "parsed",
                parsedEvent = parsedEvent,
            ),
            savedEvent = savedEvent,
        )
        val repo = ChatRepository(backend)
        val result = repo.sendMessage("привет, заправился на 40 литров")

        assertTrue(result is ChatSendResult.Saved)
        assertTrue(backend.parseMessageCalled)
        assertFalse(backend.askQuestionCalled)
    }

    @Test
    fun parsingFailureDoesNotAttemptSaving() = runBlocking {
        val backend = FakeChatBackend(parseError = IOException("offline"))

        val result = ChatRepository(backend).sendMessage("Refilled")

        assertEquals(
            ChatSendResult.Failure(ChatFailureStage.PARSING),
            result,
        )
        assertTrue(backend.savedEvents.isEmpty())
    }

    @Test
    fun savingFailureIsReportedSeparately() = runBlocking {
        val backend = FakeChatBackend(
            parseResponse = ChatParseResponse(
                status = "parsed",
                parsedEvent = ParsedEventPayload(
                    type = "repair",
                    description = "Oil change",
                    amount = 8000.5,
                ),
            ),
            saveError = IOException("backend unavailable"),
        )

        val result = ChatRepository(backend).sendMessage("Changed oil for 8000")

        assertEquals(
            ChatSendResult.Failure(ChatFailureStage.SAVING),
            result,
        )
        assertEquals(1, backend.savedEvents.size)
    }

    @Test
    fun malformedParsedResponseDoesNotAttemptSaving() = runBlocking {
        val backend = FakeChatBackend(
            parseResponse = ChatParseResponse(status = "parsed")
        )

        val result = ChatRepository(backend).sendMessage("Refilled")

        assertEquals(
            ChatSendResult.Failure(ChatFailureStage.PARSING),
            result,
        )
        assertTrue(backend.savedEvents.isEmpty())
    }

    @Test
    fun nonTimelineConditionMessageIsNotSaved() = runBlocking {
        val parsedEvent = ParsedEventPayload(
            type = "condition",
            description = "Technical condition is good",
            mileage = 125500.0,
        )
        val backend = FakeChatBackend(
            parseResponse = ChatParseResponse(
                status = "parsed",
                parsedEvent = parsedEvent,
            )
        )

        val result = ChatRepository(backend).sendMessage("Check vehicle condition")

        assertTrue(result is ChatSendResult.Clarification)
        assertTrue(backend.savedEvents.isEmpty())
    }

    @Test
    fun questionPassesChatContextToBackend() = runBlocking {
        val backend = FakeChatBackend()
        val repository = ChatRepository(backend)
        val chatContext = listOf(
            ChatContextMessage(sender = "user", text = "Привет"),
            ChatContextMessage(sender = "assistant", text = "Здравствуйте"),
        )

        val result = repository.sendMessage("Что дальше?", chatContext)

        assertEquals(ChatSendResult.Answer("OK"), result)
        assertEquals(chatContext, backend.lastChatContext)
    }

    private class FakeChatBackend(
        private val parseResponse: ChatParseResponse? = null,
        private val savedEvent: Event? = null,
        private val parseError: Exception? = null,
        private val saveError: Exception? = null,
    ) : ChatBackend {

        val savedEvents = mutableListOf<ParsedEventPayload>()
        var lastChatContext: List<ChatContextMessage>? = null
        var parseMessageCalled = false
        var askQuestionCalled = false

        override suspend fun parseMessage(message: String): ChatParseResponse {
            parseMessageCalled = true
            parseError?.let { throw it }
            return requireNotNull(parseResponse)
        }

        override suspend fun saveEvent(event: ParsedEventPayload): Event {
            savedEvents.add(event)
            saveError?.let { throw it }
            return requireNotNull(savedEvent)
        }

        override suspend fun askQuestion(
            message: String,
            chatContext: List<ChatContextMessage>,
        ): String {
            askQuestionCalled = true
            lastChatContext = chatContext
            return "OK"
        }
    }
}
