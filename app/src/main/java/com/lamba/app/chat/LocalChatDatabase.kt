package com.lamba.app.chat

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class LocalChatConverters {
    @TypeConverter
    fun fromSender(value: ChatSender): String = value.name

    @TypeConverter
    fun toSender(value: String): ChatSender = ChatSender.valueOf(value)

    @TypeConverter
    fun fromMessageType(value: ChatMessageType): String = value.name

    @TypeConverter
    fun toMessageType(value: String): ChatMessageType = ChatMessageType.valueOf(value)
}

@Database(
    entities = [LocalChatEntity::class, LocalChatMessageEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(LocalChatConverters::class)
abstract class LocalChatDatabase : RoomDatabase() {
    abstract fun localChatDao(): LocalChatDao

    companion object {
        @Volatile
        private var instance: LocalChatDatabase? = null

        fun getInstance(context: Context): LocalChatDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LocalChatDatabase::class.java,
                    "local_chat_history.db",
                ).build().also { instance = it }
            }
        }
    }
}
