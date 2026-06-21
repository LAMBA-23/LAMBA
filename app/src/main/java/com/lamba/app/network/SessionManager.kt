package com.lamba.app.network

import android.content.Context

object SessionManager {
    private const val PREFS_NAME = "lamba_session"
    private const val KEY_USER_ID = "user_id"
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
}
