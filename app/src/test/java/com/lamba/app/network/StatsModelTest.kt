package com.lamba.app.network

import org.junit.Assert.assertEquals
import org.junit.Test

class StatsModelTest {

    @Test
    fun periodForReturnsSelectedBackendPeriod() {
        val week = StatsPeriod(mileage = 10.5, fuelLiters = 5.0, recordsCount = 2)
        val month = StatsPeriod(mileage = 20.25, fuelLiters = 8.5, recordsCount = 4)
        val allTime = StatsPeriod(mileage = 84.75, fuelLiters = 10.0, recordsCount = 6)
        val stats = Stats(
            totalRecordedMileage = 84.75,
            week = week,
            month = month,
            allTime = allTime,
        )

        assertEquals(week, stats.periodFor(StatsPeriodKey.WEEK))
        assertEquals(month, stats.periodFor(StatsPeriodKey.MONTH))
        assertEquals(allTime, stats.periodFor(StatsPeriodKey.ALL_TIME))
    }
}
