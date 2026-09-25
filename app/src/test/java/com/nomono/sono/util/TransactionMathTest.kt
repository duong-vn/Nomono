package com.nomono.sono.util

import com.nomono.sono.data.DebtTransaction
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

    @Test
    fun reverse_payment_restores_amount() {
        val delta = transactionDelta(DebtType.THEY_OWE_ME, TransactionKind.PAYMENT, 20L)
        assertEquals(
            DebtBalance(50L, DebtType.THEY_OWE_ME),
            reverseTransaction(30L, DebtType.THEY_OWE_ME, delta),
        )
    }

    @Test
    fun reverse_after_overpayment_flip_restores_signed_balance() {
        // Họ nợ 50, trả 80 → tôi nợ họ 30; xóa giao dịch trả phải về họ nợ 50.
        val delta = transactionDelta(DebtType.THEY_OWE_ME, TransactionKind.PAYMENT, 80L)
        assertEquals(
            DebtBalance(50L, DebtType.THEY_OWE_ME),
            reverseTransaction(30L, DebtType.I_OWE_THEM, delta),
        )
    }

    @Test
    fun reverse_legacy_transaction_falls_back_to_inverse_kind() {
        val legacy = DebtTransaction(
            id = 1,
            debtId = 1,
            amount = 20L,
            kind = TransactionKind.PAYMENT,
            createdAt = 0,
            signedDelta = 0,
        )
        assertEquals(
            DebtBalance(50L, DebtType.THEY_OWE_ME),
            reverseStoredTransaction(30L, DebtType.THEY_OWE_ME, legacy),
        )
    }
}
