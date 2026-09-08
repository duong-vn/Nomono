package com.nomono.sono.util

import com.nomono.sono.data.Debt
import com.nomono.sono.data.DebtType

data class DebtTotals(val oweMe: Long, val iOwe: Long) {
    val net: Long get() = oweMe - iOwe
}

fun computeTotals(debts: List<Debt>): DebtTotals {
    var oweMe = 0L
    var iOwe = 0L
    for (debt in debts) {
        when (debt.debtType) {
            DebtType.THEY_OWE_ME -> oweMe += debt.amount
            DebtType.I_OWE_THEM -> iOwe += debt.amount
        }
    }
    return DebtTotals(oweMe = oweMe, iOwe = iOwe)
}
