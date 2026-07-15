package com.lamba.app.network

import com.lamba.app.R

enum class HistoryRecordType(
    val title: String,
    val formTitle: String,
    val iconRes: Int,
) {
    FUEL("Заправка", "Новая заправка", R.drawable.ic_lamba_fuel),
    MAINTENANCE("ТО", "Новое ТО", R.drawable.ic_lamba_build),
    REPAIR("Ремонт", "Новый ремонт", R.drawable.ic_lamba_repair),
    BREAKDOWN("Поломка", "Новая поломка", R.drawable.ic_lamba_issue),
    TRIP("Поездка", "Новая поездка", R.drawable.ic_lamba_route),
}

data class HistoryRecordFormData(
    val type: HistoryRecordType,
    val values: Map<String, String>,
)

object HistoryRecordEventMapper {
    private const val PHOTO_MARKER = "\nPhoto: "

    fun toEventRequest(
        type: HistoryRecordType,
        values: Map<String, String>,
    ): EventCreateRequest {
        return when (type) {
            HistoryRecordType.FUEL -> {
                val litres = values["litres"].toDecimalValue()
                val cost = values["cost"].toDecimalValue()
                val fuelType = values["fuelType"].orEmpty()
                val date = values["date"].orEmpty()
                EventCreateRequest(
                    type = "fuel",
                    description = "Заправка $date: $fuelType, ${DecimalNumberUtils.formatDecimal(litres)} л, ${DecimalNumberUtils.formatDecimal(cost)} ₽",
                    amount = cost,
                    fuelLiters = litres,
                    mileage = null,
                )
            }

            HistoryRecordType.MAINTENANCE -> {
                val name = values["name"].orEmpty()
                val description = values["description"].orEmpty()
                val date = values["date"].orEmpty()
                EventCreateRequest(
                    type = "repair",
                    description = buildRepairDescription(
                        prefix = "ТО",
                        date = date,
                        name = name,
                        description = description,
                    ),
                    amount = values["cost"].toDecimalValue(),
                    fuelLiters = null,
                    mileage = null,
                )
            }

            HistoryRecordType.REPAIR -> {
                val name = values["name"].orEmpty()
                val description = values["description"].orEmpty()
                val date = values["date"].orEmpty()
                EventCreateRequest(
                    type = "repair",
                    description = buildRepairDescription(
                        prefix = "Ремонт",
                        date = date,
                        name = name,
                        description = description,
                    ),
                    amount = values["cost"].toDecimalValue(),
                    fuelLiters = null,
                    mileage = null,
                )
            }

            HistoryRecordType.BREAKDOWN -> {
                val name = values["name"].orEmpty()
                val description = values["description"].orEmpty()
                val date = values["date"].orEmpty()
                val photoUri = values["photoUri"].orEmpty()
                EventCreateRequest(
                    type = "issue",
                    description = buildRepairDescription(
                        prefix = "Поломка",
                        date = date,
                        name = name,
                        description = description,
                        photoUri = photoUri,
                    ),
                    amount = null,
                    fuelLiters = null,
                    mileage = null,
                )
            }

            HistoryRecordType.TRIP -> {
                val date = values["date"].orEmpty()
                val tripDescription = values["description"].orEmpty()
                val odometerStart = values["odometerStart"].toOdometerValue()
                val odometerEnd = values["odometerEnd"].toOdometerValue()
                if (odometerEnd < odometerStart) {
                    throw IllegalArgumentException("Invalid odometer range")
                }
                val details = tripDescription
                val description = if (details.isBlank()) {
                    "Поездка $date"
                } else {
                    "Поездка $date: $details"
                }
                EventCreateRequest(
                    type = "trip",
                    description = description,
                    amount = null,
                    fuelLiters = null,
                    mileage = null,
                    odometerStart = odometerStart,
                    odometerEnd = odometerEnd,
                )
            }
        }
    }

