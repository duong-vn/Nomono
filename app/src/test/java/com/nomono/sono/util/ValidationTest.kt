package com.nomono.sono.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {

    @Test
    fun name_validation() {
        assertFalse(Validation.isNameValid(""))
        assertFalse(Validation.isNameValid("   "))
        assertTrue(Validation.isNameValid("A"))
        assertTrue(Validation.isNameValid("Nguyễn Văn A"))
    }

    @Test
    fun amount_validation() {
        assertFalse(Validation.isAmountValid(0L))
        assertFalse(Validation.isAmountValid(-5L))
        assertTrue(Validation.isAmountValid(1L))
        assertTrue(Validation.isAmountValid(1000L))
    }
}
