package com.nomono.sono.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionKind { PAYMENT, ADD }

@Entity(
    tableName = "debt_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Debt::class,
            parentColumns = ["id"],
            childColumns = ["debtId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("debtId")],
)
data class DebtTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long,
    val amount: Long,
    val kind: TransactionKind,
    val createdAt: Long,
)
