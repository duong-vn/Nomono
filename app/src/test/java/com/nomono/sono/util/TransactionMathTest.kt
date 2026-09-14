package com.nomono.sono.util

import com.nomono.sono.data.DebtType
import com.nomono.sono.data.TransactionKind
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionMathTest {

    @Test
    fun payment_reduces_amount() {
        assertEquals(
            DebtBalance(32L, DebtType.THEY_OWE_ME),
            applyTransaction(50L, DebtType.THEY_OWE_ME, TransactionKind.PAYMENT, 18L),
        )
    }

    @Test
    fun add_increases_amount() {
        assertEquals(
            DebtBalance(68L, DebtType.THEY_OWE_ME),
            applyTransaction(50L, DebtType.THEY_OWE_ME, TransactionKind.ADD, 18L),
        )
    }

    @Test
    fun overpayment_flips_to_i_owe_them() {
        assertEquals(
            DebtBalance(32L, DebtType.I_OWE_THEM),
            applyTransaction(18L, DebtType.THEY_OWE_ME, TransactionKind.PAYMENT, 50L),
        )
    }

    @Test
    fun overpayment_flips_to_they_owe_me() {
        assertEquals(
            DebtBalance(32L, DebtType.THEY_OWE_ME),
            applyTransaction(18L, DebtType.I_OWE_THEM, TransactionKind.PAYMENT, 50L),
        )
    }

    @Test
    fun settlement_keeps_current_direction() {
        assertEquals(
            DebtBalance(0L, DebtType.I_OWE_THEM),
            applyTransaction(18L, DebtType.I_OWE_THEM, TransactionKind.PAYMENT, 18L),
        )
    }
}
