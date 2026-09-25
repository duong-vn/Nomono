package com.nomono.sono.util

import com.nomono.sono.data.DebtTransaction
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

    val current = toSigned(currentAmount, currentDebtType)
    val delta = transactionDelta(currentDebtType, kind, txAmount)
    val result = Math.addExact(current, delta)

    return fromSigned(result, currentDebtType)
}

/** Số dư có dấu: dương = họ nợ tôi, âm = tôi nợ họ. */
fun toSigned(amount: Long, debtType: DebtType): Long {
    require(amount >= 0) { "Debt amount must not be negative" }
    return if (debtType == DebtType.THEY_OWE_ME) amount else Math.negateExact(amount)
}

fun fromSigned(signed: Long, fallbackType: DebtType): DebtBalance = when {
    signed > 0 -> DebtBalance(signed, DebtType.THEY_OWE_ME)
    signed < 0 -> DebtBalance(Math.negateExact(signed), DebtType.I_OWE_THEM)
    else -> DebtBalance(0, fallbackType)
}

/**
 * Delta đã áp vào số dư có dấu khi ghi nhận giao dịch.
 * Lưu kèm mỗi dòng để xóa giao dịch sau này trừ đúng, kể cả khi
 * hướng nợ đã đảo chiều sau đó do trả thừa.
 */
fun transactionDelta(currentDebtType: DebtType, kind: TransactionKind, txAmount: Long): Long {
    require(txAmount > 0) { "Transaction amount must be positive" }
    val unsignedDelta = if (kind == TransactionKind.PAYMENT) -txAmount else txAmount
    return if (currentDebtType == DebtType.THEY_OWE_ME) unsignedDelta else Math.negateExact(unsignedDelta)
}

/** Xóa một giao dịch: trừ delta đã lưu khỏi số dư hiện tại. */
fun reverseTransaction(currentAmount: Long, currentDebtType: DebtType, storedDelta: Long): DebtBalance {
    val result = Math.subtractExact(toSigned(currentAmount, currentDebtType), storedDelta)
    return fromSigned(result, currentDebtType)
}

/**
 * Tính số dư sau khi xóa một giao dịch đã lưu.
 * Dòng legacy (DB cũ, `signedDelta == 0`): đảo ngược naive theo hướng hiện tại (best-effort).
 */
fun reverseStoredTransaction(
    currentAmount: Long,
    currentDebtType: DebtType,
    transaction: DebtTransaction,
): DebtBalance {
    if (transaction.signedDelta != 0L) {
        return reverseTransaction(currentAmount, currentDebtType, transaction.signedDelta)
    }
    val inverseKind = if (transaction.kind == TransactionKind.PAYMENT) {
        TransactionKind.ADD
    } else {
        TransactionKind.PAYMENT
    }
    return applyTransaction(currentAmount, currentDebtType, inverseKind, transaction.amount)
}
