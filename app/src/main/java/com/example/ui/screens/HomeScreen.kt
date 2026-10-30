package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.model.AppStrings
import com.example.model.CategoryList
import com.example.model.CurrencyHelper
import com.example.model.DateHelper
import com.example.model.TransactionType
import com.example.ui.FinancialSummary
import com.example.ui.components.AvailableBalanceCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.TransferBlue

@Composable
fun HomeScreen(
    summary: FinancialSummary,
    recentTransactions: List<TransactionEntity>,
    selectedYearMonth: String,
    userName: String,
    currencyCode: String,
    isPrivacyHidden: Boolean,
    language: String,
    onOpenAddTransaction: (String) -> Unit,
    onQuickAdd: (String) -> Boolean,
    onNavigateToTransactions: () -> Unit,
    onUpdateUserName: (String) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var quickAddText by remember { mutableStateOf("") }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User profile header row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            editedName = userName
                            showEditNameDialog = true
                        }
                    ) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Name",
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = AppStrings.get("monthly_summary", language),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // "+ Add" Pill Button
                Button(
                    onClick = { onOpenAddTransaction(TransactionType.EXPENSE.name) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("top_add_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.get("add", language),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Available Balance Card
        item {
            AvailableBalanceCard(
                availableBalance = summary.availableBalance,
                monthIncome = summary.totalIncome,
                monthExpense = summary.totalExpense,
                currencyCode = currencyCode,
                selectedYearMonth = selectedYearMonth,
                isPrivacyHidden = isPrivacyHidden,
                language = language
            )
        }

        // Quick Action Row: [+ Expense] [+ Income] [⇄ Transfer]
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // + Expense
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PrimaryIndigo,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onOpenAddTransaction(TransactionType.EXPENSE.name) }
                        .testTag("quick_expense_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Expense",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = AppStrings.get("expense", language),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // + Income
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onOpenAddTransaction(TransactionType.INCOME.name) }
                        .testTag("quick_income_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Income",
                            tint = IncomeGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = AppStrings.get("income", language),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = IncomeGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // ⇄ Transfer
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TransferBlue.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onOpenAddTransaction(TransactionType.TRANSFER.name) }
                        .testTag("quick_transfer_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Transfer",
                            tint = TransferBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = AppStrings.get("transfer", language),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TransferBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // 2x2 Stat Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: Net Savings
                    StatCard(
                        title = AppStrings.get("net_savings", language),
                        value = CurrencyHelper.format(summary.netSavings, currencyCode, isPrivacyHidden),
                        valueColor = if (summary.netSavings >= 0) IncomeGreen else ExpenseRed,
                        subtitle = "${summary.savingsRatePercent}% ${AppStrings.get("savings_rate", language)}",
                        modifier = Modifier.weight(1f)
                    )

                    // Card 2: Remaining Budget
                    StatCard(
                        title = AppStrings.get("remaining_budget", language),
                        value = CurrencyHelper.format(summary.remainingBudget, currencyCode, isPrivacyHidden),
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        subtitle = "${summary.budgetUsedPercent}% ${AppStrings.get("used", language)}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 3: Avg Daily Spend
                    StatCard(
                        title = AppStrings.get("avg_daily_spend", language),
                        value = "${CurrencyHelper.format(summary.avgDailySpend, currencyCode, isPrivacyHidden)} / day",
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        subtitle = "${summary.daysLeft} ${AppStrings.get("days_left", language)}",
                        modifier = Modifier.weight(1f)
                    )

                    // Card 4: Month-End Forecast
                    StatCard(
                        title = AppStrings.get("month_end_forecast", language),
                        value = CurrencyHelper.format(summary.monthEndForecast, currencyCode, isPrivacyHidden),
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        subtitle = AppStrings.get("est_balance", language),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Natural Language / Quick Add Input Bar
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Quick Add",
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = quickAddText,
                        onValueChange = { quickAddText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_input"),
                        placeholder = {
                            Text(
                                text = AppStrings.get("quick_add_hint", language),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    Button(
                        onClick = {
                            if (quickAddText.isNotBlank()) {
                                val success = onQuickAdd(quickAddText)
                                if (success) {
                                    quickAddText = ""
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("quick_add_button")
                    ) {
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Section: Recent Transactions Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.get("recent_transactions", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                TextButton(
                    onClick = onNavigateToTransactions,
                    modifier = Modifier.testTag("view_all_transactions_button")
                ) {
                    Text(
                        text = AppStrings.get("view_all", language),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = PrimaryIndigo,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Transactions List or Empty State
        if (recentTransactions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = AppStrings.get("no_transactions_found", language),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { onOpenAddTransaction(TransactionType.EXPENSE.name) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("empty_add_transaction_button")
                        ) {
                            Text(
                                text = AppStrings.get("add_transaction", language),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = PrimaryIndigo,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        } else {
            items(recentTransactions.take(5)) { tx ->
                TransactionRowItem(
                    transaction = tx,
                    currencyCode = currencyCode,
                    isPrivacyHidden = isPrivacyHidden,
                    onDelete = { onDeleteTransaction(tx) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Profile Name") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    singleLine = true,
                    label = { Text("Your Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            onUpdateUserName(editedName.trim())
                        }
                        showEditNameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    valueColor: Color,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.shadow(1.dp, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = valueColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: TransactionEntity,
    currencyCode: String,
    isPrivacyHidden: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            when (transaction.type) {
                                TransactionType.INCOME.name -> IncomeGreen.copy(alpha = 0.12f)
                                TransactionType.EXPENSE.name -> ExpenseRed.copy(alpha = 0.12f)
                                else -> TransferBlue.copy(alpha = 0.12f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = CategoryList.getIcon(transaction.category),
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${transaction.account} • ${DateHelper.formatDate(transaction.dateMillis)}" +
                                if (!transaction.person.isNullOrBlank()) " • ${transaction.person}" else "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Amount and optional delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isIncome = transaction.type == TransactionType.INCOME.name
                val isTransfer = transaction.type == TransactionType.TRANSFER.name
                val color = when {
                    isIncome -> IncomeGreen
                    isTransfer -> TransferBlue
                    else -> ExpenseRed
                }
                val prefix = when {
                    isIncome -> "+"
                    isTransfer -> ""
                    else -> "-"
                }

                Text(
                    text = "$prefix${CurrencyHelper.format(transaction.amount, currencyCode, isPrivacyHidden)}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
