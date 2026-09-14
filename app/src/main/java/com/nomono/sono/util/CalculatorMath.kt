package com.nomono.sono.util

import java.math.BigDecimal
import java.math.RoundingMode

enum class CalculatorOperator(val symbol: String) {
    ADD("+"),
    SUBTRACT("−"),
    MULTIPLY("×"),
    DIVIDE("÷"),
}

sealed interface CalculatorResult {
    data class Value(val value: BigDecimal) : CalculatorResult
    data class Error(val message: String) : CalculatorResult
}

private const val DIVISION_SCALE = 8
private const val MAX_INTEGER_DIGITS = 15
private const val MAX_FRACTION_DIGITS = 8

fun applyCalculatorOperation(
    left: BigDecimal,
    operator: CalculatorOperator,
    right: BigDecimal,
): CalculatorResult {
    if (operator == CalculatorOperator.DIVIDE && right.compareTo(BigDecimal.ZERO) == 0) {
        return CalculatorResult.Error("Không thể chia cho 0")
    }

    val result = runCatching {
        when (operator) {
            CalculatorOperator.ADD -> left.add(right)
            CalculatorOperator.SUBTRACT -> left.subtract(right)
            CalculatorOperator.MULTIPLY -> left.multiply(right)
            CalculatorOperator.DIVIDE -> left.divide(right, DIVISION_SCALE, RoundingMode.HALF_UP)
        }.stripTrailingZeros()
    }.getOrElse {
        return CalculatorResult.Error("Không thể tính kết quả")
    }

    // ponytail: calculator accepts 15 whole digits and 8 fraction digits — raise limits when larger values matter.
    return if (result.integerDigits() > MAX_INTEGER_DIGITS || result.scale() > MAX_FRACTION_DIGITS) {
        CalculatorResult.Error("Số quá lớn")
    } else {
        CalculatorResult.Value(result)
    }
}

fun parseCalculatorInput(input: String): BigDecimal? = input.toBigDecimalOrNull()

fun formatCalculatorValue(value: BigDecimal): String = value.stripTrailingZeros().toPlainString()

private fun BigDecimal.integerDigits(): Int = (precision() - scale()).coerceAtLeast(0)
