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
    fun tripFormValuesMapToTripEventRequest() {
        val request = HistoryRecordEventMapper.toEventRequest(
            HistoryRecordType.TRIP,
            mapOf(
                "date" to "2026-07-06",
                "mileage" to "120",
                "description" to "Дом - аэропорт",
            ),
        )

        assertEquals("trip", request.type)
        assertEquals(null, request.amount)
        assertEquals(null, request.fuelLiters)
        assertEquals(120, request.mileage)
        assertEquals("Поездка 2026-07-06: Дом - аэропорт, 120 км", request.description)
    }
}