    fun fromEvent(
        event: Event,
        tripMileageOverride: Double? = null,
    ): HistoryRecordFormData {
        return when {
            event.type == "fuel" -> {
                val parsed = parseFuelDescription(event.description)
                HistoryRecordFormData(
                    type = HistoryRecordType.FUEL,
                    values = mapOf(
                        "date" to parsed["date"].orIfBlank(formatEventDate(event.createdAt)),
                        "fuelType" to parsed["fuelType"].orIfBlank("Не указано"),
                        "litres" to DecimalNumberUtils.formatDecimal(event.fuelLiters),
                        "cost" to DecimalNumberUtils.formatDecimal(event.amount),
                    ),
                )
            }

            event.type == "trip" -> {
                val parsed = parseTripDescription(event.description)
                val tripDistance = event.tripDistance?.toDouble()
                    ?: calculateTripDistance(event.odometerStart, event.odometerEnd)
                    ?: tripMileageOverride
                    ?: event.mileage
                val values = mutableMapOf(
                    "date" to parsed["date"].orIfBlank(formatEventDate(event.createdAt)),
                    "mileage" to DecimalNumberUtils.formatDecimal(tripDistance),
                    "description" to parsed["description"].orEmpty(),
                )
                event.odometerStart?.let { values["odometerStart"] = it.toString() }
                    ?: parsed["odometerStart"]?.takeIf { it.isNotBlank() }?.let { values["odometerStart"] = it }
                event.odometerEnd?.let { values["odometerEnd"] = it.toString() }
                    ?: parsed["odometerEnd"]?.takeIf { it.isNotBlank() }?.let { values["odometerEnd"] = it }
                HistoryRecordFormData(
                    type = HistoryRecordType.TRIP,
                    values = values,
                )
            }

            event.type == "repair" && event.description.startsWith("ТО ") -> {
                val parsed = parseRepairDescription(event.description, "ТО")
                HistoryRecordFormData(
                    type = HistoryRecordType.MAINTENANCE,
                    values = mapOf(
                        "name" to parsed["name"].orIfBlank(event.description),
                        "date" to parsed["date"].orIfBlank(formatEventDate(event.createdAt)),
                        "cost" to DecimalNumberUtils.formatDecimal(event.amount),
                        "description" to parsed["description"].orEmpty(),
                    ),
                )
            }

            event.type == "issue" -> {
                val parsed = parseRepairDescription(stripPhotoUri(event.description), "Поломка")
                HistoryRecordFormData(
                    type = HistoryRecordType.BREAKDOWN,
                    values = mapOf(
                        "name" to parsed["name"].orIfBlank(event.description),
                        "date" to parsed["date"].orIfBlank(formatEventDate(event.createdAt)),
                        "description" to parsed["description"].orEmpty(),
                        "photoUri" to extractPhotoUri(event.description).orEmpty(),
                    ),
                )
            }

            else -> {
                val parsed = parseRepairDescription(event.description, "Ремонт")
                HistoryRecordFormData(
                    type = HistoryRecordType.REPAIR,
                    values = mapOf(
                        "name" to parsed["name"].orIfBlank(event.description),
                        "date" to parsed["date"].orIfBlank(formatEventDate(event.createdAt)),
                        "cost" to DecimalNumberUtils.formatDecimal(event.amount),
                        "description" to parsed["description"].orEmpty(),
                    ),
                )
            }
        }
    }

    private fun buildRepairDescription(
        prefix: String,
        date: String,
        name: String,
        description: String,
        photoUri: String = "",
    ): String {
        val text = if (description.isBlank()) {
            "$prefix $date: $name"
        } else {
            "$prefix $date: $name. $description"
        }
        return if (photoUri.isBlank()) text else text + PHOTO_MARKER + photoUri
    }

