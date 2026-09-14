package com.nomono.sono.util

import com.nomono.sono.data.DebtType
import com.nomono.sono.data.TransactionKind

data class DebtBalance(val amount: Long, val debtType: DebtType)

fun applyTransaction(
    currentAmount: Long,
    currentDebtType: DebtType,
    kind: TransactionKind,
    txAmount: Long,
): DebtBalance {
    require(currentAmount >= 0) { "Debt amount must not be negative" }
    require(txAmount > 0) { "Transaction amount must be positive" }

    val current = if (currentDebtType == DebtType.THEY_OWE_ME) {
        currentAmount
    } else {
        Math.negateExact(currentAmount)
    }
    val unsignedDelta = if (kind == TransactionKind.PAYMENT) -txAmount else txAmount
    val delta = if (currentDebtType == DebtType.THEY_OWE_ME) unsignedDelta else -unsignedDelta
    val result = Math.addExact(current, delta)

    return when {
        result > 0 -> DebtBalance(result, DebtType.THEY_OWE_ME)
        result < 0 -> DebtBalance(Math.negateExact(result), DebtType.I_OWE_THEM)
        else -> DebtBalance(0, currentDebtType)
    }
}
