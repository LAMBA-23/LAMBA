package com.lamba.app.chat

interface KeyValueStore {
    fun putString(key: String, value: String)

    fun getString(key: String): String?

    fun remove(key: String)
}

class ChatSessionState(
    private val keyValueStore: KeyValueStore,
) {
    fun setCurrentChatId(userId: Int, chatId: Long) {
        keyValueStore.putString(currentChatKey(userId), chatId.toString())
    }

    fun getCurrentChatId(userId: Int): Long? {
        return keyValueStore.getString(currentChatKey(userId))
            ?.toLongOrNull()
    }

    fun clearCurrentChatId(userId: Int) {
        keyValueStore.remove(currentChatKey(userId))
    }

    private fun currentChatKey(userId: Int): String = "current_chat_id_$userId"
}
