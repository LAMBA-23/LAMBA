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
            description = "Заправка на 2500 рублей",
            amount = 2500,
            mileage = 125300,
        )
        val savedEvent = Event(
            id = 42,
            type = "fuel",
            description = "Заправка на 2500 рублей",
            amount = 2500,
            mileage = 125300,
            createdAt = "2026-06-21T12:00:00",
        )
        val backend = FakeChatBackend(
            parseResponse = ChatParseResponse(
                status = "parsed",
                parsedEvent = parsedEvent,
            ),
            savedEvent = savedEvent,
        )

        val result = ChatRepository(backend).sendMessage("Заправился на 2500 рублей")

        assertEquals(ChatSendResult.Saved(savedEvent), result)
        assertEquals(listOf(parsedEvent), backend.savedEvents)
    }

    @Test
    fun clarificationIsShownWithoutSavingEvent() = runBlocking {
        val question = "Это была заправка, ремонт, поездка или проблема?"
        val backend = FakeChatBackend(
            parseResponse = ChatParseResponse(
                status = "clarification_needed",
                clarificationQuestion = question,
            )
        )

        val result = ChatRepository(backend).sendMessage("Потратил 3000")

        assertEquals(ChatSendResult.Clarification(question), result)
        assertTrue(backend.savedEvents.isEmpty())
    }

    @Test
    fun parsingFailureDoesNotAttemptSaving() = runBlocking {
        val backend = FakeChatBackend(parseError = IOException("offline"))

        val result = ChatRepository(backend).sendMessage("Заправился")

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
                    description = "Замена масла",
                    amount = 8000,
                ),
            ),
            saveError = IOException("backend unavailable"),
        )

        val result = ChatRepository(backend).sendMessage("Поменял масло за 8000")

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

        val result = ChatRepository(backend).sendMessage("Заправился")

        assertEquals(
            ChatSendResult.Failure(ChatFailureStage.PARSING),
            result,
        )
        assertTrue(backend.savedEvents.isEmpty())
    }

    @Test
    fun technicalConditionUpdateIsSaved() = runBlocking {
        val parsedEvent = ParsedEventPayload(
            type = "condition",
            description = "Техническое состояние хорошее",
            mileage = 125500,
        )
        val savedEvent = Event(
            id = 43,
            type = "condition",
            description = parsedEvent.description,
            amount = 0,
            mileage = 125500,
        )
        val backend = FakeChatBackend(
            parseResponse = ChatParseResponse(
                status = "parsed",
                parsedEvent = parsedEvent,
            ),
            savedEvent = savedEvent,
        )

        val result = ChatRepository(backend).sendMessage(
            "Техническое состояние хорошее, пробег 125500"
        )

        assertEquals(ChatSendResult.Saved(savedEvent), result)
        assertEquals(listOf(parsedEvent), backend.savedEvents)
    }

    private class FakeChatBackend(
        private val parseResponse: ChatParseResponse? = null,
        private val savedEvent: Event? = null,
        private val parseError: Exception? = null,
        private val saveError: Exception? = null,
    ) : ChatBackend {

        val savedEvents = mutableListOf<ParsedEventPayload>()

        override suspend fun parseMessage(message: String): ChatParseResponse {
            parseError?.let { throw it }
            return requireNotNull(parseResponse)
        }

        override suspend fun saveEvent(event: ParsedEventPayload): Event {
            savedEvents.add(event)
            saveError?.let { throw it }
            return requireNotNull(savedEvent)
        }
    }
}
