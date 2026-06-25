package com.lamba.app.network

import android.content.Context

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

    private fun chatRequestsKey(userId: Int): String = "$KEY_CHAT_REQUESTS_PREFIX$userId"
}
