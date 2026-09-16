package com.nomono.sono.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

object VndFormat {

    // DecimalFormat is not thread-safe; confine one instance per thread so totals
    // can safely move to a background dispatcher.
    private val formatterHolder = ThreadLocal.withInitial {
        DecimalFormat(
            "#,##0",
            DecimalFormatSymbols.getInstance().apply {
                groupingSeparator = '.'
                decimalSeparator = ','
            },
        ).apply {
            isGroupingUsed = true
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }

    fun format(amount: Long): String = formatterHolder.get().format(amount) + " ₫"

    // Single pass, no intermediate filtered String (called on every keystroke).
    fun parseDigits(input: String): Long {
        var result = 0L
        for (c in input) {
            if (c in '0'..'9') {
                result = result * 10 + (c - '0')
                if (result > 999_999_999_999_999L) return 999_999_999_999_999L
            }
        }
        return result
    }
}
