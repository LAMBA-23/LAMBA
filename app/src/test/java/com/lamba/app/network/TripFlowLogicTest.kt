package com.lamba.app.network

import com.lamba.app.chat.KeyValueStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class TripFlowLogicTest {

    @Test
    fun validStartOdometerInputIsAccepted() {
        val result = TripFlowLogic.validateStartOdometer("120000")

        assertEquals(OdometerInputResult.Valid(120000), result)
    }

    @Test
    fun invalidOrBlankStartOdometerInputIsRejected() {
        assertEquals(
            OdometerInputResult.Invalid("Введите текущий пробег"),
            TripFlowLogic.validateStartOdometer(""),
        )
        assertEquals(
            OdometerInputResult.Invalid("Введите пробег целым неотрицательным числом"),
            TripFlowLogic.validateStartOdometer("-1"),
        )
        assertEquals(
            OdometerInputResult.Invalid("Введите пробег целым неотрицательным числом"),
            TripFlowLogic.validateStartOdometer("120.5"),
        )
    }

    @Test
    fun actionLabelSwitchesWhenTripIsActive() {
        assertEquals("Начать поездку", TripFlowLogic.actionLabel(null))
        assertEquals("Завершить поездку", TripFlowLogic.actionLabel(ActiveTrip(120000)))
    }

    @Test
    fun endingOdometerLowerThanStartIsRejected() {
        val result = TripFlowLogic.validateFinishOdometer("119999", odometerStart = 120000)

        assertEquals(
            OdometerInputResult.Invalid("Пробег в конце не может быть меньше начального"),
            result,
        )
    }

    @Test
    fun tripDistanceIsCalculatedFromOdometerRange() {
        assertEquals(125, TripFlowLogic.tripDistance(120000, 120125))
        assertThrows(IllegalArgumentException::class.java) {
            TripFlowLogic.tripDistance(120125, 120000)
        }
    }

    @Test
    fun completedTripRequestUsesHistoryCompatibleFields() {
        val request = TripFlowLogic.createTripRequest(
            odometerStart = 120000,
            odometerEnd = 120125,
            date = "15.07.2026",
        )

        assertEquals("trip", request.type)
        assertEquals(null, request.amount)
        assertEquals(null, request.fuelLiters)
        assertEquals(null, request.mileage)
        assertEquals(120000, request.odometerStart)
        assertEquals(120125, request.odometerEnd)
        assertEquals("Поездка 15.07.2026", request.description)
    }

    @Test
    fun completedTripRequestCanBeMappedForHistoryDisplay() {
        val request = TripFlowLogic.createTripRequest(
            odometerStart = 120000,
            odometerEnd = 120125,
            date = "15.07.2026",
        )
        val historyData = HistoryRecordEventMapper.fromEvent(
            Event(
                id = 10,
                type = request.type,
                description = request.description,
                amount = 0.0,
                mileage = 0.0,
                odometerStart = request.odometerStart,
                odometerEnd = request.odometerEnd,
                tripDistance = 125,
            ),
        )

        assertEquals(HistoryRecordType.TRIP, historyData.type)
        assertEquals("125", historyData.values["mileage"])
        assertEquals("120000", historyData.values["odometerStart"])
        assertEquals("120125", historyData.values["odometerEnd"])
    }

    @Test
    fun activeTripStatePreservesStartUntilCleared() {
        val state = ActiveTripState(FakeKeyValueStore())

        state.save(userId = 7, ActiveTrip(120000))

        assertEquals(ActiveTrip(120000), state.get(7))
        assertNull(state.get(8))
        state.clear(7)
        assertNull(state.get(7))
    }

    @Test
    fun activeTripStateCanPreserveStartAfterSaveFailure() {
        val state = ActiveTripState(FakeKeyValueStore())
        state.save(userId = 7, ActiveTrip(120000))

        assertEquals(ActiveTrip(120000), state.get(7))
    }

    @Test
    fun activeTripStateClearsAfterSuccessfulSaving() {
        val state = ActiveTripState(FakeKeyValueStore())
        state.save(userId = 7, ActiveTrip(120000))

        state.clear(7)

        assertNull(state.get(7))
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
