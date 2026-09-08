package com.nomono.sono.data

import com.nomono.sono.util.applyTransaction
import kotlinx.coroutines.flow.Flow

class DebtRepository(private val dao: DebtDao) {

    fun observeAll(): Flow<List<Debt>> = dao.observeAll()

    fun observeTransactions(debtId: Long): Flow<List<DebtTransaction>> =
        dao.observeTransactions(debtId)

    suspend fun add(name: String, amount: Long, debtType: DebtType, avatarUri: String?) {
        val now = System.currentTimeMillis()
        dao.insert(
            Debt(
                name = name,
                amount = amount,
                debtType = debtType,
                avatarUri = avatarUri,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun update(id: Long, name: String, amount: Long, debtType: DebtType, avatarUri: String?) {
        val existing = dao.getById(id) ?: return
        dao.update(
            existing.copy(
                name = name,
                amount = amount,
                debtType = debtType,
                avatarUri = avatarUri,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun recordTransaction(debtId: Long, amount: Long, kind: TransactionKind) {
        val debt = dao.getById(debtId) ?: return
        val now = System.currentTimeMillis()
        dao.insertTransaction(
            DebtTransaction(
                debtId = debtId,
                amount = amount,
                kind = kind,
                createdAt = now,
            ),
        )
        dao.update(
            debt.copy(
                amount = applyTransaction(debt.amount, kind, amount),
                updatedAt = now,
            ),
        )
    }

    suspend fun clear(id: Long) {
        val existing = dao.getById(id) ?: return
        dao.deleteTransactions(id)
        dao.update(existing.copy(amount = 0, updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(id: Long) {
        val existing = dao.getById(id) ?: return
        dao.deleteTransactions(id)
        dao.delete(existing)
    }
}
