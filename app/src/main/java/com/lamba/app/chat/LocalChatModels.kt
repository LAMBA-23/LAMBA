package com.lamba.app.chat

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class ChatSender {
    USER,
    ASSISTANT,
    SYSTEM,
}

enum class ChatMessageType {
    GREETING,
    USER,
    ASSISTANT,
    CLARIFICATION,
    EVENT_CONFIRMATION,
    ERROR,
}

@Entity(
    tableName = "local_chats",
    indices = [Index(value = ["user_id", "last_activity_at"])],
)
data class LocalChatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    val title: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "last_activity_at")
    val lastActivityAt: Long,
)

@Entity(
    tableName = "local_chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = LocalChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chat_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["chat_id", "sort_order"])],
)
data class LocalChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "chat_id")
    val chatId: Long,
    val sender: ChatSender,
    val text: String,
    val type: ChatMessageType,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
)

data class LocalChatWithMessages(
    @Embedded
    val chat: LocalChatEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "chat_id",
    )
    val messages: List<LocalChatMessageEntity>,
)
