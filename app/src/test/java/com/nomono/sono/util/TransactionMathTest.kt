package com.nomono.sono.util

import com.nomono.sono.data.TransactionKind
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionMathTest {

    @Test
    fun payment_reduces_amount() {
        assertEquals(32L, applyTransaction(50L, TransactionKind.PAYMENT, 18L))
    }

    @Test
    fun add_increases_amount() {
        assertEquals(68L, applyTransaction(50L, TransactionKind.ADD, 18L))
    }

    @Test
    fun payment_never_goes_negative() {
        assertEquals(0L, applyTransaction(18L, TransactionKind.PAYMENT, 50L))
    }

    @Test
    fun add_from_zero() {
        assertEquals(100L, applyTransaction(0L, TransactionKind.ADD, 100L))
    }
}
