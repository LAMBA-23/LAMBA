package com.lamba.app.chat

interface LocalChatStore {
    suspend fun createChat(chat: LocalChatEntity): Long

    suspend fun updateChat(chat: LocalChatEntity)

    suspend fun insertMessage(message: LocalChatMessageEntity): Long

    suspend fun getChat(chatId: Long): LocalChatWithMessages?

    suspend fun getChatsForUser(userId: Int): List<LocalChatWithMessages>

    suspend fun deleteChat(chatId: Long)

    suspend fun deleteChatsForUser(userId: Int)

    suspend fun getNextSortOrder(chatId: Long): Int
}

class RoomLocalChatStore(
    private val dao: LocalChatDao,
) : LocalChatStore {
    override suspend fun createChat(chat: LocalChatEntity): Long = dao.insertChat(chat)

    override suspend fun updateChat(chat: LocalChatEntity) = dao.updateChat(chat)

    override suspend fun insertMessage(message: LocalChatMessageEntity): Long = dao.insertMessage(message)

    override suspend fun getChat(chatId: Long): LocalChatWithMessages? = dao.getChat(chatId)

    override suspend fun getChatsForUser(userId: Int): List<LocalChatWithMessages> = dao.getChatsForUser(userId)

    override suspend fun deleteChat(chatId: Long) = dao.deleteChat(chatId)

    override suspend fun deleteChatsForUser(userId: Int) = dao.deleteChatsForUser(userId)

    override suspend fun getNextSortOrder(chatId: Long): Int = dao.getNextSortOrder(chatId)
}
