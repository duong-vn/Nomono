package com.nomono.sono.util

import com.nomono.sono.data.Debt
import com.nomono.sono.data.DebtType
import org.junit.Assert.assertEquals
import org.junit.Test

class DebtTotalsTest {

    private fun debt(name: String, amount: Long, type: DebtType) = Debt(
        id = 0,
        name = name,
        amount = amount,
        debtType = type,
        avatarUri = null,
        createdAt = 0,
        updatedAt = 0,
    )

    @Test
    fun empty_list_is_zero() {
        val totals = computeTotals(emptyList())
        assertEquals(0L, totals.oweMe)
        assertEquals(0L, totals.iOwe)
        assertEquals(0L, totals.net)
    }

    @Test
    fun sums_each_direction() {
        val debts = listOf(
            debt("A", 1_500_000, DebtType.THEY_OWE_ME),
            debt("B", 350_000, DebtType.I_OWE_THEM),
            debt("C", 2_000_000, DebtType.THEY_OWE_ME),
            debt("D", 50_000, DebtType.I_OWE_THEM),
        )
        val totals = computeTotals(debts)
        assertEquals(3_500_000L, totals.oweMe)
        assertEquals(400_000L, totals.iOwe)
        assertEquals(3_100_000L, totals.net)
    }
}
