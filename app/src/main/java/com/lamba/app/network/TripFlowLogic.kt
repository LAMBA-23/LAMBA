package com.lamba.app.network

object TripFlowLogic {
    const val START_ACTION_LABEL = "Начать поездку"
    const val FINISH_ACTION_LABEL = "Завершить поездку"
    const val SAVED_MESSAGE = "Поездка сохранена в историю"

    fun actionLabel(activeTrip: ActiveTrip?): String {
        return if (activeTrip == null) START_ACTION_LABEL else FINISH_ACTION_LABEL
    }

    fun validateStartOdometer(input: String): OdometerInputResult {
        return parseOdometer(input)
    }

    fun validateFinishOdometer(input: String, odometerStart: Int): OdometerInputResult {
        val parsed = parseOdometer(input)
        if (parsed is OdometerInputResult.Valid && parsed.value < odometerStart) {
            return OdometerInputResult.Invalid("Пробег в конце не может быть меньше начального")
        }
        return parsed
    }

    fun tripDistance(odometerStart: Int, odometerEnd: Int): Int {
        require(odometerEnd >= odometerStart) { "Odometer end must be greater than or equal to start" }
        return odometerEnd - odometerStart
    }

    fun createTripRequest(odometerStart: Int, odometerEnd: Int, date: String): EventCreateRequest {
        tripDistance(odometerStart, odometerEnd)
        return HistoryRecordEventMapper.toEventRequest(
            HistoryRecordType.TRIP,
            mapOf(
                "date" to date,
                "odometerStart" to odometerStart.toString(),
                "odometerEnd" to odometerEnd.toString(),
                "description" to "",
            ),
        )
    }

    private fun parseOdometer(input: String): OdometerInputResult {
        val value = input.trim()
        if (value.isBlank()) {
            return OdometerInputResult.Invalid("Введите текущий пробег")
        }
        if (!Regex("""\d+""").matches(value)) {
            return OdometerInputResult.Invalid("Введите пробег целым неотрицательным числом")
        }
        return value.toIntOrNull()
            ?.let { OdometerInputResult.Valid(it) }
            ?: OdometerInputResult.Invalid("Введите корректный пробег")
    }
}

data class ActiveTrip(
    val odometerStart: Int,
)

sealed class OdometerInputResult {
    data class Valid(val value: Int) : OdometerInputResult()
    data class Invalid(val message: String) : OdometerInputResult()
}
