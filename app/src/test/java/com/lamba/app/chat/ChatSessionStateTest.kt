package com.lamba.app.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChatSessionStateTest {

    @Test
    fun storesCurrentChatIdPerUser() {
        val prefs = FakeKeyValueStore()
        val state = ChatSessionState(prefs)

        state.setCurrentChatId(userId = 10, chatId = 55L)

        assertEquals(55L, state.getCurrentChatId(10))
        assertNull(state.getCurrentChatId(11))
    }

    @Test
    fun clearsCurrentChatIdWithoutTouchingOtherUsers() {
        val prefs = FakeKeyValueStore()
        val state = ChatSessionState(prefs)
        state.setCurrentChatId(userId = 10, chatId = 55L)
        state.setCurrentChatId(userId = 11, chatId = 66L)

        state.clearCurrentChatId(10)

        assertNull(state.getCurrentChatId(10))
        assertEquals(66L, state.getCurrentChatId(11))
    }

    private class FakeKeyValueStore : KeyValueStore {
        private val values = mutableMapOf<String, String>()

        override fun putString(key: String, value: String) {
            values[key] = value
        }

        override fun getString(key: String): String? = values[key]

        override fun remove(key: String) {
            values.remove(key)
        }
    }
}
