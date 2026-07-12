package com.lamba.app.chat

import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalChatTitleTest {

    @Test
    fun fallbackTitleIsLimitedToFiftyCharacters() = runBlocking {
        val repository = LocalChatHistoryRepository(FakeLocalChatStore())

        val chatId = repository.createChatWithFirstExchange(
            userId = 15,
            greeting = "Hello",
            userMessage = "This is a very long first user message that must be shortened for title fallback",
            assistantMessage = "Reply",
            assistantMessageType = ChatMessageType.ASSISTANT,
            createdAt = Instant.parse("2026-07-10T10:15:30Z"),
        )

        val title = repository.getChat(chatId)?.chat?.title

        assertEquals("This is a very long first user message that must b", title)
        assertEquals(50, title?.length)
    }

    @Test
    fun blankTitleUpdateKeepsExistingTitle() = runBlocking {
        val repository = LocalChatHistoryRepository(FakeLocalChatStore())
        val chatId = repository.createChatWithFirstExchange(
            userId = 16,
            greeting = "Hello",
            userMessage = "Initial title",
            assistantMessage = "Reply",
            assistantMessageType = ChatMessageType.ASSISTANT,
            createdAt = Instant.parse("2026-07-10T10:15:30Z"),
        )

        repository.updateChatTitle(chatId, "   ")

        assertEquals("Initial title", repository.getChat(chatId)?.chat?.title)
    }
}
