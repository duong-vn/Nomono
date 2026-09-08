package com.nomono.sono.util

object Validation {
    fun isNameValid(name: String): Boolean = name.isNotBlank()
    fun isAmountValid(amount: Long): Boolean = amount > 0L
}
