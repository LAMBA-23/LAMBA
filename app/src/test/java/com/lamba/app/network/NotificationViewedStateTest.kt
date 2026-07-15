package com.lamba.app.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationViewedStateTest {

    @Test
    fun hasUnreadReturnsFalseWhenThereAreNoRecommendations() {
        assertFalse(NotificationViewedState.hasUnread(emptyList(), emptySet()))
    }

    @Test
    fun hasUnreadReturnsTrueForRecommendationNotInViewedSet() {
        assertTrue(
            NotificationViewedState.hasUnread(
                currentIds = listOf("high_fuel_price"),
                viewedIds = emptySet(),
            ),
        )
    }

    @Test
    fun hasUnreadReturnsFalseAfterRecommendationWasViewed() {
        assertFalse(
            NotificationViewedState.hasUnread(
                currentIds = listOf("high_fuel_price"),
                viewedIds = setOf("high_fuel_price"),
            ),
        )
    }

    @Test
    fun hasUnreadReturnsTrueWhenDifferentRecommendationAppears() {
        assertTrue(
            NotificationViewedState.hasUnread(
                currentIds = listOf("recent_breakdown"),
                viewedIds = setOf("high_fuel_price"),
            ),
        )
    }

    @Test
    fun serializeAndDeserializePreserveViewedIdsForRestart() {
        val stored = NotificationViewedState.serialize(
            setOf("recent_breakdown", "high_fuel_price"),
        )

        assertEquals(
            setOf("high_fuel_price", "recent_breakdown"),
            NotificationViewedState.deserialize(stored),
        )
    }
}
