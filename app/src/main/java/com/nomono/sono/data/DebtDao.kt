package com.nomono.sono.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {

    @Query("SELECT * FROM debts ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getById(id: Long): Debt?

    @Insert
    suspend fun insert(debt: Debt): Long

    @Update
    suspend fun update(debt: Debt)

    @Delete
    suspend fun delete(debt: Debt)

    @Query("SELECT * FROM debt_transactions WHERE debtId = :debtId ORDER BY createdAt DESC, id DESC")
    fun observeTransactions(debtId: Long): Flow<List<DebtTransaction>>

    @Query("SELECT * FROM debt_transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): DebtTransaction?

    @Delete
    suspend fun deleteTransaction(transaction: DebtTransaction)

    @Insert
    suspend fun insertTransaction(transaction: DebtTransaction): Long

    @Transaction
    suspend fun recordTransaction(transaction: DebtTransaction, debt: Debt) {
        insertTransaction(transaction)
        update(debt)
    }

    @Query("DELETE FROM debt_transactions WHERE debtId = :debtId")
    suspend fun deleteTransactions(debtId: Long)

    @Transaction
    suspend fun clearDebt(debt: Debt) {
        deleteTransactions(debt.id)
        update(debt)
    }

    @Transaction
    suspend fun deleteDebt(debt: Debt) {
        deleteTransactions(debt.id)
        delete(debt)
    }

    @Transaction
    suspend fun deleteTransactionAndRecalc(transaction: DebtTransaction, debt: Debt) {
        deleteTransaction(transaction)
        update(debt)
    }
}
