package com.lamba.app.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RetrofitClientTest {

    @Test
    fun resolveBackendUrlPreservesAbsoluteUrls() {
        val url = "https://example.com/events/5/photo?user_id=2"

        assertEquals(url, RetrofitClient.resolveBackendUrl(url))
    }

    @Test
    fun resolveBackendUrlExpandsRootRelativeEventPhotoUrls() {
        val resolved = RetrofitClient.resolveBackendUrl("/events/5/photo?user_id=2")

        assertEquals("http://186.246.27.211:8000/events/5/photo?user_id=2", resolved)
    }

    @Test
    fun backendUrlReferenceIncludesRootRelativePhotoRoutes() {
        assertTrue(RetrofitClient.isBackendUrlReference("/events/5/photo?user_id=2"))
        assertTrue(RetrofitClient.isBackendUrlReference("/events/5/photo/thumbnail?user_id=2"))
        assertTrue(RetrofitClient.isBackendUrlReference("http://186.246.27.211:8000/events/5/photo"))
        assertTrue(RetrofitClient.isBackendUrlReference("https://example.com/events/5/photo"))
    }

    @Test
    fun backendUrlReferenceExcludesLocalContentUris() {
        assertFalse(RetrofitClient.isBackendUrlReference("content://media/photo/5"))
    }
}
