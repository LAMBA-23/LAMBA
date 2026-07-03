package com.lamba.app.network

import org.junit.Assert.assertEquals
import org.junit.Test

class StatsModelTest {

    @Test
    fun periodForReturnsSelectedBackendPeriod() {
        val week = StatsPeriod(mileage = 10, fuelLiters = 5, recordsCount = 2)
        val month = StatsPeriod(mileage = 20, fuelLiters = 8, recordsCount = 4)
        val allTime = StatsPeriod(mileage = 84, fuelLiters = 10, recordsCount = 6)
        val stats = Stats(
            totalRecordedMileage = 84,
            week = week,
            month = month,
            allTime = allTime,
        )

        assertEquals(week, stats.periodFor(StatsPeriodKey.WEEK))
        assertEquals(month, stats.periodFor(StatsPeriodKey.MONTH))
        assertEquals(allTime, stats.periodFor(StatsPeriodKey.ALL_TIME))
    }
}
