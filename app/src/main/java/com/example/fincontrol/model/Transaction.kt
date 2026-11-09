package com.example.fincontrol.model

data class Transaction(
    val id: Int,
    val description: String,
    val category: String,
    val value: Double,
    val isExpense: Boolean
)
