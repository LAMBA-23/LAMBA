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
    fun toEventRequest(
        type: HistoryRecordType,
        values: Map<String, String>,
    ): EventCreateRequest {
        return when (type) {
            HistoryRecordType.FUEL -> {
                val litres = values["litres"].toIntValue()
                val cost = values["cost"].toIntValue()
                val fuelType = values["fuelType"].orEmpty()
                val date = values["date"].orEmpty()
                EventCreateRequest(
                    type = "fuel",
                    description = "Заправка $date: $fuelType, $litres л, $cost ₽",
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
                    amount = values["cost"].toIntValue(),
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
                    amount = values["cost"].toIntValue(),
                    fuelLiters = null,
                    mileage = null,
                )
            }

            HistoryRecordType.BREAKDOWN -> {
                val name = values["name"].orEmpty()
                val description = values["description"].orEmpty()
                val date = values["date"].orEmpty()
                EventCreateRequest(
                    type = "issue",
                    description = buildRepairDescription(
                        prefix = "Поломка",
                        date = date,
                        name = name,
                        description = description,
                    ),
                    amount = null,
                    fuelLiters = null,
                    mileage = null,
                )
            }

            HistoryRecordType.TRIP -> {
                val date = values["date"].orEmpty()
                val tripDescription = values["description"].orEmpty()
                val mileage = values["mileage"].toIntValue()
                val details = if (tripDescription.isBlank()) {
                    "$mileage км"
                } else {
                    "$tripDescription, $mileage км"
                }
                EventCreateRequest(
                    type = "trip",
                    description = "Поездка $date: $details",
                    amount = null,
                    fuelLiters = null,
                    mileage = mileage,
                )
            }
        }
    }

    fun fromEvent(
        event: Event,
        tripMileageOverride: Int? = null,
    ): HistoryRecordFormData {
        return when {
            event.type == "fuel" -> {
                val parsed = parseFuelDescription(event.description)
                HistoryRecordFormData(
                    type = HistoryRecordType.FUEL,
                    values = mapOf(
                        "date" to parsed["date"].orIfBlank(formatEventDate(event.createdAt)),
                        "fuelType" to parsed["fuelType"].orIfBlank("Не указано"),
                        "litres" to event.fuelLiters.toString(),
                        "cost" to event.amount.toString(),
                    ),
                )
            }

            event.type == "trip" -> {
                val parsed = parseTripDescription(event.description)
                HistoryRecordFormData(
                    type = HistoryRecordType.TRIP,
                    values = mapOf(
                        "date" to parsed["date"].orIfBlank(formatEventDate(event.createdAt)),
                        "mileage" to (tripMileageOverride ?: event.mileage).toString(),
                        "description" to parsed["description"].orEmpty(),
                    ),
                )
            }

            event.type == "repair" && event.description.startsWith("ТО ") -> {
                val parsed = parseRepairDescription(event.description, "ТО")
                HistoryRecordFormData(
                    type = HistoryRecordType.MAINTENANCE,
                    values = mapOf(
                        "name" to parsed["name"].orIfBlank(event.description),
                        "date" to parsed["date"].orIfBlank(formatEventDate(event.createdAt)),
                        "cost" to event.amount.toString(),
                        "description" to parsed["description"].orEmpty(),
                    ),
                )
            }

            event.type == "issue" -> {
                val parsed = parseRepairDescription(event.description, "Поломка")
                HistoryRecordFormData(
                    type = HistoryRecordType.BREAKDOWN,
                    values = mapOf(
                        "name" to parsed["name"].orIfBlank(event.description),
                        "date" to parsed["date"].orIfBlank(formatEventDate(event.createdAt)),
                        "description" to parsed["description"].orEmpty(),
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
                        "cost" to event.amount.toString(),
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
    ): String {
        return if (description.isBlank()) {
            "$prefix $date: $name"
        } else {
            "$prefix $date: $name. $description"
        }
    }

    private fun parseFuelDescription(description: String): Map<String, String> {
        val match = Regex("""^Заправка (.*?): (.*), (\d+) л, (\d+) ₽$""").matchEntire(description)
        if (match == null) {
            return emptyMap()
        }
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
        if (match == null) {
            return emptyMap()
        }
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
            return emptyMap()
        }
        val date = withoutPrefix.substring(0, separatorIndex)
        val details = withoutPrefix.substring(separatorIndex + 2)
        val suffix = Regex(""", (\d+) км$""").find(details)
        if (suffix != null) {
            return mapOf(
                "date" to date,
                "description" to details.removeSuffix(suffix.value),
            )
        }
        return mapOf("date" to date, "description" to "")
    }

    private fun String?.toIntValue(): Int {
        return this.orEmpty().replace(" ", "").replace(',', '.').toDouble().toInt()
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
