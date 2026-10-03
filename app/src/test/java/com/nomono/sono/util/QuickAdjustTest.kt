package com.nomono.sono.util

import com.nomono.sono.data.DebtType
import com.nomono.sono.data.TransactionKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuickAdjustTest {

    @Test
    fun positiveDelta_theyOweMe_createsAddTransaction() {
        val result = QuickAdjust.calculateAdjustment(DebtType.THEY_OWE_ME, 10_000L)
        assertEquals(Pair(10_000L, TransactionKind.ADD), result)
    }

    @Test
    fun positiveDelta_iOweThem_createsPaymentTransaction() {
        val result = QuickAdjust.calculateAdjustment(DebtType.I_OWE_THEM, 10_000L)
        assertEquals(Pair(10_000L, TransactionKind.PAYMENT), result)
    }

    @Test
    fun negativeDelta_theyOweMe_createsPaymentTransaction() {
        val result = QuickAdjust.calculateAdjustment(DebtType.THEY_OWE_ME, -10_000L)
        assertEquals(Pair(10_000L, TransactionKind.PAYMENT), result)
    }

    @Test
    fun negativeDelta_iOweThem_createsAddTransaction() {
        val result = QuickAdjust.calculateAdjustment(DebtType.I_OWE_THEM, -10_000L)
        assertEquals(Pair(10_000L, TransactionKind.ADD), result)
    }

    @Test
    fun zeroDelta_returnsNull() {
        val result = QuickAdjust.calculateAdjustment(DebtType.THEY_OWE_ME, 0L)
        assertNull(result)
    }

    @Test
    fun overpayment_flipsDirection_endToEndSimulation() {
        // Họ nợ tôi 5.000, bấm -10.000 -> Tôi nợ họ 5.000
        val adj1 = QuickAdjust.calculateAdjustment(DebtType.THEY_OWE_ME, -10_000L)!!
        val balance1 = applyTransaction(5_000L, DebtType.THEY_OWE_ME, adj1.second, adj1.first)
        assertEquals(DebtBalance(5_000L, DebtType.I_OWE_THEM), balance1)

        // Đang là 0đ (THEY_OWE_ME), bấm -10.000 -> Tôi nợ họ 10.000
        val adj2 = QuickAdjust.calculateAdjustment(DebtType.THEY_OWE_ME, -10_000L)!!
        val balance2 = applyTransaction(0L, DebtType.THEY_OWE_ME, adj2.second, adj2.first)
        assertEquals(DebtBalance(10_000L, DebtType.I_OWE_THEM), balance2)

        // Đang nợ họ 10.000 (I_OWE_THEM), bấm +10.000 -> về 0đ
        val adj3 = QuickAdjust.calculateAdjustment(DebtType.I_OWE_THEM, 10_000L)!!
        val balance3 = applyTransaction(10_000L, DebtType.I_OWE_THEM, adj3.second, adj3.first)
        assertEquals(DebtBalance(0L, DebtType.I_OWE_THEM), balance3)

        // Đang 0đ (I_OWE_THEM), bấm +10.000 -> Họ nợ tôi 10.000
        val adj4 = QuickAdjust.calculateAdjustment(DebtType.I_OWE_THEM, 10_000L)!!
        val balance4 = applyTransaction(0L, DebtType.I_OWE_THEM, adj4.second, adj4.first)
        assertEquals(DebtBalance(10_000L, DebtType.THEY_OWE_ME), balance4)
    }
}
