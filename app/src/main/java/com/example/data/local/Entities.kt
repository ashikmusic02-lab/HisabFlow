package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "EXPENSE", "INCOME", "TRANSFER"
    val amount: Double,
    val category: String,
    val account: String,
    val toAccount: String? = null,
    val dateMillis: Long,
    val description: String,
    val person: String? = null,
    val yearMonth: String // e.g. "2026-10"
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val yearMonth: String, // e.g. "2026-10"
    val category: String,
    val allocatedAmount: Double
)

@Entity(tableName = "month_plans")
data class MonthPlanEntity(
    @PrimaryKey val yearMonth: String, // e.g. "2026-10"
    val expectedSalary: Double
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val targetDateMillis: Long? = null,
    val note: String? = null
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "LENT" (I lent / I will receive), "BORROWED" (I borrowed / I owe)
    val personName: String,
    val amount: Double,
    val dueDateMillis: Long? = null,
    val note: String? = null,
    val isSettled: Boolean = false
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "BANK", "CASH", "CARD", "WALLET"
    val balance: Double
)
