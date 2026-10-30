package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AccountEntity
import com.example.model.AppStrings
import com.example.model.CurrencyHelper
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryIndigo

@Composable
fun SettingsScreen(
    accounts: List<AccountEntity>,
    language: String,
    currencyCode: String,
    isDarkTheme: Boolean,
    isPinEnabled: Boolean,
    isPrivacyHidden: Boolean,
    onSetLanguage: (String) -> Unit,
    onSetCurrency: (String) -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit,
    onOpenPinSetup: () -> Unit,
    onRemovePin: () -> Unit,
    onUpdateAccountBalance: (AccountEntity, Double) -> Unit,
    onResetAllData: () -> Unit,
    onShowInterstitialTest: () -> Unit = {},
    onShowRewardedTest: () -> Unit = {},
    onShowAppOpenTest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var languageDropdownExpanded by remember { mutableStateOf(false) }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }

    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var accountBalanceInput by remember { mutableStateOf("") }

    val primaryAccount = accounts.firstOrNull { it.type == "BANK" }
        ?: accounts.firstOrNull()
        ?: AccountEntity(name = "bank account", type = "BANK", balance = 0.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Bank Account Card: "bank account", "BANK • TAP TO EDIT", "0 ﷼"
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        editingAccount = primaryAccount
                        accountBalanceInput = primaryAccount.balance.toString()
                    }
                    .testTag("account_card_item")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryIndigo.copy(alpha = 0.12f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = "Bank",
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = primaryAccount.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = AppStrings.get("bank_tap_edit", language),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Text(
                        text = CurrencyHelper.format(primaryAccount.balance, currencyCode, isPrivacyHidden),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        // Section: App Preferences
        item {
            Column {
                Text(
                    text = AppStrings.get("app_preferences", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // 1. Language Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.get("language", language),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = AppStrings.get("choose_language", language),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Box {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { languageDropdownExpanded = true }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                        .testTag("settings_language_dropdown")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (language == "bn") "বাংলা" else "English",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = languageDropdownExpanded,
                                    onDismissRequest = { languageDropdownExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("English") },
                                        onClick = {
                                            onSetLanguage("en")
                                            languageDropdownExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("বাংলা (Bengali)") },
                                        onClick = {
                                            onSetLanguage("bn")
                                            languageDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // 2. Currency Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.get("currency", language),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = AppStrings.get("select_currency", language),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Box {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { currencyDropdownExpanded = true }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                        .testTag("settings_currency_dropdown")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = currencyCode,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = currencyDropdownExpanded,
                                    onDismissRequest = { currencyDropdownExpanded = false }
                                ) {
                                    CurrencyHelper.supportedCurrencies.forEach { (code, _) ->
                                        DropdownMenuItem(
                                            text = { Text(code) },
                                            onClick = {
                                                onSetCurrency(code)
                                                currencyDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Dark Theme Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.get("dark_theme", language),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = AppStrings.get("dark_theme_desc", language),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = onToggleDarkTheme,
                                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryIndigo),
                                modifier = Modifier.testTag("dark_theme_switch")
                            )
                        }
                    }
                }
            }
        }

        // Section: Security & PIN Lock
        item {
            Column {
                Text(
                    text = AppStrings.get("security_pin", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.get("app_pin_lock", language),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = AppStrings.get("pin_lock_desc", language),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    if (isPinEnabled) {
                                        onRemovePin()
                                    } else {
                                        onOpenPinSetup()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("pin_lock_action_button")
                            ) {
                                Text(
                                    text = if (isPinEnabled) AppStrings.get("change_remove_pin", language) else AppStrings.get("set_pin", language),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryIndigo
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }



        // Section: Reset Financial Data
        item {
            Column {
                Text(
                    text = AppStrings.get("reset_data", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    ),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

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
                                text = AppStrings.get("reset_desc", language),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Button(
                            onClick = { showResetConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("reset_data_button")
                        ) {
                            Text(
                                text = AppStrings.get("reset", language),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section: About & Legal
        item {
            Column {
                Text(
                    text = AppStrings.get("about_legal", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Privacy Policy Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPrivacyPolicyDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.get("privacy_policy", language),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = AppStrings.get("privacy_policy_desc", language),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            Text(
                                text = "View",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PrimaryIndigo,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // Terms of Service Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTermsDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.get("terms_conditions", language),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = AppStrings.get("terms_conditions_desc", language),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            Text(
                                text = "View",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PrimaryIndigo,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // App Version Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = AppStrings.get("app_version", language),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = "1.0.0 (Offline & Secure)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
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

    // Edit Account Balance Dialog
    editingAccount?.let { account ->
        AlertDialog(
            onDismissRequest = { editingAccount = null },
            title = { Text("Edit ${account.name} Balance") },
            text = {
                OutlinedTextField(
                    value = accountBalanceInput,
                    onValueChange = { accountBalanceInput = it },
                    label = { Text("Starting / Current Balance") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bal = accountBalanceInput.toDoubleOrNull() ?: 0.0
                        onUpdateAccountBalance(account, bal)
                        editingAccount = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingAccount = null }) {
                    Text(AppStrings.get("cancel", language))
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text(AppStrings.get("reset_data", language)) },
            text = {
                Text(
                    text = if (language == "bn")
                        "আপনি কি নিশ্চিত যে সমস্ত ডেটা স্থায়ীভাবে মুছে ফেলতে চান? এই ক্রিয়াটি পূর্বাবস্থায় ফেরানো যাবে না।"
                    else
                        "Are you sure you want to permanently clear all local transactions, budgets, goals & accounts? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text(AppStrings.get("reset", language), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text(AppStrings.get("cancel", language))
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = {
                Text(
                    text = if (language == "bn") "গোপনীয়তা নীতি (Privacy Policy)" else "Privacy Policy",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (language == "bn")
                            "১. স্থানীয় ডেটা সংরক্ষণ:\nHisabFlow সম্পূর্ণ অফলাইন ফাইন্যান্স অ্যাপ। আপনার সমস্ত লেনদেন, বাজেট এবং ব্যক্তিগত হিসাব শুধুমাত্র আপনার নিজস্ব ডিভাইসের লোকাল মেমরিতে (SQLite) সংরক্ষিত থাকে। কোনো অনলাইন ক্লাউড বা সার্ভারে কোনো ডেটা প্রেরণ করা হয় না।\n\n২. বিজ্ঞাপন ও তৃতীয় পক্ষ:\nঅ্যাপটিতে বিজ্ঞাপন পরিবেশনের জন্য Google AdMob SDK ব্যবহৃত হতে পারে। গুগল ব্যক্তিগতকৃত বা অপ্রাসঙ্গিক বিজ্ঞাপন পরিবেশনের উদ্দেশ্যে সাধারণ ডিভাইস আইডেন্টিফায়ার (GAID) ব্যবহার করতে পারে।\n\n৩. আপনার নিরাপত্তা:\n৪-ডিজিটের স্থানীয় পিন লক সুরক্ষার মাধ্যমে আপনার ডেটা সুরক্ষিত রাখা হয়।"
                        else
                            "1. Local Data Storage:\nHisabFlow is a 100% offline personal finance app. All your transactions, budgets, goals, and records are stored exclusively on your device's local memory (SQLite). No financial data is ever transmitted to external servers.\n\n2. Advertising Policy:\nThe app may use Google AdMob SDK to serve advertisements. Google may process standard device identifiers (GAID) to deliver relevant ad content.\n\n3. Security:\nInstant secure 4-digit PIN lock keeps your financial data strictly private on your device.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Terms of Service Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = if (language == "bn") "শর্তাবলী (Terms & Conditions)" else "Terms & Conditions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (language == "bn")
                            "১. সেবা ব্যবহার:\nHisabFlow অ্যাপটি ব্যক্তিগত আর্থিক হিসাব ও বাজেট ব্যবস্থাপনার উদ্দেশ্যে বিনামূল্যে ব্যবহারের জন্য প্রদান করা হয়েছে।\n\n২. ডেটার দায়িত্ব:\nযেহেতু অ্যাপটি সম্পূর্ণ অফলাইন, ব্যবহারকারী তার ডিভাইসের ব্যাকআপ ও নিরাপত্তার জন্য নিজেই দায়বদ্ধ। অ্যাপ রিসেট করলে ডেটা স্থায়ীভাবে মুছে যাবে।\n\n৩. হালনাগাদ:\nভবিষ্যতে নতুন ফিচার বা নীতি হালনাগাদের অধিকার সংরক্ষিত।"
                        else
                            "1. Use of Service:\nHisabFlow is provided free of charge for personal expense tracking and financial budgeting.\n\n2. User Responsibility:\nBecause the app runs completely offline, the user is responsible for device security and backup. Clearing app data or resetting will permanently remove records.\n\n3. Modifications:\nWe reserve the right to update features and terms as the app evolves.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showTermsDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
