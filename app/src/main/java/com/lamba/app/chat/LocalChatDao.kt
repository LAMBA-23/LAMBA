package com.lamba.app.chat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface LocalChatDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertChat(chat: LocalChatEntity): Long

    @Update
    suspend fun updateChat(chat: LocalChatEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMessage(message: LocalChatMessageEntity): Long

    @Transaction
    @Query("SELECT * FROM local_chats WHERE id = :chatId")
    suspend fun getChat(chatId: Long): LocalChatWithMessages?

    @Transaction
    @Query(
        """
        SELECT * FROM local_chats
        WHERE user_id = :userId
        ORDER BY last_activity_at DESC, id DESC
        """,
    )
    suspend fun getChatsForUser(userId: Int): List<LocalChatWithMessages>

    @Query("DELETE FROM local_chats WHERE id = :chatId")
    suspend fun deleteChat(chatId: Long)

    @Query("DELETE FROM local_chats WHERE user_id = :userId")
    suspend fun deleteChatsForUser(userId: Int)

    @Query("SELECT COALESCE(MAX(sort_order) + 1, 0) FROM local_chat_messages WHERE chat_id = :chatId")
    suspend fun getNextSortOrder(chatId: Long): Int
}
