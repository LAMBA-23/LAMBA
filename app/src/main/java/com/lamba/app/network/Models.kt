package com.lamba.app.network

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("name") val name: String? = null
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class RegisterResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("name") val name: String? = null
)

data class Vehicle(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("brand") val brand: String,
    @SerializedName("model") val model: String,
    @SerializedName("production_year") val productionYear: Int,
    @SerializedName("current_mileage") val currentMileage: Int,
    @SerializedName("created_at") val createdAt: String? = null
)

data class VehicleRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("brand") val brand: String,
    @SerializedName("model") val model: String,
    @SerializedName("production_year") val productionYear: Int,
    @SerializedName("current_mileage") val currentMileage: Int
)

data class ChatParseRequest(
    @SerializedName("message") val message: String
)

data class ParsedEventPayload(
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String,
    @SerializedName("amount") val amount: Int? = null,
    @SerializedName("fuel_liters") val fuelLiters: Double? = null,
    @SerializedName("mileage") val mileage: Int? = null
)

data class ChatParseResponse(
    @SerializedName("status") val status: String,
    @SerializedName("parsed_event") val parsedEvent: ParsedEventPayload? = null,
    @SerializedName("clarification_question") val clarificationQuestion: String? = null
)

data class EventCreateRequest(
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String,
    @SerializedName("amount") val amount: Int? = null,
    @SerializedName("fuel_liters") val fuelLiters: Double? = null,
    @SerializedName("mileage") val mileage: Int? = null
)

data class Event(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("fuel_liters") val fuelLiters: Double = 0.0,
    @SerializedName("mileage") val mileage: Int,
    @SerializedName("created_at") val createdAt: String? = null
) {
    companion object {
        fun mapUiTypeToBackend(uiType: String): String {
            return when (uiType.trim().lowercase()) {
                "заправка", "топливо", "бензин", "fuel" -> "fuel"
                "ремонт", "сервис", "repair" -> "repair"
                "поездка", "trip" -> "trip"
                "проблема", "поломка", "issue" -> "issue"
                else -> "issue"
            }
        }
    }
}

data class Stats(
    @SerializedName("fuel_expenses") val fuelExpenses: Int = 0,
    @SerializedName("repair_expenses") val repairExpenses: Int = 0,
    @SerializedName("trip_count") val tripCount: Int = 0,
    @SerializedName("total_recorded_mileage") val totalRecordedMileage: Int = 0,
    @SerializedName("week") val week: StatsPeriod = StatsPeriod(),
    @SerializedName("month") val month: StatsPeriod = StatsPeriod(),
    @SerializedName("all_time") val allTime: StatsPeriod = StatsPeriod()
) {
    fun periodFor(period: StatsPeriodKey): StatsPeriod {
        return when (period) {
            StatsPeriodKey.WEEK -> week
            StatsPeriodKey.MONTH -> month
            StatsPeriodKey.ALL_TIME -> allTime
        }
    }
}

data class StatsPeriod(
    @SerializedName("mileage") val mileage: Int = 0,
    @SerializedName("total_expenses") val totalExpenses: Int = 0,
    @SerializedName("fuel_expenses") val fuelExpenses: Int = 0,
    @SerializedName("repair_expenses") val repairExpenses: Int = 0,
    @SerializedName("records_count") val recordsCount: Int = 0,
    @SerializedName("avg_fuel_consumption") val avgFuelConsumption: Int = 0,
    @SerializedName("avg_expense_consumption") val avgExpenseConsumption: Int = 0,
    @SerializedName("mileage_km") val mileageKm: Int = 0,
    @SerializedName("expenses_rub") val expensesRub: Int = 0,
    @SerializedName("fuel_liters") val fuelLiters: Double = 0.0,
    @SerializedName("avg_fuel_consumption_l_per_100km") val avgFuelConsumptionLPer100Km: Int = 0
)

enum class StatsPeriodKey {
    WEEK,
    MONTH,
    ALL_TIME,
}

data class ChatAskRequest(
    @SerializedName("message") val message: String,
    @SerializedName("chat_context") val chatContext: List<ChatContextMessage> = emptyList(),
)

data class ChatAskResponse(
    @SerializedName("answer") val answer: String
)

data class ChatContextMessage(
    @SerializedName("sender") val sender: String,
    @SerializedName("text") val text: String,
)

data class ChatTitleRequest(
    @SerializedName("first_user_message") val firstUserMessage: String,
    @SerializedName("first_assistant_reply") val firstAssistantReply: String,
)

data class ChatTitleResponse(
    @SerializedName("title") val title: String,
)
