package com.nomono.sono.util

import com.nomono.sono.data.DebtType
import com.nomono.sono.data.TransactionKind

object QuickAdjust {

    /**
     * Tính toán (amount, TransactionKind) tương ứng với delta có dấu:
     * - delta > 0 (+): Họ nợ thêm (nếu đang THEY_OWE_ME thì ADD, nếu đang I_OWE_THEM thì PAYMENT).
     * - delta < 0 (-): Họ trả bớt / Tôi nợ thêm (nếu đang THEY_OWE_ME thì PAYMENT, nếu đang I_OWE_THEM thì ADD).
     */
    fun calculateAdjustment(
        currentDebtType: DebtType,
        delta: Long,
    ): Pair<Long, TransactionKind>? {
        if (delta == 0L) return null
        return if (delta > 0L) {
            delta to if (currentDebtType == DebtType.THEY_OWE_ME) TransactionKind.ADD else TransactionKind.PAYMENT
        } else {
            (-delta) to if (currentDebtType == DebtType.THEY_OWE_ME) TransactionKind.PAYMENT else TransactionKind.ADD
        }
    }
}
