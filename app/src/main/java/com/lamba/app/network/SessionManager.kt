package com.lamba.app.network

import android.content.Context
import com.lamba.app.chat.ChatSessionState
import com.lamba.app.chat.KeyValueStore

object SessionManager {
    private const val PREFS_NAME = "lamba_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_CHAT_REQUESTS_PREFIX = "chat_requests_"
    private const val MISSING_USER_ID = -1

    fun saveUserId(context: Context, userId: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_USER_ID, userId)
            .apply()
    }

    fun getUserId(context: Context): Int? {
        val userId = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_USER_ID, MISSING_USER_ID)

        return if (userId == MISSING_USER_ID) null else userId
    }

    fun saveUserName(context: Context, name: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    fun getUserName(context: Context): String? {
        return context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USER_NAME, null)
            ?.takeIf { it.isNotBlank() }
    }

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userId = getUserId(context)
        val chatSessionState = ChatSessionState(SharedPreferencesKeyValueStore(context))

        with(prefs.edit()) {
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            if (userId != null) {
                remove(chatRequestsKey(userId))
                chatSessionState.clearCurrentChatId(userId)
            }
            apply()
        }
    }

    fun addChatRequest(context: Context, message: String) {
        val request = message.trim()
        if (request.isBlank()) return
        val userId = getUserId(context) ?: return

        val requests = getChatRequests(context).toMutableList()
        requests.remove(request)
        requests.add(0, request)

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(chatRequestsKey(userId), requests.take(10).joinToString(separator = "\n"))
            .apply()
    }

    fun getChatRequests(context: Context): List<String> {
        val userId = getUserId(context) ?: return emptyList()
        return context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(chatRequestsKey(userId), null)
            ?.lineSequence()
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toList()
            .orEmpty()
    }

    fun setCurrentChatId(context: Context, chatId: Long) {
        val userId = getUserId(context) ?: return
        ChatSessionState(SharedPreferencesKeyValueStore(context)).setCurrentChatId(userId, chatId)
    }

    fun getCurrentChatId(context: Context): Long? {
        val userId = getUserId(context) ?: return null
        return ChatSessionState(SharedPreferencesKeyValueStore(context)).getCurrentChatId(userId)
    }

    fun clearCurrentChatId(context: Context) {
        val userId = getUserId(context) ?: return
        ChatSessionState(SharedPreferencesKeyValueStore(context)).clearCurrentChatId(userId)
    }

    private fun chatRequestsKey(userId: Int): String = "$KEY_CHAT_REQUESTS_PREFIX$userId"

    private class SharedPreferencesKeyValueStore(
        context: Context,
    ) : KeyValueStore {
        private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        override fun putString(key: String, value: String) {
            prefs.edit().putString(key, value).apply()
        }

        override fun getString(key: String): String? = prefs.getString(key, null)

        override fun remove(key: String) {
            prefs.edit().remove(key).apply()
        }
    }
}
