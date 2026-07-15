package com.lamba.app.network

import java.math.BigDecimal
import java.math.RoundingMode

object DecimalNumberUtils {
    private val decimalPattern = Regex("""^\d+(?:[.,]\d{1,3})?$""")

    fun parsePositiveDecimal(value: String): Double? {
        val normalized = normalize(value)
        if (!decimalPattern.matches(normalized)) return null
        return normalized.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 }
    }

    fun formatDecimal(value: Double): String {
        val decimal = BigDecimal.valueOf(value)
            .setScale(3, RoundingMode.HALF_UP)
            .stripTrailingZeros()
        return decimal.toPlainString().replace('.', ',')
    }

    fun formatDecimal(value: String): String {
        val parsed = parsePositiveDecimal(value) ?: return value
        return formatDecimal(parsed)
    }

    fun formatMoney(value: Double): String = "${formatDecimal(value)} ₽"

    fun formatLitres(value: Double): String = "${formatDecimal(value)} л"

    fun formatKilometers(value: Double): String = "${formatDecimal(value)} км"

    fun normalize(value: String): String = value.trim().replace(" ", "")
}
