package com.nomono.sono.util

import com.nomono.sono.data.TransactionKind

fun applyTransaction(current: Long, kind: TransactionKind, txAmount: Long): Long =
    (current + if (kind == TransactionKind.PAYMENT) -txAmount else txAmount).coerceAtLeast(0L)
