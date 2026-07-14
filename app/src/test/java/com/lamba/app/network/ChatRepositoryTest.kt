package com.lamba.app.network

import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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
        val question = "Was it fuel, repair, trip, or issue?"
        val backend = FakeChatBackend(
            parseResponse = ChatParseResponse(
                status = "clarification_needed",
                clarificationQuestion = question,
            )
        )

        val result = ChatRepository(backend).sendMessage("Spent 3000")

        assertEquals(ChatSendResult.Clarification(question), result)
        assertTrue(backend.savedEvents.isEmpty())
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

        override suspend fun parseMessage(message: String): ChatParseResponse {
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
            lastChatContext = chatContext
            return "OK"
        }
    }
}