    private fun parseFuelDescription(description: String): Map<String, String> {
        val match = Regex("""^Заправка (.*?): (.*), (\d+(?:[.,]\d{1,3})?) л, (\d+(?:[.,]\d{1,3})?) ₽$""").matchEntire(description)
            ?: return emptyMap()
        return mapOf(
            "date" to match.groupValues[1],
            "fuelType" to match.groupValues[2],
        )
    }

    private fun parseRepairDescription(
        description: String,
        prefix: String,
    ): Map<String, String> {
        val match = Regex("^$prefix (.*?): (.*?)(?:\\. (.*))?$").matchEntire(description)
            ?: return emptyMap()
        return mapOf(
            "date" to match.groupValues[1],
            "name" to match.groupValues[2],
            "description" to match.groupValues.getOrElse(3) { "" },
        )
    }

    private fun parseTripDescription(description: String): Map<String, String> {
        val prefix = "Поездка "
        if (!description.startsWith(prefix)) {
            return emptyMap()
        }
        val withoutPrefix = description.removePrefix(prefix)
        val separatorIndex = withoutPrefix.indexOf(": ")
        if (separatorIndex <= 0) {
            return mapOf("date" to withoutPrefix.trim(), "description" to "")
        }
        val date = withoutPrefix.substring(0, separatorIndex)
        val details = withoutPrefix.substring(separatorIndex + 2)
        val suffix = Regex(""", (\d+(?:[.,]\d{1,3})?) км$""").find(details)
        if (suffix != null) {
            val detailsWithoutDistance = details.removeSuffix(suffix.value)
            val odometerMatch = Regex("""(?:^|, )(\d+)-(\d+)$""").find(detailsWithoutDistance)
            val routeDescription = if (odometerMatch == null) {
                detailsWithoutDistance
            } else {
                detailsWithoutDistance
                    .removeSuffix(odometerMatch.value)
                    .removeSuffix(", ")
            }
            return mapOf(
                "date" to date,
                "description" to routeDescription,
                "odometerStart" to (odometerMatch?.groupValues?.getOrNull(1) ?: ""),
                "odometerEnd" to (odometerMatch?.groupValues?.getOrNull(2) ?: ""),
            )
        }
        return mapOf("date" to date, "description" to details)
    }

    private fun String?.toOptionalIntValue(): Int? {
        val decimal = toOptionalDecimalValue() ?: return null
        return decimal.toInt()
    }

    private fun String?.toOptionalDecimalValue(): Double? {
        val value = this.orEmpty().trim()
        return if (value.isBlank()) {
            null
        } else {
            DecimalNumberUtils.parsePositiveDecimal(value)
                ?: throw IllegalArgumentException("Invalid decimal value")
        }
    }

    private fun calculateTripDistance(odometerStart: Int?, odometerEnd: Int?): Double? {
        if (odometerStart == null || odometerEnd == null) {
            return null
        }
        return (odometerEnd - odometerStart).takeIf { it >= 0 }?.toDouble()
    }

    private fun String?.toDecimalValue(): Double {
        return DecimalNumberUtils.parsePositiveDecimal(this.orEmpty())
            ?: throw IllegalArgumentException("Invalid decimal value")
    }

    private fun String?.toOdometerValue(): Int {
        val value = this.orEmpty().trim()
        if (!Regex("""\d+""").matches(value)) {
            throw IllegalArgumentException("Invalid odometer value")
        }
        return value.toInt()
    }

    fun extractPhotoUri(description: String): String? {
        return description.substringAfter(PHOTO_MARKER, missingDelimiterValue = "")
            .ifBlank { null }
    }

    private fun stripPhotoUri(description: String): String {
        return description.substringBefore(PHOTO_MARKER)
    }

    private fun String?.orIfBlank(fallback: String): String {
        val value = this.orEmpty().trim()
        return if (value.isBlank()) fallback else value
    }

    private fun formatEventDate(createdAt: String?): String {
        if (createdAt.isNullOrBlank()) {
            return ""
        }
        return createdAt.substringBefore("T")
    }
}
