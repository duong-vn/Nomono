package com.nomono.sono.util

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorMathTest {

    @Test
    fun applies_basic_operations() {
        assertValue("7", applyCalculatorOperation(BigDecimal("3"), CalculatorOperator.ADD, BigDecimal("4")))
        assertValue("2", applyCalculatorOperation(BigDecimal("5"), CalculatorOperator.SUBTRACT, BigDecimal("3")))
        assertValue("12", applyCalculatorOperation(BigDecimal("3"), CalculatorOperator.MULTIPLY, BigDecimal("4")))
        assertValue("2.5", applyCalculatorOperation(BigDecimal("10"), CalculatorOperator.DIVIDE, BigDecimal("4")))
    }

    @Test
    fun division_rounds_to_eight_fraction_digits() {
        assertValue("0.33333333", applyCalculatorOperation(BigDecimal.ONE, CalculatorOperator.DIVIDE, BigDecimal("3")))
    }

    @Test
    fun handles_negative_values() {
        assertValue("-3.5", applyCalculatorOperation(BigDecimal("-7"), CalculatorOperator.DIVIDE, BigDecimal("2")))
    }

    @Test
    fun rejects_division_by_zero() {
        assertEquals(
            CalculatorResult.Error("Không thể chia cho 0"),
            applyCalculatorOperation(BigDecimal.ONE, CalculatorOperator.DIVIDE, BigDecimal.ZERO),
        )
    }

    @Test
    fun rejects_result_beyond_display_limit() {
        assertEquals(
            CalculatorResult.Error("Số quá lớn"),
            applyCalculatorOperation(BigDecimal("999999999999999"), CalculatorOperator.MULTIPLY, BigDecimal.TEN),
        )
    }

    private fun assertValue(expected: String, result: CalculatorResult) {
        assertEquals(CalculatorResult.Value(BigDecimal(expected)), result)
    }
}
