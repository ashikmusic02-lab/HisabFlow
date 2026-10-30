package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DebtEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import com.example.model.AppStrings
import com.example.model.CategoryList
import com.example.model.CurrencyHelper
import com.example.model.DateHelper
import com.example.ui.FinancialSummary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.TransferBlue

@Composable
fun ReportsScreen(
    summary: FinancialSummary,
    monthlyTransactions: List<TransactionEntity>,
    allTransactions: List<TransactionEntity>,
    savingsGoals: List<SavingsGoalEntity>,
    debts: List<DebtEntity>,
    selectedYearMonth: String,
    currencyCode: String,
    isPrivacyHidden: Boolean,
    language: String,
    onAddGoal: (String, Double, Double, String?) -> Unit,
    onDepositGoal: (SavingsGoalEntity, Double) -> Unit,
    onDeleteGoal: (SavingsGoalEntity) -> Unit,
    onAddDebt: (type: String, person: String, amount: Double, note: String?, dueDate: Long?) -> Unit,
    onToggleDebtSettled: (DebtEntity) -> Unit,
    onDeleteDebt: (DebtEntity) -> Unit,
    onWatchRewardedAd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isYearlyView by remember { mutableStateOf(false) }
    var breakdownType by remember { mutableStateOf("EXPENSE") }

    // Dialog states
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalTitleInput by remember { mutableStateOf("") }
    var goalTargetInput by remember { mutableStateOf("") }
    var goalInitialInput by remember { mutableStateOf("") }

    var depositGoalTarget by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var depositAmountInput by remember { mutableStateOf("") }

    var showAddDebtDialog by remember { mutableStateOf(false) }
    var debtTypeInput by remember { mutableStateOf("LENT") } // LENT or BORROWED
    var debtPersonInput by remember { mutableStateOf("") }
    var debtAmountInput by remember { mutableStateOf("") }
    var debtNoteInput by remember { mutableStateOf("") }

    val activeTransactions = if (isYearlyView) {
        val yearPrefix = selectedYearMonth.take(4)
        allTransactions.filter { it.yearMonth.startsWith(yearPrefix) }
    } else {
        monthlyTransactions
    }

    val totalIncome = activeTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = activeTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // [Monthly] | [Yearly (2026)] Toggle
        item {
            val year = selectedYearMonth.take(4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isYearlyView) PrimaryIndigo else Color.Transparent)
                        .clickable { isYearlyView = false }
                        .padding(vertical = 10.dp)
                        .testTag("report_toggle_monthly"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.get("monthly", language),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (!isYearlyView) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isYearlyView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isYearlyView) PrimaryIndigo else Color.Transparent)
                        .clickable { isYearlyView = true }
                        .padding(vertical = 10.dp)
                        .testTag("report_toggle_yearly"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${AppStrings.get("yearly", language)} ($year)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isYearlyView) FontWeight.Bold else FontWeight.Medium,
                            color = if (isYearlyView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Expense Forecast Section
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppStrings.get("expense_forecast", language),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = AppStrings.get("forecast_note", language),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Daily Burn Rate
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = AppStrings.get("daily_burn_rate", language),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = CurrencyHelper.format(summary.avgDailySpend, currencyCode, isPrivacyHidden),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                        // Projected Month Expense
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = AppStrings.get("projected_month_expense", language),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = CurrencyHelper.format(summary.projectedMonthExpense, currencyCode, isPrivacyHidden),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseRed
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Income vs Expense Chart Section
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.get("income_vs_expense", language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = if (isYearlyView) "Year ${selectedYearMonth.take(4)}" else AppStrings.get("current_month", language),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Clean Bar Chart Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val maxVal = maxOf(totalIncome, totalExpense, 1.0).toFloat()
                            val barWidth = 60.dp.toPx()
                            val corner = CornerRadius(16f, 16f)

                            val availableHeight = size.height - 40f
                            val incomeHeight = ((totalIncome.toFloat() / maxVal) * availableHeight).coerceAtLeast(10f)
                            val expenseHeight = ((totalExpense.toFloat() / maxVal) * availableHeight).coerceAtLeast(10f)

                            val incomeX = size.width * 0.35f - barWidth / 2
                            val expenseX = size.width * 0.65f - barWidth / 2

                            // Income bar
                            drawRoundRect(
                                color = IncomeGreen,
                                topLeft = Offset(incomeX, size.height - incomeHeight),
                                size = Size(barWidth, incomeHeight),
                                cornerRadius = corner
                            )

                            // Expense bar
                            drawRoundRect(
                                color = ExpenseRed,
                                topLeft = Offset(expenseX, size.height - expenseHeight),
                                size = Size(barWidth, expenseHeight),
                                cornerRadius = corner
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend & values below chart
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = CurrencyHelper.format(totalIncome, currencyCode, isPrivacyHidden),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                            )
                            Text(
                                text = AppStrings.get("income", language),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = CurrencyHelper.format(totalExpense, currencyCode, isPrivacyHidden),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            )
                            Text(
                                text = AppStrings.get("expense", language),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }

        // Spending Breakdown Section
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppStrings.get("spending_breakdown", language),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Breakdown Toggle: [Income] [Expense]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (breakdownType == "EXPENSE") ExpenseRed else Color.Transparent)
                                .clickable { breakdownType = "EXPENSE" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = AppStrings.get("expense", language),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (breakdownType == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (breakdownType == "INCOME") IncomeGreen else Color.Transparent)
                                .clickable { breakdownType = "INCOME" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = AppStrings.get("income", language),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (breakdownType == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val breakdownMap = if (breakdownType == "EXPENSE") summary.categorySpendMap else summary.categoryIncomeMap
                    val totalSum = if (breakdownType == "EXPENSE") summary.totalExpense else summary.totalIncome

                    if (breakdownMap.isEmpty() || totalSum <= 0) {
                        Text(
                            text = if (breakdownType == "EXPENSE") AppStrings.get("no_expenses_recorded", language) else "No income recorded for this period yet",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            breakdownMap.entries.sortedByDescending { it.value }.forEach { (category, amt) ->
                                val pct = ((amt / totalSum) * 100).toInt()
                                val progress = (amt / totalSum).toFloat().coerceIn(0f, 1f)

                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = CategoryList.getIcon(category), fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = category,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                        }
                                        Text(
                                            text = "${CurrencyHelper.format(amt, currencyCode, isPrivacyHidden)} ($pct%)",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (breakdownType == "EXPENSE") ExpenseRed else IncomeGreen
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (breakdownType == "EXPENSE") ExpenseRed else IncomeGreen,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Savings Goals Section
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.get("savings_goals", language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        TextButton(
                            onClick = {
                                goalTitleInput = ""
                                goalTargetInput = ""
                                goalInitialInput = ""
                                showAddGoalDialog = true
                            },
                            modifier = Modifier.testTag("new_savings_goal_button")
                        ) {
                            Text(
                                text = AppStrings.get("new_goal", language),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PrimaryIndigo,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    if (savingsGoals.isEmpty()) {
                        Text(
                            text = AppStrings.get("no_savings_goals", language),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            savingsGoals.forEach { goal ->
                                val progress = if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                                val pct = (progress * 100).toInt()

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Savings,
                                                    contentDescription = null,
                                                    tint = IncomeGreen,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = goal.title,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                            IconButton(
                                                onClick = { onDeleteGoal(goal) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Goal",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = IncomeGreen,
                                            trackColor = MaterialTheme.colorScheme.surface,
                                            strokeCap = StrokeCap.Round
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${CurrencyHelper.format(goal.savedAmount, currencyCode, isPrivacyHidden)} / ${CurrencyHelper.format(goal.targetAmount, currencyCode, isPrivacyHidden)} ($pct%)",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )

                                            Button(
                                                onClick = {
                                                    depositGoalTarget = goal
                                                    depositAmountInput = ""
                                                },
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text(
                                                    text = "+ Deposit",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Debt & Lending Ledger Section
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.get("debt_lending", language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        TextButton(
                            onClick = {
                                debtPersonInput = ""
                                debtAmountInput = ""
                                debtNoteInput = ""
                                showAddDebtDialog = true
                            },
                            modifier = Modifier.testTag("new_debt_entry_button")
                        ) {
                            Text(
                                text = AppStrings.get("new_entry", language),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PrimaryIndigo,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    if (debts.isEmpty()) {
                        Text(
                            text = AppStrings.get("no_debts_recorded", language),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            debts.forEach { debt ->
                                val isLent = debt.type == "LENT"
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            IconButton(
                                                onClick = { onToggleDebtSettled(debt) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (debt.isSettled) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                                    contentDescription = "Settle Status",
                                                    tint = if (debt.isSettled) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (isLent) IncomeGreen.copy(alpha = 0.2f) else ExpenseRed.copy(alpha = 0.2f)
                                                    ) {
                                                        Text(
                                                            text = if (isLent) (if (language == "bn") "পাবো" else "Lent") else (if (language == "bn") "দিতে হবে" else "Borrowed"),
                                                            color = if (isLent) IncomeGreen else ExpenseRed,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = debt.personName,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (debt.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    )
                                                }
                                                if (!debt.note.isNullOrBlank()) {
                                                    Text(
                                                        text = debt.note,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontSize = 11.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = CurrencyHelper.format(debt.amount, currencyCode, isPrivacyHidden),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (debt.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else if (isLent) IncomeGreen else ExpenseRed
                                                )
                                            )
                                            IconButton(
                                                onClick = { onDeleteDebt(debt) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Debt",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Export Financial Statement (Rewarded Ad Integration)
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == "bn") "মাসিক স্টেটমেন্ট ডাউনলোড" else "Export Monthly Statement",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = if (language == "bn") "একটি ছোট ভিডিও বিজ্ঞাপন দেখে সম্পূর্ণ PDF/Excel রিপোর্ট আনলক করুন" else "Watch a short rewarded ad to unlock and download full statement",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onWatchRewardedAd,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Text(
                            text = if (language == "bn") "ভিডিও দেখুন" else "Watch Ad",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text(AppStrings.get("new_goal", language)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = goalTitleInput,
                        onValueChange = { goalTitleInput = it },
                        label = { Text("Goal Title (e.g. Emergency Fund, Hajj)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = goalTargetInput,
                        onValueChange = { goalTargetInput = it },
                        label = { Text("Target Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = goalInitialInput,
                        onValueChange = { goalInitialInput = it },
                        label = { Text("Already Saved Amount (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = goalTargetInput.toDoubleOrNull() ?: 0.0
                        val initial = goalInitialInput.toDoubleOrNull() ?: 0.0
                        if (goalTitleInput.isNotBlank() && target > 0) {
                            onAddGoal(goalTitleInput.trim(), target, initial, null)
                            showAddGoalDialog = false
                        }
                    }
                ) {
                    Text("Save Goal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text(AppStrings.get("cancel", language))
                }
            }
        )
    }

    // Deposit to Goal Dialog
    depositGoalTarget?.let { goal ->
        AlertDialog(
            onDismissRequest = { depositGoalTarget = null },
            title = { Text("Deposit to ${goal.title}") },
            text = {
                OutlinedTextField(
                    value = depositAmountInput,
                    onValueChange = { depositAmountInput = it },
                    label = { Text("Deposit Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = depositAmountInput.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onDepositGoal(goal, amount)
                            depositGoalTarget = null
                        }
                    }
                ) {
                    Text("Deposit")
                }
            },
            dismissButton = {
                TextButton(onClick = { depositGoalTarget = null }) {
                    Text(AppStrings.get("cancel", language))
                }
            }
        )
    }

    // Add Debt Dialog
    if (showAddDebtDialog) {
        AlertDialog(
            onDismissRequest = { showAddDebtDialog = false },
            title = { Text(AppStrings.get("new_entry", language)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (debtTypeInput == "LENT") IncomeGreen else Color.Transparent)
                                .clickable { debtTypeInput = "LENT" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (language == "bn") "আমি পাবো (Lent)" else "I Lent",
                                color = if (debtTypeInput == "LENT") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (debtTypeInput == "BORROWED") ExpenseRed else Color.Transparent)
                                .clickable { debtTypeInput = "BORROWED" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (language == "bn") "আমি দেবো (Borrowed)" else "I Borrowed",
                                color = if (debtTypeInput == "BORROWED") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = debtPersonInput,
                        onValueChange = { debtPersonInput = it },
                        label = { Text("Person Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = debtAmountInput,
                        onValueChange = { debtAmountInput = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = debtNoteInput,
                        onValueChange = { debtNoteInput = it },
                        label = { Text("Note / Reason (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = debtAmountInput.toDoubleOrNull() ?: 0.0
                        if (debtPersonInput.isNotBlank() && amount > 0) {
                            onAddDebt(debtTypeInput, debtPersonInput.trim(), amount, debtNoteInput.ifBlank { null }, null)
                            showAddDebtDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDebtDialog = false }) {
                    Text(AppStrings.get("cancel", language))
                }
            }
        )
    }
}
