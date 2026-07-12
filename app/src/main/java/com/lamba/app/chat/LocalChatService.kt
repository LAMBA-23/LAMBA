package com.lamba.app.chat

import android.content.Context

object LocalChatService {
    fun getRepository(context: Context): LocalChatHistoryRepository {
        val database = LocalChatDatabase.getInstance(context)
        return LocalChatHistoryRepository(
            RoomLocalChatStore(database.localChatDao()),
        )
    }
}
