package com.lamba.app.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
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
        assertEquals(2500.0, request.amount ?: 0.0, 0.0)
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
                "litres" to "35,75",
                "cost" to "2500.5",
            ),
        )

        assertEquals("fuel", request.type)
        assertEquals(2500.5, request.amount ?: 0.0, 0.0)
        assertEquals(35.75, request.fuelLiters ?: 0.0, 0.0)
        assertEquals(null, request.mileage)
        assertEquals(
            "Заправка 2026-07-06: AI-95, 35,75 л, 2500,5 ₽",
            request.description,
        )
    }

    @Test
    fun repairFormValuesMapDecimalCostToRepairEventRequest() {
        val request = HistoryRecordEventMapper.toEventRequest(
            HistoryRecordType.REPAIR,
            mapOf(
                "date" to "2026-07-06",
                "name" to "Замена масла",
                "cost" to "7000,125",
                "description" to "Фильтр и масло",
            ),
        )

        assertEquals("repair", request.type)
        assertEquals(7000.125, request.amount ?: 0.0, 0.0)
        assertEquals(null, request.fuelLiters)
        assertEquals(null, request.mileage)
        assertEquals(
            "Ремонт 2026-07-06: Замена масла. Фильтр и масло",
            request.description,
        )
    }

    @Test
    fun maintenanceFormValuesMapDecimalCostToRepairEventRequest() {
        val request = HistoryRecordEventMapper.toEventRequest(
            HistoryRecordType.MAINTENANCE,
            mapOf(
                "date" to "2026-07-06",
                "name" to "ТО-2",
                "cost" to "12000.25",
                "description" to "",
            ),
        )

        assertEquals("repair", request.type)
        assertEquals(12000.25, request.amount ?: 0.0, 0.0)
        assertEquals(null, request.fuelLiters)
        assertEquals(null, request.mileage)
        assertEquals("ТО 2026-07-06: ТО-2", request.description)
    }

    @Test
    fun tripFormValuesMapDecimalDistanceToTripEventRequest() {
        val request = HistoryRecordEventMapper.toEventRequest(
            HistoryRecordType.TRIP,
            mapOf(
                "date" to "2026-07-06",
                "mileage" to "120,345",
                "description" to "Дом - аэропорт",
            ),
        )

        assertEquals("trip", request.type)
        assertEquals(null, request.amount)
        assertEquals(null, request.fuelLiters)
        assertEquals(120.345, request.mileage ?: 0.0, 0.0)
        assertEquals("Поездка 2026-07-06: Дом - аэропорт, 120,345 км", request.description)
    }

    @Test
    fun malformedDecimalValuesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            HistoryRecordEventMapper.toEventRequest(
                HistoryRecordType.FUEL,
                mapOf(
                    "date" to "2026-07-06",
                    "fuelType" to "AI-95",
                    "litres" to "12.3456",
                    "cost" to "2500",
                ),
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            HistoryRecordEventMapper.toEventRequest(
                HistoryRecordType.REPAIR,
                mapOf(
                    "date" to "2026-07-06",
                    "name" to "Замена масла",
                    "cost" to "12,5.3",
                    "description" to "",
                ),
            )
        }
    }

    @Test
    fun breakdownFormValuesMapToIssueEventRequest() {
        val request = HistoryRecordEventMapper.toEventRequest(
            HistoryRecordType.BREAKDOWN,
            mapOf(
                "date" to "2026-07-06",
                "name" to "Горит Check Engine",
                "description" to "Индикатор появился после запуска",
            ),
        )

        assertEquals("issue", request.type)
        assertEquals(null, request.amount)
        assertEquals(null, request.fuelLiters)
        assertEquals(null, request.mileage)
        assertEquals(
            "Поломка 2026-07-06: Горит Check Engine. Индикатор появился после запуска",
            request.description,
        )
    }

    @Test
    fun issueEventMapsToBreakdownFormValues() {
        val formData = HistoryRecordEventMapper.fromEvent(
            Event(
                id = 5,
                type = "issue",
                description = "Поломка 2026-07-06: Горит Check Engine. Индикатор появился после запуска",
                amount = 0.0,
                mileage = 0.0,
                createdAt = "2026-07-06T10:00:00",
            ),
        )

        assertEquals(HistoryRecordType.BREAKDOWN, formData.type)
        assertEquals("2026-07-06", formData.values["date"])
        assertEquals("Горит Check Engine", formData.values["name"])
        assertEquals("Индикатор появился после запуска", formData.values["description"])
    }
}
