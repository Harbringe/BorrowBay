package com.example.borrowbay.core.util

import java.util.Locale
import kotlin.math.abs

object FormattingUtils {

    fun formatCurrency(amount: Double): String {
        return when {
            amount >= 1_000_000_000 -> String.format(Locale.ROOT, "%.1fB", amount / 1_000_000_000.0)
            amount >= 1_000_000 -> String.format(Locale.ROOT, "%.1fM", amount / 1_000_000.0)
            amount >= 1_000 -> String.format(Locale.ROOT, "%.1fK", amount / 1_000.0)
            else -> amount.toInt().toString()
        }.replace(".0", "")
    }

    fun formatDistance(distance: Double): String {
        return when {
            distance >= 1000 -> String.format(Locale.ROOT, "%.0f km", distance)
            distance >= 10 -> String.format(Locale.ROOT, "%.1f km", distance)
            else -> String.format(Locale.ROOT, "%.2f km", distance)
        }
    }

    fun formatName(name: String, maxLength: Int = 15): String {
        return if (name.length > maxLength) {
            name.take(maxLength - 3) + "..."
        } else {
            name
        }
    }
}
