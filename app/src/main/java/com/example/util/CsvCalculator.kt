package com.example.util

import java.util.Locale

data class CsvSummary(
    val sum: Double = 0.0,
    val average: Double = 0.0,
    val count: Int = 0,
    val min: Double? = null,
    val max: Double? = null,
    val parsedNumbers: List<Double> = emptyList()
) {
    val formattedSum: String
        get() = if (sum % 1.0 == 0.0) {
            String.format(Locale.US, "%,.0f", sum)
        } else {
            String.format(Locale.US, "%,.2f", sum)
        }

    val formattedAvg: String
        get() = if (count > 0) {
            String.format(Locale.US, "%.2f", average)
        } else {
            "0.00"
        }
}

object CsvCalculator {

    /**
     * Parses comma-separated, newline-separated, or space-separated numbers from a raw string.
     * Tolerates common currency symbols and malformed tokens without crashing.
     */
    fun calculate(rawCsvText: String): CsvSummary {
        if (rawCsvText.isBlank()) {
            return CsvSummary()
        }

        // Split by commas, newlines, semicolons, tabs, and spaces
        val tokens = rawCsvText.split(Regex("[,;\n\r\t]+"))
        val numbers = mutableListOf<Double>()

        for (token in tokens) {
            val cleaned = token.trim()
                .replace("$", "")
                .replace("€", "")
                .replace("£", "")
                .replace("¥", "")
                .replace("₹", "")
                .trim()

            if (cleaned.isNotEmpty()) {
                val num = cleaned.toDoubleOrNull()
                if (num != null && !num.isNaN() && !num.isInfinite()) {
                    numbers.add(num)
                }
            }
        }

        if (numbers.isEmpty()) {
            return CsvSummary()
        }

        val sum = numbers.sum()
        val count = numbers.size
        val avg = sum / count
        val min = numbers.minOrNull()
        val max = numbers.maxOrNull()

        return CsvSummary(
            sum = sum,
            average = avg,
            count = count,
            min = min,
            max = max,
            parsedNumbers = numbers
        )
    }
}
