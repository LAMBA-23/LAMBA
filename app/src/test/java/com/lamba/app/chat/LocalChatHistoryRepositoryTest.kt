package com.lamba.app.chat

import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalChatHistoryRepositoryTest {

    @Test
    fun createsNonEmptyChatWithGreetingAndMessages() = runBlocking {
        val repository = LocalChatHistoryRepository(FakeLocalChatStore())

        val chatId = repository.createChatWithFirstExchange(
            userId = 7,
            greeting = "Привет",
            userMessage = "Покажи расходы",
            assistantMessage = "Вот последние расходы",
            assistantMessageType = ChatMessageType.ASSISTANT,
            createdAt = Instant.parse("2026-07-10T10:15:30Z"),
        )

        val chat = repository.getChat(chatId)

        assertEquals(7, chat?.chat?.userId)
        assertFalse(chat?.chat?.title.isNullOrBlank())
        assertEquals(
            listOf("Привет", "Покажи расходы", "Вот последние расходы"),
            chat?.messages?.map { it.text },
        )
        assertEquals(
            listOf(
                ChatMessageType.GREETING,
                ChatMessageType.USER,
                ChatMessageType.ASSISTANT,
            ),
            chat?.messages?.map { it.type },
        )
    }

    @Test
    fun keepsOnlyFiveMostRecentNonEmptyChatsPerUser() = runBlocking {
        val repository = LocalChatHistoryRepository(FakeLocalChatStore())

        repeat(6) { index ->
            repository.createChatWithFirstExchange(
                userId = 42,
                greeting = "Привет",
                userMessage = "Сообщение $index",
                assistantMessage = "Ответ $index",
                assistantMessageType = ChatMessageType.ASSISTANT,
                createdAt = Instant.parse("2026-07-10T10:15:3${index}Z"),
            )
        }

        val chats = repository.getChatsForUser(42)

        assertEquals(5, chats.size)
        assertEquals("Сообщение 5", chats.first().messages.first { it.type == ChatMessageType.USER }.text)
        assertNull(chats.find { chat ->
            chat.messages.any { it.type == ChatMessageType.USER && it.text == "Сообщение 0" }
        })
    }

    @Test
    fun reordersChatsByLatestActivityAfterNewMessage() = runBlocking {
        val repository = LocalChatHistoryRepository(FakeLocalChatStore())
        val firstChatId = repository.createChatWithFirstExchange(
            userId = 9,
            greeting = "Привет",
            userMessage = "Первый",
            assistantMessage = "Ответ 1",
            assistantMessageType = ChatMessageType.ASSISTANT,
            createdAt = Instant.parse("2026-07-10T10:15:30Z"),
        )
        val secondChatId = repository.createChatWithFirstExchange(
            userId = 9,
            greeting = "Привет",
            userMessage = "Второй",
            assistantMessage = "Ответ 2",
            assistantMessageType = ChatMessageType.ASSISTANT,
            createdAt = Instant.parse("2026-07-10T10:15:31Z"),
        )

        repository.appendMessage(
            chatId = firstChatId,
            sender = ChatSender.USER,
            text = "Продолжение",
            type = ChatMessageType.USER,
            createdAt = Instant.parse("2026-07-10T10:15:40Z"),
        )

        val orderedChats = repository.getChatsForUser(9)

        assertEquals(listOf(firstChatId, secondChatId), orderedChats.map { it.chat.id })
    }

    @Test
    fun clearsChatsForUser() = runBlocking {
        val repository = LocalChatHistoryRepository(FakeLocalChatStore())
        repository.createChatWithFirstExchange(
            userId = 14,
            greeting = "Привет",
            userMessage = "Первая",
            assistantMessage = "Ответ",
            assistantMessageType = ChatMessageType.ASSISTANT,
            createdAt = Instant.parse("2026-07-10T10:15:30Z"),
        )

        repository.clearUserChats(14)

        assertTrue(repository.getChatsForUser(14).isEmpty())
    }
}
