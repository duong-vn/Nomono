package com.nomono.sono.ui.home

import com.nomono.sono.data.Debt
import com.nomono.sono.data.DebtType
import org.junit.Assert.assertEquals
import org.junit.Test

class DebtFilterTest {

    private val debts = listOf(
        debt("An", 200_000, DebtType.THEY_OWE_ME),
        debt("Bình", 150_000, DebtType.I_OWE_THEM),
        debt("Chi", 0, DebtType.THEY_OWE_ME),
    )

    @Test
    fun filter_they_owe_me_excludes_settled_debts() {
        assertEquals(listOf("An"), debts.filterFor(DebtFilter.THEY_OWE_ME).map { it.name })
    }

    @Test
    fun filter_i_owe_them_excludes_settled_debts() {
        assertEquals(listOf("Bình"), debts.filterFor(DebtFilter.I_OWE_THEM).map { it.name })
    }

    @Test
    fun filter_settled_keeps_only_zero_balance() {
        assertEquals(listOf("Chi"), debts.filterFor(DebtFilter.SETTLED).map { it.name })
    }

    private fun debt(name: String, amount: Long, type: DebtType) = Debt(
        id = name.hashCode().toLong(),
        name = name,
        amount = amount,
        debtType = type,
        avatarUri = null,
        createdAt = 0,
        updatedAt = 0,
    )
}
