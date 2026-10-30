package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AccountEntity
import com.example.data.local.AppDatabase
import com.example.data.local.BudgetEntity
import com.example.data.local.DebtEntity
import com.example.data.local.HisabRepository
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesState
import com.example.model.DateHelper
import com.example.model.QuickAddParser
import com.example.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class FinancialSummary(
    val availableBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netSavings: Double = 0.0,
    val savingsRatePercent: Int = 0,
    val totalBudget: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val budgetUsedPercent: Int = 0,
    val avgDailySpend: Double = 0.0,
    val daysLeft: Int = 0,
    val projectedMonthExpense: Double = 0.0,
    val monthEndForecast: Double = 0.0,
    val expectedSalary: Double = 0.0,
    val totalAllocatedBudget: Double = 0.0,
    val plannedSurplus: Double = 0.0,
    val categorySpendMap: Map<String, Double> = emptyMap(),
    val categoryIncomeMap: Map<String, Double> = emptyMap()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = HisabRepository(database)
    val preferences = UserPreferences(application)

    val prefsState: StateFlow<UserPreferencesState> = preferences.state

    // Active Year-Month for filtering (e.g., dynamically "2026-10" or current month)
    private val _selectedYearMonth = MutableStateFlow(DateHelper.getCurrentYearMonth())
    val selectedYearMonth: StateFlow<String> = _selectedYearMonth.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyTransactions: StateFlow<List<TransactionEntity>> = _selectedYearMonth
        .flatMapLatest { ym -> repository.getTransactionsByMonth(ym) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyBudgets: StateFlow<List<BudgetEntity>> = _selectedYearMonth
        .flatMapLatest { ym -> repository.getBudgetsByMonth(ym) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyPlan = _selectedYearMonth
        .flatMapLatest { ym -> repository.getMonthPlan(ym) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debts: StateFlow<List<DebtEntity>> = repository.getAllDebts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<AccountEntity>> = repository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined summary calculations
    val summary: StateFlow<FinancialSummary> = combine(
        monthlyTransactions,
        monthlyBudgets,
        monthlyPlan,
        accounts,
        _selectedYearMonth
    ) { txs, budgets, plan, accts, ym ->
        var income = 0.0
        var expense = 0.0
        val catSpend = mutableMapOf<String, Double>()
        val catIncome = mutableMapOf<String, Double>()

        for (tx in txs) {
            when (tx.type) {
                TransactionType.INCOME.name -> {
                    income += tx.amount
                    val current = catIncome[tx.category] ?: 0.0
                    catIncome[tx.category] = current + tx.amount
                }
                TransactionType.EXPENSE.name -> {
                    expense += tx.amount
                    val current = catSpend[tx.category] ?: 0.0
                    catSpend[tx.category] = current + tx.amount
                }
            }
        }

        val totalAccountsBalance = accts.sumOf { it.balance }
        val available = (income - expense + totalAccountsBalance).coerceAtLeast(0.0)
        val netSavings = income - expense
        val savingsRate = if (income > 0) ((netSavings / income) * 100).toInt().coerceAtLeast(0) else 0

        val totalAllocated = budgets.sumOf { it.allocatedAmount }
        val remainingBudget = (totalAllocated - expense).coerceAtLeast(0.0)
        val budgetUsedPct = if (totalAllocated > 0) ((expense / totalAllocated) * 100).toInt() else 0

        val elapsed = DateHelper.getElapsedDaysInMonth(ym)
        val daysInMonth = DateHelper.getDaysInMonth(ym)
        val daysLeft = DateHelper.getDaysLeftInMonth(ym)
        val dailySpend = if (elapsed > 0) expense / elapsed else 0.0
        val projectedExpense = dailySpend * daysInMonth
        val monthEndForecast = (income - projectedExpense).coerceAtLeast(0.0)

        val expSalary = plan?.expectedSalary ?: 0.0
        val plannedSurplus = expSalary - totalAllocated

        FinancialSummary(
            availableBalance = available,
            totalIncome = income,
            totalExpense = expense,
            netSavings = netSavings,
            savingsRatePercent = savingsRate,
            totalBudget = totalAllocated,
            remainingBudget = remainingBudget,
            budgetUsedPercent = budgetUsedPct,
            avgDailySpend = dailySpend,
            daysLeft = daysLeft,
            projectedMonthExpense = projectedExpense,
            monthEndForecast = monthEndForecast,
            expectedSalary = expSalary,
            totalAllocatedBudget = totalAllocated,
            plannedSurplus = plannedSurplus,
            categorySpendMap = catSpend,
            categoryIncomeMap = catIncome
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    fun selectYearMonth(ym: String) {
        _selectedYearMonth.value = ym
    }

    fun addTransaction(
        type: String,
        amount: Double,
        category: String,
        account: String,
        toAccount: String? = null,
        dateMillis: Long = System.currentTimeMillis(),
        description: String,
        person: String? = null
    ) {
        viewModelScope.launch {
            val ym = DateHelper.getYearMonthFromMillis(dateMillis)
            repository.insertTransaction(
                TransactionEntity(
                    type = type,
                    amount = amount,
                    category = category,
                    account = account,
                    toAccount = toAccount,
                    dateMillis = dateMillis,
                    description = description,
                    person = person,
                    yearMonth = ym
                )
            )
        }
    }

    fun updateTransaction(oldTransaction: TransactionEntity, newTransaction: TransactionEntity) {
        viewModelScope.launch {
            val ym = DateHelper.getYearMonthFromMillis(newTransaction.dateMillis)
            repository.updateTransaction(oldTransaction, newTransaction.copy(yearMonth = ym))
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun quickAddTransaction(rawText: String): Boolean {
        val parsed = QuickAddParser.parse(rawText) ?: return false
        addTransaction(
            type = parsed.type.name,
            amount = parsed.amount,
            category = parsed.category,
            account = "Cash",
            description = parsed.description
        )
        return true
    }

    fun setExpectedSalary(salary: Double) {
        viewModelScope.launch {
            repository.setExpectedSalary(_selectedYearMonth.value, salary)
        }
    }

    fun addBudgetCategory(category: String, amount: Double) {
        viewModelScope.launch {
            repository.insertBudget(
                BudgetEntity(
                    yearMonth = _selectedYearMonth.value,
                    category = category,
                    allocatedAmount = amount
                )
            )
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    fun copyBudgetsFromPreviousMonth(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val currentYm = _selectedYearMonth.value
            // Calculate previous month
            val parts = currentYm.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val prevMonth = if (month == 1) 12 else month - 1
            val prevYear = if (month == 1) year - 1 else year
            val prevYm = String.format("%04d-%02d", prevYear, prevMonth)

            val count = repository.copyBudgetsFromMonth(prevYm, currentYm)
            onComplete(count)
        }
    }

    fun addSavingsGoal(title: String, targetAmount: Double, initialSaved: Double = 0.0, note: String? = null) {
        viewModelScope.launch {
            repository.insertGoal(
                SavingsGoalEntity(
                    title = title,
                    targetAmount = targetAmount,
                    savedAmount = initialSaved,
                    note = note
                )
            )
        }
    }

    fun depositToSavingsGoal(goal: SavingsGoalEntity, depositAmount: Double) {
        viewModelScope.launch {
            val updated = goal.copy(savedAmount = goal.savedAmount + depositAmount)
            repository.updateGoal(updated)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun addDebt(type: String, personName: String, amount: Double, note: String? = null, dueDate: Long? = null) {
        viewModelScope.launch {
            repository.insertDebt(
                DebtEntity(
                    type = type,
                    personName = personName,
                    amount = amount,
                    dueDateMillis = dueDate,
                    note = note,
                    isSettled = false
                )
            )
        }
    }

    fun toggleDebtSettled(debt: DebtEntity) {
        viewModelScope.launch {
            repository.updateDebt(debt.copy(isSettled = !debt.isSettled))
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }

    fun updateAccountBalance(account: AccountEntity, newBalance: Double) {
        viewModelScope.launch {
            repository.updateAccount(account.copy(balance = newBalance))
        }
    }

    fun resetFinancialData() {
        viewModelScope.launch {
            repository.clearAllData()
            preferences.resetAll()
        }
    }
}
