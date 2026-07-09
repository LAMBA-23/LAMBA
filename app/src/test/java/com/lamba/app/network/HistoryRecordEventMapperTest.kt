package com.lamba.app.network

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryRecordEventMapperTest {

    @Test
    fun fuelFormValuesMapToFuelEventRequest() {
        val request = HistoryRecordEventMapper.toEventRequest(
            HistoryRecordType.FUEL,
            mapOf(
                "date" to "2026-07-06",
                "fuelType" to "AI-95",
                "litres" to "40",
                "cost" to "2500",
            ),
        )

        assertEquals("fuel", request.type)
        assertEquals(2500, request.amount)
        assertEquals(40.0, request.fuelLiters ?: 0.0, 0.0)
        assertEquals(null, request.mileage)
        assertEquals(
            "Заправка 2026-07-06: AI-95, 40 л, 2500 ₽",
            request.description,
        )
    }

    @Test
    fun decimalFuelFormValuesMapToFuelEventRequest() {
        val request = HistoryRecordEventMapper.toEventRequest(
            HistoryRecordType.FUEL,
            mapOf(
                "date" to "2026-07-06",
                "fuelType" to "AI-95",
                "litres" to "35.7",
                "cost" to "2500",
            ),
        )

        assertEquals("fuel", request.type)
        assertEquals(2500, request.amount)
        assertEquals(35.7, request.fuelLiters ?: 0.0, 0.0)
        assertEquals(null, request.mileage)
        assertEquals(
            "Заправка 2026-07-06: AI-95, 35.7 л, 2500 ₽",
            request.description,
        )
    }

    @Test
    fun repairFormValuesMapToRepairEventRequest() {
        val request = HistoryRecordEventMapper.toEventRequest(
            HistoryRecordType.REPAIR,
            mapOf(
                "date" to "2026-07-06",
                "name" to "Замена масла",
                "cost" to "7000",
                "description" to "Фильтр и масло",
            ),
        )

        assertEquals("repair", request.type)
        assertEquals(7000, request.amount)
        assertEquals(null, request.fuelLiters)
        assertEquals(null, request.mileage)
        assertEquals(
            "Ремонт 2026-07-06: Замена масла. Фильтр и масло",
            request.description,
        )
    }

    @Test
    fun maintenanceFormValuesMapToRepairEventRequest() {
        val request = HistoryRecordEventMapper.toEventRequest(
            HistoryRecordType.MAINTENANCE,
            mapOf(
                "date" to "2026-07-06",
                "name" to "ТО-2",
                "cost" to "12000",
                "description" to "",
            ),
        )

        assertEquals("repair", request.type)
        assertEquals(12000, request.amount)
        assertEquals(null, request.fuelLiters)
        assertEquals(null, request.mileage)
        assertEquals("ТО 2026-07-06: ТО-2", request.description)
    }

    @Test
    fun tripOdometerFormValuesMapToTripEventRequest() {
        val request = HistoryRecordEventMapper.toEventRequest(
            HistoryRecordType.TRIP,
            mapOf(
                "date" to "2026-07-06",
                "odometerStart" to "120000",
                "odometerEnd" to "120120",
                "description" to "Дом - аэропорт",
            ),
        )

        assertEquals("trip", request.type)
        assertEquals(null, request.amount)
        assertEquals(null, request.fuelLiters)
        assertEquals(null, request.mileage)
        assertEquals(120000, request.odometerStart)
        assertEquals(120120, request.odometerEnd)
        assertEquals("Поездка 2026-07-06: Дом - аэропорт", request.description)
    }

    @Test
    fun legacyTripMileageFormValuesMapToTripEventRequest() {
        val request = HistoryRecordEventMapper.toEventRequest(
            HistoryRecordType.TRIP,
            mapOf(
                "date" to "2026-07-06",
                "mileage" to "120",
                "description" to "Дом - аэропорт",
            ),
        )

        assertEquals("trip", request.type)
        assertEquals(120, request.mileage)
        assertEquals(null, request.odometerStart)
        assertEquals(null, request.odometerEnd)
        assertEquals("Поездка 2026-07-06: Дом - аэропорт, 120 км", request.description)
    }

    @Test
    fun tripEventUsesBackendTripDistanceFirst() {
        val data = HistoryRecordEventMapper.fromEvent(
            Event(
                id = 1,
                type = "trip",
                description = "Поездка 2026-07-06: Дом - аэропорт",
                amount = 0,
                mileage = 120120,
                odometerStart = 120000,
                odometerEnd = 120150,
                tripDistance = 130,
            ),
        )

        assertEquals(HistoryRecordType.TRIP, data.type)
        assertEquals("130", data.values["mileage"])
        assertEquals("120000", data.values["odometerStart"])
        assertEquals("120150", data.values["odometerEnd"])
        assertEquals("Дом - аэропорт", data.values["description"])
    }

    @Test
    fun tripEventFallsBackToOdometerDistance() {
        val data = HistoryRecordEventMapper.fromEvent(
            Event(
                id = 1,
                type = "trip",
                description = "Поездка 2026-07-06: Дом - аэропорт",
                amount = 0,
                mileage = 120120,
                odometerStart = 120000,
                odometerEnd = 120120,
                tripDistance = null,
            ),
        )

        assertEquals("120", data.values["mileage"])
    }

    @Test
    fun legacyTripEventFallsBackToMileageOverride() {
        val data = HistoryRecordEventMapper.fromEvent(
            Event(
                id = 1,
                type = "trip",
                description = "Поездка 2026-07-06: Дом - аэропорт, 120 км",
                amount = 0,
                mileage = 120120,
                tripDistance = null,
            ),
            tripMileageOverride = 120,
        )

        assertEquals("120", data.values["mileage"])
        assertEquals(null, data.values["odometerStart"])
        assertEquals(null, data.values["odometerEnd"])
        assertEquals("Дом - аэропорт", data.values["description"])
    }
}
