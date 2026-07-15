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
    fun inactiveViewedIdsAreForgottenSoRepeatedRuleCanBecomeUnread() {
        val activeViewedIds = NotificationViewedState.retainActiveViewedIds(
            currentIds = emptyList(),
            viewedIds = setOf("high_fuel_price:1"),
        )

        assertTrue(activeViewedIds.isEmpty())
        assertTrue(
            NotificationViewedState.hasUnread(
                currentIds = listOf("high_fuel_price:2"),
                viewedIds = activeViewedIds,
            ),
        )
    }

    @Test
    fun continuouslyActiveOccurrenceRemainsViewed() {
        val activeViewedIds = NotificationViewedState.retainActiveViewedIds(
            currentIds = listOf("high_fuel_price:1"),
            viewedIds = setOf("high_fuel_price:1", "stale_records:2"),
        )

        assertEquals(setOf("high_fuel_price:1"), activeViewedIds)
        assertFalse(
            NotificationViewedState.hasUnread(
                currentIds = listOf("high_fuel_price:1"),
                viewedIds = activeViewedIds,
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
