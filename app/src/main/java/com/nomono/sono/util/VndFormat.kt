package com.nomono.sono.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

object VndFormat {

    private val formatter: DecimalFormat = DecimalFormat(
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

    fun format(amount: Long): String = formatter.format(amount) + " ₫"

    fun parseDigits(input: String): Long = input.filter(Char::isDigit).toLongOrNull() ?: 0L
}
