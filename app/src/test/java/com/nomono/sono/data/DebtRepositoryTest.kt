package com.nomono.sono.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DebtRepositoryTest {

    @Test
    fun clear_keeps_person_at_zero_and_removes_transactions() = runTest {
        val debt = debt(amount = 250_000)
        val dao = FakeDebtDao(debt)

        DebtRepository(dao).clear(debt.id)

        assertEquals(0L, dao.debt?.amount)
        assertFalse(dao.hasTransactions)
        assertEquals(0, dao.deletedDebts)
    }

    @Test
    fun delete_person_removes_person_and_transactions() = runTest {
        val debt = debt()
        val dao = FakeDebtDao(debt)

        DebtRepository(dao).delete(debt.id)

        assertEquals(null, dao.debt)
        assertFalse(dao.hasTransactions)
        assertEquals(1, dao.deletedDebts)
    }

    @Test
    fun overpayment_flips_debt_direction() = runTest {
        val debt = debt(amount = 100_000)
        val dao = FakeDebtDao(debt)

        DebtRepository(dao).recordTransaction(debt.id, 150_000, TransactionKind.PAYMENT)

        assertEquals(50_000L, dao.debt?.amount)
        assertEquals(DebtType.I_OWE_THEM, dao.debt?.debtType)
    }

    private fun debt(amount: Long = 100_000) = Debt(
        id = 1,
        name = "An",
        amount = amount,
        debtType = DebtType.THEY_OWE_ME,
        avatarUri = null,
        createdAt = 1,
        updatedAt = 1,
    )

    private class FakeDebtDao(initialDebt: Debt) : DebtDao {
        var debt: Debt? = initialDebt
        var hasTransactions = true
        var deletedDebts = 0

        override fun observeAll(): Flow<List<Debt>> = emptyFlow()

        override suspend fun getById(id: Long): Debt? = debt

        override suspend fun insert(debt: Debt): Long = debt.id

        override suspend fun update(debt: Debt) {
            this.debt = debt
        }

        override suspend fun delete(debt: Debt) {
            this.debt = null
            deletedDebts++
        }

        override fun observeTransactions(debtId: Long): Flow<List<DebtTransaction>> = emptyFlow()

        override suspend fun insertTransaction(transaction: DebtTransaction): Long = transaction.id

        override suspend fun recordTransaction(transaction: DebtTransaction, debt: Debt) {
            update(debt)
        }

        override suspend fun deleteTransactions(debtId: Long) {
            hasTransactions = false
        }
    }
}
