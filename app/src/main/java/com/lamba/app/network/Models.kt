package com.lamba.app.network

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("user_id") val userId: Int? = null
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

data class Event(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String,
    @SerializedName("amount") val amount: Int,
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
    @SerializedName("total_recorded_mileage") val totalRecordedMileage: Int = 0
)
