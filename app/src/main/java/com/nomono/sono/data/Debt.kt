package com.nomono.sono.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DebtType {
    THEY_OWE_ME,
    I_OWE_THEM,
}

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Long,
    val debtType: DebtType,
    val avatarUri: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
