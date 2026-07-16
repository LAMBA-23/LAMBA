package com.lamba.app.network

import com.lamba.app.chat.KeyValueStore

class ActiveTripState(
    private val keyValueStore: KeyValueStore,
) {
    fun save(userId: Int, trip: ActiveTrip) {
        keyValueStore.putString(activeTripKey(userId), trip.odometerStart.toString())
    }

    fun get(userId: Int): ActiveTrip? {
        return keyValueStore.getString(activeTripKey(userId))
            ?.toIntOrNull()
            ?.takeIf { it >= 0 }
            ?.let { ActiveTrip(it) }
    }

    fun clear(userId: Int) {
        keyValueStore.remove(activeTripKey(userId))
    }

    private fun activeTripKey(userId: Int): String = "active_trip_odometer_start_$userId"
}
