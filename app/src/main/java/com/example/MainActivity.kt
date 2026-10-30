package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.TransactionEntity
import com.example.data.local.UserPreferences
import com.example.model.AppStrings
import com.example.model.DateHelper
import com.example.model.TransactionType
import com.example.ui.MainViewModel
import com.example.ui.components.HisabBottomNav
import com.example.ui.components.HisabTopBar
import com.example.ui.components.PinLockScreen
import com.example.ui.screens.AddTransactionSheet
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        userPreferences = UserPreferences(this)
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                userPreferences.lockApp()
            }
        })

        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val prefsState by mainViewModel.prefsState.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = prefsState.isDarkTheme) {
                MainAppContent(
                    viewModel = mainViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefsState by viewModel.prefsState.collectAsStateWithLifecycle()
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    val monthlyTransactions by viewModel.monthlyTransactions.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val monthlyBudgets by viewModel.monthlyBudgets.collectAsStateWithLifecycle()
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val debts by viewModel.debts.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

    // Add Transaction Bottom Sheet state
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var initialAddType by remember { mutableStateOf(TransactionType.EXPENSE.name) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    // Pin setup modal state
    var isSettingUpPin by remember { mutableStateOf(false) }

    // Handle Hardware Back Button
    if (selectedTab != 0) {
        BackHandler {
            selectedTab = 0
        }
    }

    // Check if App is Locked by PIN
    if (prefsState.isPinEnabled && !prefsState.isUnlocked) {
        PinLockScreen(
            title = "HisabFlow",
            subtitle = AppStrings.get("pin_unlock_subtitle", prefsState.language),
            language = prefsState.language,
            remainingLockoutSeconds = prefsState.remainingLockoutSeconds,
            onPinEntered = { pin ->
                viewModel.preferences.verifyPin(pin)
            }
        )
        return
    }

    // Check if user is in PIN Setup Screen
    if (isSettingUpPin) {
        PinLockScreen(
            title = AppStrings.get("pin_setup_title", prefsState.language),
            subtitle = AppStrings.get("pin_setup_subtitle", prefsState.language),
            language = prefsState.language,
            onPinEntered = { pin ->
                viewModel.preferences.setPin(pin)
                isSettingUpPin = false
                val msg = AppStrings.get("pin_success_enabled", prefsState.language)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                true
            },
            onCancel = { isSettingUpPin = false }
        )
        return
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            HisabTopBar(
                selectedYearMonth = selectedYearMonth,
                onSelectYearMonth = { viewModel.selectYearMonth(it) },
                isPrivacyHidden = prefsState.isPrivacyHidden,
                onTogglePrivacy = { viewModel.preferences.togglePrivacy() },
                language = prefsState.language,
                onToggleLanguage = { viewModel.preferences.toggleLanguage() }
            )
        },
        bottomBar = {
            HisabBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                language = prefsState.language
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> HomeScreen(
                        summary = summary,
                        recentTransactions = monthlyTransactions,
                        selectedYearMonth = selectedYearMonth,
                        userName = prefsState.userName,
                        currencyCode = prefsState.currencyCode,
                        isPrivacyHidden = prefsState.isPrivacyHidden,
                        language = prefsState.language,
                        onOpenAddTransaction = { type ->
                            initialAddType = type
                            editingTransaction = null
                            showAddTransactionSheet = true
                        },
                        onQuickAdd = { text ->
                            val success = viewModel.quickAddTransaction(text)
                            if (success) {
                                val msg = if (prefsState.language == "bn") "লেনদেন দ্রুত যোগ করা হয়েছে!" else "Quick transaction added!"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            } else {
                                val msg = if (prefsState.language == "bn") "বুঝতে পারিনি। লিখুন: Groceries 1200" else "Could not parse. Try: Groceries 1200"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                            success
                        },
                        onNavigateToTransactions = { selectedTab = 2 },
                        onUpdateUserName = { viewModel.preferences.setUserName(it) },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) }
                    )

                    1 -> BudgetScreen(
                        summary = summary,
                        budgets = monthlyBudgets,
                        currencyCode = prefsState.currencyCode,
                        isPrivacyHidden = prefsState.isPrivacyHidden,
                        language = prefsState.language,
                        onSetExpectedSalary = { viewModel.setExpectedSalary(it) },
                        onAddBudgetCategory = { cat, amt -> viewModel.addBudgetCategory(cat, amt) },
                        onDeleteBudget = { viewModel.deleteBudget(it) },
                        onCopyFromPreviousMonth = { cb -> viewModel.copyBudgetsFromPreviousMonth(cb) }
                    )

                    2 -> TransactionsScreen(
                        transactions = monthlyTransactions,
                        currencyCode = prefsState.currencyCode,
                        isPrivacyHidden = prefsState.isPrivacyHidden,
                        language = prefsState.language,
                        onOpenAddTransaction = { type ->
                            initialAddType = type
                            editingTransaction = null
                            showAddTransactionSheet = true
                        },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) }
                    )

                    3 -> ReportsScreen(
                        summary = summary,
                        monthlyTransactions = monthlyTransactions,
                        allTransactions = allTransactions,
                        savingsGoals = savingsGoals,
                        debts = debts,
                        selectedYearMonth = selectedYearMonth,
                        currencyCode = prefsState.currencyCode,
                        isPrivacyHidden = prefsState.isPrivacyHidden,
                        language = prefsState.language,
                        onAddGoal = { title, target, current, note -> viewModel.addSavingsGoal(title, target, current, note) },
                        onDepositGoal = { goal, amt -> viewModel.depositToSavingsGoal(goal, amt) },
                        onDeleteGoal = { viewModel.deleteSavingsGoal(it) },
                        onAddDebt = { type, person, amt, note, due -> viewModel.addDebt(type, person, amt, note, due) },
                        onToggleDebtSettled = { viewModel.toggleDebtSettled(it) },
                        onDeleteDebt = { viewModel.deleteDebt(it) }
                    )

                    4 -> SettingsScreen(
                        accounts = accounts,
                        language = prefsState.language,
                        currencyCode = prefsState.currencyCode,
                        isDarkTheme = prefsState.isDarkTheme,
                        isPinEnabled = prefsState.isPinEnabled,
                        isPrivacyHidden = prefsState.isPrivacyHidden,
                        onSetLanguage = { viewModel.preferences.setLanguage(it) },
                        onSetCurrency = { viewModel.preferences.setCurrency(it) },
                        onToggleDarkTheme = { viewModel.preferences.setDarkTheme(it) },
                        onOpenPinSetup = { isSettingUpPin = true },
                        onRemovePin = {
                            viewModel.preferences.removePin()
                            val msg = AppStrings.get("pin_removed", prefsState.language)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        onUpdateAccountBalance = { account, bal -> viewModel.updateAccountBalance(account, bal) },
                        onResetAllData = {
                            viewModel.resetFinancialData()
                            val msg = if (prefsState.language == "bn") "সমস্ত ডেটা রিসেট হয়েছে" else "All financial data has been reset"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Add / Edit Transaction BottomSheet Modal
    if (showAddTransactionSheet) {
        AddTransactionSheet(
            onDismiss = { showAddTransactionSheet = false },
            onSave = { type, amount, category, account, toAccount, dateMillis, desc, person ->
                if (editingTransaction != null) {
                    val oldTx = editingTransaction!!
                    val updated = oldTx.copy(
                        type = type,
                        amount = amount,
                        category = category,
                        account = account,
                        toAccount = toAccount,
                        dateMillis = dateMillis,
                        description = desc,
                        person = person,
                        yearMonth = DateHelper.getYearMonthFromMillis(dateMillis)
                    )
                    viewModel.updateTransaction(oldTx, updated)
                } else {
                    viewModel.addTransaction(
                        type = type,
                        amount = amount,
                        category = category,
                        account = account,
                        toAccount = toAccount,
                        dateMillis = dateMillis,
                        description = desc,
                        person = person
                    )
                }
            },
            currencyCode = prefsState.currencyCode,
            language = prefsState.language,
            accounts = accounts,
            initialType = initialAddType,
            editingTransaction = editingTransaction
        )
    }
}
