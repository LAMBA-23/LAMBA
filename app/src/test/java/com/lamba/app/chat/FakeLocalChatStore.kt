package com.lamba.app.chat

class FakeLocalChatStore : LocalChatStore {
    private val chats = linkedMapOf<Long, LocalChatEntity>()
    private val messages = linkedMapOf<Long, MutableList<LocalChatMessageEntity>>()
    private var nextChatId = 1L
    private var nextMessageId = 1L

    override suspend fun createChat(chat: LocalChatEntity): Long {
        val chatId = nextChatId++
        chats[chatId] = chat.copy(id = chatId)
        messages.putIfAbsent(chatId, mutableListOf())
        return chatId
    }

    override suspend fun updateChat(chat: LocalChatEntity) {
        chats[chat.id] = chat
    }

    override suspend fun insertMessage(message: LocalChatMessageEntity): Long {
        val messageId = nextMessageId++
        messages.getOrPut(message.chatId) { mutableListOf() }
            .add(message.copy(id = messageId))
        return messageId
    }

    override suspend fun getChat(chatId: Long): LocalChatWithMessages? {
        val chat = chats[chatId] ?: return null
        return LocalChatWithMessages(
            chat = chat,
            messages = messages[chatId].orEmpty().sortedBy { it.sortOrder },
        )
    }

    override suspend fun getChatsForUser(userId: Int): List<LocalChatWithMessages> {
        return chats.values
            .filter { it.userId == userId }
            .sortedWith(
                compareByDescending<LocalChatEntity> { it.lastActivityAt }
                    .thenByDescending { it.id }
            )
            .mapNotNull { getChat(it.id) }
    }

    override suspend fun deleteChat(chatId: Long) {
        chats.remove(chatId)
        messages.remove(chatId)
    }

    override suspend fun deleteChatsForUser(userId: Int) {
        val chatIds = chats.values
            .filter { it.userId == userId }
            .map { it.id }
            .toList()
        for (chatId in chatIds) {
            deleteChat(chatId)
        }
    }

    override suspend fun getNextSortOrder(chatId: Long): Int {
        return messages[chatId].orEmpty().maxOfOrNull { it.sortOrder }?.plus(1) ?: 0
    }
}
