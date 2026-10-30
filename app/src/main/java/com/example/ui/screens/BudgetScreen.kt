package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BudgetEntity
import com.example.model.AppStrings
import com.example.model.CategoryList
import com.example.model.CurrencyHelper
import com.example.model.toCleanDoubleOrNull
import com.example.ui.FinancialSummary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.WarningOrange

@Composable
fun BudgetScreen(
    summary: FinancialSummary,
    budgets: List<BudgetEntity>,
    currencyCode: String,
    isPrivacyHidden: Boolean,
    language: String,
    onSetExpectedSalary: (Double) -> Unit,
    onAddBudgetCategory: (String, Double) -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit,
    onCopyFromPreviousMonth: ((Int) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSalaryDialog by remember { mutableStateOf(false) }
    var salaryInput by remember { mutableStateOf("") }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Education") }
    var categoryAllocatedInput by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Salary & Expense Plan Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.get("salary_expense_plan", language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        IconButton(
                            onClick = {
                                salaryInput = if (summary.expectedSalary > 0) summary.expectedSalary.toLong().toString() else ""
                                showSalaryDialog = true
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("edit_salary_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Expected Salary",
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = AppStrings.get("salary_plan_subtitle", language),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Two Boxes: Expected Salary & Total Allocated
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Expected Salary Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = AppStrings.get("expected_salary", language),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = CurrencyHelper.format(summary.expectedSalary, currencyCode, isPrivacyHidden),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = IncomeGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Total Allocated Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = AppStrings.get("total_allocated", language),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = CurrencyHelper.format(summary.totalAllocatedBudget, currencyCode, isPrivacyHidden),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = WarningOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Planned Surplus
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.get("planned_surplus", language) + ":",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = CurrencyHelper.format(summary.plannedSurplus, currencyCode, isPrivacyHidden),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (summary.plannedSurplus >= 0) IncomeGreen else ExpenseRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Action Buttons Row: [+ Add Budget Category] & [Copy/Duplicate Button]
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        categoryAllocatedInput = ""
                        showAddCategoryDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("add_budget_category_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.get("add_budget_category", language),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Copy previous month icon button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            onCopyFromPreviousMonth { copiedCount ->
                                val msg = if (copiedCount > 0) {
                                    if (language == "bn") "পূর্ববর্তী মাস থেকে $copiedCount টি বাজেট কপি করা হয়েছে!" else "Copied $copiedCount budgets from previous month!"
                                } else {
                                    if (language == "bn") "পূর্ববর্তী মাসে কোনো বাজেট পাওয়া যায়নি" else "No budgets found in previous month"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("copy_budgets_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy from previous month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Category Budgets Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.get("category_budgets", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "${budgets.size} ${AppStrings.get("categories", language)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Empty state or budgets list
        if (budgets.isEmpty()) {
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
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Budget Chart",
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = AppStrings.get("no_budgets_set", language),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                categoryAllocatedInput = ""
                                showAddCategoryDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("empty_add_budget_button")
                        ) {
                            Text(
                                text = AppStrings.get("add_budget_category", language),
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
            items(budgets) { budget ->
                val spent = summary.categorySpendMap[budget.category] ?: 0.0
                val progress = if (budget.allocatedAmount > 0) (spent / budget.allocatedAmount).toFloat().coerceIn(0f, 1f) else 0f
                val isExceeded = spent > budget.allocatedAmount

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = CategoryList.getIcon(budget.category),
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = budget.category,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            IconButton(
                                onClick = { onDeleteBudget(budget) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Budget",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isExceeded) ExpenseRed else PrimaryIndigo,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Spent: ${CurrencyHelper.format(spent, currencyCode, isPrivacyHidden)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = "Budget: ${CurrencyHelper.format(budget.allocatedAmount, currencyCode, isPrivacyHidden)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Edit Expected Salary Dialog
    if (showSalaryDialog) {
        AlertDialog(
            onDismissRequest = { showSalaryDialog = false },
            title = { Text(AppStrings.get("expected_salary", language)) },
            text = {
                OutlinedTextField(
                    value = salaryInput,
                    onValueChange = { salaryInput = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = salaryInput.toCleanDoubleOrNull() ?: 0.0
                        onSetExpectedSalary(amount)
                        showSalaryDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSalaryDialog = false }) {
                    Text(AppStrings.get("cancel", language))
                }
            }
        )
    }

    // Add Budget Category Dialog
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text(AppStrings.get("add_budget_category", language)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Category:", style = MaterialTheme.typography.labelMedium)
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoryDropdownExpanded = true }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = CategoryList.getIcon(selectedCategory), fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(selectedCategory)
                                }
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            CategoryList.allCategories.filter { it.isExpenseDefault }.forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Row {
                                            Text(cat.icon)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(if (language == "bn") cat.nameBn else cat.nameEn)
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = if (language == "bn") cat.nameBn else cat.nameEn
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = categoryAllocatedInput,
                        onValueChange = { categoryAllocatedInput = it },
                        label = { Text("Budget Limit / Allocated Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = categoryAllocatedInput.toCleanDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onAddBudgetCategory(selectedCategory, amount)
                            categoryAllocatedInput = ""
                            showAddCategoryDialog = false
                        } else {
                            val msg = if (language == "bn") "দয়া করে বাজেটের পরিমাণ লিখুন" else "Please enter budget amount"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Add Budget")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text(AppStrings.get("cancel", language))
                }
            }
        )
    }
}
