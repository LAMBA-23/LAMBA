package com.lamba.app.chat

import java.time.Instant

class LocalChatHistoryRepository(
    private val store: LocalChatStore,
) {

    suspend fun createChatWithFirstExchange(
        userId: Int,
        greeting: String,
        userMessage: String,
        assistantMessage: String,
        assistantMessageType: ChatMessageType = ChatMessageType.ASSISTANT,
        createdAt: Instant,
    ): Long {
        val timestamp = createdAt.toEpochMilli()
        val title = fallbackTitle(userMessage)
        val chatId = store.createChat(
            LocalChatEntity(
                userId = userId,
                title = title,
                createdAt = timestamp,
                lastActivityAt = timestamp,
            ),
        )

        store.insertMessage(
            LocalChatMessageEntity(
                chatId = chatId,
                sender = ChatSender.SYSTEM,
                text = greeting,
                type = ChatMessageType.GREETING,
                createdAt = timestamp,
                sortOrder = 0,
            ),
        )
        store.insertMessage(
            LocalChatMessageEntity(
                chatId = chatId,
                sender = ChatSender.USER,
                text = userMessage,
                type = ChatMessageType.USER,
                createdAt = timestamp,
                sortOrder = 1,
            ),
        )
        store.insertMessage(
            LocalChatMessageEntity(
                chatId = chatId,
                sender = ChatSender.ASSISTANT,
                text = assistantMessage,
                type = assistantMessageType,
                createdAt = timestamp,
                sortOrder = 2,
            ),
        )

        trimChatsToFive(userId)
        return chatId
    }

    suspend fun appendMessage(
        chatId: Long,
        sender: ChatSender,
        text: String,
        type: ChatMessageType,
        createdAt: Instant,
    ) {
        val chat = store.getChat(chatId) ?: return
        val timestamp = createdAt.toEpochMilli()
        store.insertMessage(
            LocalChatMessageEntity(
                chatId = chatId,
                sender = sender,
                text = text,
                type = type,
                createdAt = timestamp,
                sortOrder = store.getNextSortOrder(chatId),
            ),
        )
        store.updateChat(chat.chat.copy(lastActivityAt = timestamp))
    }

    suspend fun getChat(chatId: Long): LocalChatWithMessages? = store.getChat(chatId)

    suspend fun getChatsForUser(userId: Int): List<LocalChatWithMessages> = store.getChatsForUser(userId)

    suspend fun clearUserChats(userId: Int) {
        store.deleteChatsForUser(userId)
    }

    suspend fun updateChatTitle(chatId: Long, title: String) {
        val chat = store.getChat(chatId)?.chat ?: return
        val normalizedTitle = title.trim().take(MAX_TITLE_LENGTH).ifBlank { chat.title }
        store.updateChat(chat.copy(title = normalizedTitle))
    }

    private suspend fun trimChatsToFive(userId: Int) {
        val chats = store.getChatsForUser(userId)
        chats.drop(MAX_CHATS_PER_USER).forEach { chat ->
            store.deleteChat(chat.chat.id)
        }
    }

    private fun fallbackTitle(userMessage: String): String {
        return userMessage.trim()
            .replace(Regex("\\s+"), " ")
            .take(MAX_TITLE_LENGTH)
            .ifBlank { DEFAULT_TITLE }
    }

    private companion object {
        const val MAX_CHATS_PER_USER = 5
        const val MAX_TITLE_LENGTH = 50
        const val DEFAULT_TITLE = "Новый чат"
    }
}
