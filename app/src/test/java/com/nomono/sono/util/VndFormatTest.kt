package com.nomono.sono.util

import org.junit.Assert.assertEquals
import org.junit.Test

class VndFormatTest {

    @Test
    fun format_zero() {
        assertEquals("0 ₫", VndFormat.format(0))
    }

    @Test
    fun format_small() {
        assertEquals("50 ₫", VndFormat.format(50))
        assertEquals("350 ₫", VndFormat.format(350))
    }

    @Test
    fun format_thousands() {
        assertEquals("1.500 ₫", VndFormat.format(1500))
        assertEquals("12.000 ₫", VndFormat.format(12000))
    }

    @Test
    fun format_millions() {
        assertEquals("1.500.000 ₫", VndFormat.format(1500000))
        assertEquals("12.000.000 ₫", VndFormat.format(12000000))
    }

    @Test
    fun format_large() {
        assertEquals("1.234.567.890.123 ₫", VndFormat.format(1234567890123))
    }

    @Test
    fun format_max_long() {
        assertEquals("9.223.372.036.854.775.807 ₫", VndFormat.format(Long.MAX_VALUE))
    }

    @Test
    fun parseDigits_grouped() {
        assertEquals(1500000L, VndFormat.parseDigits("1.500.000"))
    }

    @Test
    fun parseDigits_ignores_non_digits() {
        assertEquals(0L, VndFormat.parseDigits("abc"))
        assertEquals(0L, VndFormat.parseDigits(""))
        assertEquals(123L, VndFormat.parseDigits("1a2b3"))
    }
}
