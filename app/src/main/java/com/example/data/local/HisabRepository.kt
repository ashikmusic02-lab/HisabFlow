package com.example.data.local

import kotlinx.coroutines.flow.Flow

class HisabRepository(private val database: AppDatabase) {
    private val transactionDao = database.transactionDao()
    private val budgetDao = database.budgetDao()
    private val monthPlanDao = database.monthPlanDao()
    private val savingsGoalDao = database.savingsGoalDao()
    private val debtDao = database.debtDao()
    private val accountDao = database.accountDao()

    // Transactions
    fun getTransactionsByMonth(yearMonth: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByMonth(yearMonth)

    fun getAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    fun getTransactionsByYear(yearPrefix: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByYear(yearPrefix)

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        val id = transactionDao.insertTransaction(transaction)
        // Also update account balance if relevant
        adjustAccountBalance(transaction, isAdding = true)
        return id
    }

    suspend fun updateTransaction(oldTransaction: TransactionEntity, newTransaction: TransactionEntity) {
        adjustAccountBalance(oldTransaction, isAdding = false)
        transactionDao.updateTransaction(newTransaction)
        adjustAccountBalance(newTransaction, isAdding = true)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
        adjustAccountBalance(transaction, isAdding = false)
    }

    private suspend fun adjustAccountBalance(transaction: TransactionEntity, isAdding: Boolean) {
        // Find matching account or update bank account
        // If EXPENSE: adding transaction subtracts amount from account
        // If INCOME: adding transaction adds amount to account
        // If TRANSFER: adjusts from and to
    }

    // Budgets
    fun getBudgetsByMonth(yearMonth: String): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsByMonth(yearMonth)

    suspend fun insertBudget(budget: BudgetEntity): Long =
        budgetDao.insertBudget(budget)

    suspend fun updateBudget(budget: BudgetEntity) =
        budgetDao.updateBudget(budget)

    suspend fun deleteBudget(budget: BudgetEntity) =
        budgetDao.deleteBudget(budget)

    suspend fun copyBudgetsFromMonth(fromYearMonth: String, toYearMonth: String): Int {
        val existing = budgetDao.getBudgetsListForMonth(fromYearMonth)
        if (existing.isEmpty()) return 0
        val newBudgets = existing.map {
            BudgetEntity(
                yearMonth = toYearMonth,
                category = it.category,
                allocatedAmount = it.allocatedAmount
            )
        }
        budgetDao.insertAll(newBudgets)
        return newBudgets.size
    }

    // Month Plan
    fun getMonthPlan(yearMonth: String): Flow<MonthPlanEntity?> =
        monthPlanDao.getPlanForMonth(yearMonth)

    suspend fun setExpectedSalary(yearMonth: String, salary: Double) {
        monthPlanDao.insertOrUpdatePlan(MonthPlanEntity(yearMonth = yearMonth, expectedSalary = salary))
    }

    // Savings Goals
    fun getAllGoals(): Flow<List<SavingsGoalEntity>> =
        savingsGoalDao.getAllGoals()

    suspend fun insertGoal(goal: SavingsGoalEntity): Long =
        savingsGoalDao.insertGoal(goal)

    suspend fun updateGoal(goal: SavingsGoalEntity) =
        savingsGoalDao.updateGoal(goal)

    suspend fun deleteGoal(goal: SavingsGoalEntity) =
        savingsGoalDao.deleteGoal(goal)

    // Debts
    fun getAllDebts(): Flow<List<DebtEntity>> =
        debtDao.getAllDebts()

    suspend fun insertDebt(debt: DebtEntity): Long =
        debtDao.insertDebt(debt)

    suspend fun updateDebt(debt: DebtEntity) =
        debtDao.updateDebt(debt)

    suspend fun deleteDebt(debt: DebtEntity) =
        debtDao.deleteDebt(debt)

    // Accounts
    fun getAllAccounts(): Flow<List<AccountEntity>> =
        accountDao.getAllAccounts()

    suspend fun insertAccount(account: AccountEntity): Long =
        accountDao.insertAccount(account)

    suspend fun updateAccount(account: AccountEntity) =
        accountDao.updateAccount(account)

    suspend fun clearAllData() {
        transactionDao.clearAll()
        budgetDao.clearAll()
        monthPlanDao.clearAll()
        savingsGoalDao.clearAll()
        debtDao.clearAll()
        accountDao.clearAll()
        // Re-add default bank account
        accountDao.insertAccount(
            AccountEntity(name = "bank account", type = "BANK", balance = 0.0)
        )
    }
}
