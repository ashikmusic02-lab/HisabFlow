package com.example.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TransactionType(val labelEn: String, val labelBn: String) {
    EXPENSE("Expense", "ব্যয়"),
    INCOME("Income", "আয়"),
    TRANSFER("Transfer", "স্থানান্তর")
}

data class CategoryItem(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val icon: String,
    val isExpenseDefault: Boolean = true
)

object CategoryList {
    val allCategories = listOf(
        CategoryItem("education", "Education", "শিক্ষা", "🎓", true),
        CategoryItem("food", "Food & Groceries", "খাবার ও বাজার", "🍔", true),
        CategoryItem("transport", "Transportation", "যাতায়াত", "🚗", true),
        CategoryItem("housing", "Housing & Rent", "ভাড়া ও বাসস্থান", "🏠", true),
        CategoryItem("utilities", "Bills & Utilities", "বিল ও সেবা", "💡", true),
        CategoryItem("shopping", "Shopping", "কেনাকাটা", "🛍️", true),
        CategoryItem("health", "Healthcare", "চিকিৎসা", "💊", true),
        CategoryItem("entertainment", "Entertainment", "বিনোদন", "🎬", true),
        CategoryItem("salary", "Salary", "বেতন", "💼", false),
        CategoryItem("business", "Business", "ব্যবসা", "📈", false),
        CategoryItem("investment", "Investment", "বিনিয়োগ", "💰", false),
        CategoryItem("gift", "Gift & Bonus", "উপহার ও বোনাস", "🎁", false),
        CategoryItem("other", "Other", "অন্যান্য", "📦", true)
    )

    fun getIcon(categoryName: String): String {
        return allCategories.find { 
            it.nameEn.equals(categoryName, ignoreCase = true) || 
            it.nameBn.equals(categoryName, ignoreCase = true) ||
            categoryName.contains(it.nameEn, ignoreCase = true)
        }?.icon ?: "💳"
    }
}

fun String.toCleanEnglishDigits(): String {
    return this.replace('০', '0')
        .replace('১', '1')
        .replace('২', '2')
        .replace('৩', '3')
        .replace('৪', '4')
        .replace('৫', '5')
        .replace('৬', '6')
        .replace('৭', '7')
        .replace('৮', '8')
        .replace('৯', '9')
        .replace(",", "")
        .trim()
}

fun String.toBengaliDigits(): String {
    return this.replace('0', '০')
        .replace('1', '১')
        .replace('2', '২')
        .replace('3', '৩')
        .replace('4', '৪')
        .replace('5', '৫')
        .replace('6', '৬')
        .replace('7', '৭')
        .replace('8', '৮')
        .replace('9', '৯')
}

fun String.toCleanDoubleOrNull(): Double? {
    return this.toCleanEnglishDigits().toDoubleOrNull()
}

object QuickAddParser {
    data class ParsedTransaction(
        val type: TransactionType,
        val description: String,
        val amount: Double,
        val category: String
    )

    fun parse(input: String): ParsedTransaction? {
        val converted = input.toCleanEnglishDigits()
        if (converted.isEmpty()) return null

        // Match patterns like "Groceries 1200" or "1200 Groceries" or "Salary 30000" or "খাবার ৫০০"
        val regex = Regex("""([a-zA-Z\u0980-\u09FF\s&]+?)\s*(\d+(?:\.\d+)?)\b|\b(\d+(?:\.\d+)?)\s*([a-zA-Z\u0980-\u09FF\s&]+)""")
        val match = regex.find(converted)

        if (match != null) {
            val (desc1, amt1, amt2, desc2) = match.destructured
            val description = if (desc1.isNotEmpty()) desc1.trim() else desc2.trim()
            val amountStr = if (amt1.isNotEmpty()) amt1 else amt2
            val amount = amountStr.toDoubleOrNull() ?: return null

            val lowerDesc = description.lowercase()
            val isIncome = lowerDesc.contains("salary") || lowerDesc.contains("বেতন") ||
                    lowerDesc.contains("income") || lowerDesc.contains("আয়") ||
                    lowerDesc.contains("freelance") || lowerDesc.contains("bonus") ||
                    lowerDesc.contains("profit") || lowerDesc.contains("লাভ")

            val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

            val category = when {
                lowerDesc.contains("education") || lowerDesc.contains("school") || lowerDesc.contains("বই") || lowerDesc.contains("পড়া") -> "Education"
                lowerDesc.contains("grocer") || lowerDesc.contains("food") || lowerDesc.contains("খাবার") || lowerDesc.contains("বাজার") || lowerDesc.contains("restaurant") -> "Food & Groceries"
                lowerDesc.contains("rent") || lowerDesc.contains("house") || lowerDesc.contains("ভাড়া") -> "Housing & Rent"
                lowerDesc.contains("fuel") || lowerDesc.contains("uber") || lowerDesc.contains("bus") || lowerDesc.contains("যাতায়াত") -> "Transportation"
                lowerDesc.contains("bill") || lowerDesc.contains("electric") || lowerDesc.contains("gas") || lowerDesc.contains("কারেন্ট") -> "Bills & Utilities"
                lowerDesc.contains("salary") || lowerDesc.contains("বেতন") -> "Salary"
                else -> if (isIncome) "Income" else "Other"
            }

            return ParsedTransaction(type, description.replaceFirstChar { it.uppercase() }, amount, category)
        }

        return null
    }
}

object CurrencyHelper {
    val supportedCurrencies = listOf(
        "BDT (৳)" to "৳",
        "SAR (﷼)" to "﷼",
        "USD ($)" to "$",
        "EUR (€)" to "€",
        "INR (₹)" to "₹",
        "GBP (£)" to "£",
        "AED (د.إ)" to "د.إ"
    )

    fun getSymbol(currencyCode: String): String {
        return supportedCurrencies.find { it.first == currencyCode }?.second
            ?: currencyCode.substringAfter("(").substringBefore(")").ifEmpty { "৳" }
    }

    fun format(amount: Double, currencyCode: String, isPrivacyHidden: Boolean = false, language: String = "en"): String {
        val symbol = getSymbol(currencyCode)
        if (isPrivacyHidden) {
            return "•••• $symbol"
        }
        val formatted = if (amount % 1.0 == 0.0) {
            String.format(Locale.US, "%,d", amount.toLong())
        } else {
            String.format(Locale.US, "%,.2f", amount)
        }
        val finalNumber = if (language == "bn") formatted.toBengaliDigits() else formatted
        return "$finalNumber $symbol"
    }

    fun formatCompact(amount: Double, currencyCode: String, isPrivacyHidden: Boolean = false, language: String = "en"): String {
        val symbol = getSymbol(currencyCode)
        if (isPrivacyHidden) return "•••• $symbol"
        val formatted = if (amount % 1.0 == 0.0) {
            amount.toLong().toString()
        } else {
            String.format(Locale.US, "%.1f", amount)
        }
        val finalNumber = if (language == "bn") formatted.toBengaliDigits() else formatted
        return "$finalNumber $symbol"
    }
}

object DateHelper {
    private fun getLocale(language: String): Locale = if (language == "bn") Locale("bn") else Locale.ENGLISH

    fun formatYearMonthDisplay(yearMonth: String, language: String = "en"): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM", Locale.US)
            val date = parser.parse(yearMonth) ?: Date()
            val formatter = SimpleDateFormat("MMMM yyyy", getLocale(language))
            val result = formatter.format(date)
            if (language == "bn") result.toBengaliDigits() else result
        } catch (e: Exception) {
            if (language == "bn") yearMonth.toBengaliDigits() else yearMonth
        }
    }

    fun formatYearMonthShort(yearMonth: String, language: String = "en"): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM", Locale.US)
            val date = parser.parse(yearMonth) ?: Date()
            val formatter = SimpleDateFormat("MMM yyyy", getLocale(language))
            val result = formatter.format(date).uppercase()
            if (language == "bn") result.toBengaliDigits() else result
        } catch (e: Exception) {
            yearMonth.uppercase()
        }
    }

    fun getCurrentYearMonth(): String = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    fun getYearMonthFromMillis(millis: Long): String {
        return SimpleDateFormat("yyyy-MM", Locale.US).format(Date(millis))
    }

    fun formatDate(millis: Long, language: String = "en"): String {
        val formatter = SimpleDateFormat("MM/dd/yyyy", getLocale(language))
        val str = formatter.format(Date(millis))
        return if (language == "bn") str.toBengaliDigits() else str
    }

    fun formatTime(millis: Long, language: String = "en"): String {
        val formatter = SimpleDateFormat("hh:mm a", getLocale(language))
        val str = formatter.format(Date(millis))
        return if (language == "bn") str.toBengaliDigits() else str
    }

    fun formatReadableDate(millis: Long, language: String = "en"): String {
        val formatter = SimpleDateFormat("EEE, dd MMM yyyy", getLocale(language))
        val str = formatter.format(Date(millis))
        return if (language == "bn") str.toBengaliDigits() else str
    }

    fun getDaysLeftInMonth(yearMonth: String): Int {
        val cal = Calendar.getInstance()
        val currentYm = getCurrentYearMonth()
        if (yearMonth != currentYm) return 0
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        return (maxDays - currentDay).coerceAtLeast(0)
    }

    fun getElapsedDaysInMonth(yearMonth: String): Int {
        val cal = Calendar.getInstance()
        val currentYm = getCurrentYearMonth()
        if (yearMonth != currentYm) return 30
        return cal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
    }

    fun getDaysInMonth(yearMonth: String): Int {
        return try {
            val cal = Calendar.getInstance()
            cal.time = SimpleDateFormat("yyyy-MM", Locale.US).parse(yearMonth) ?: Date()
            cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        } catch (e: Exception) {
            30
        }
    }

    fun getAvailableMonths(): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        // Anchor to current month so the app dynamically tracks current time
        val formatter = SimpleDateFormat("yyyy-MM", Locale.US)
        for (i in -6..6) {
            val temp = cal.clone() as Calendar
            temp.add(Calendar.MONTH, i)
            list.add(formatter.format(temp.time))
        }
        return list
    }
}

object AppStrings {
    fun get(key: String, lang: String = "en"): String {
        val isBn = lang == "bn"
        return when (key) {
            "app_name" -> "HisabFlow"
            "monthly_summary" -> if (isBn) "মাসিক আর্থিক সারসংক্ষেপ" else "Monthly Financial Summary"
            "add" -> if (isBn) "+ যোগ করুন" else "+ Add"
            "available_balance" -> if (isBn) "উপলব্ধ ব্যালেন্স" else "AVAILABLE BALANCE"
            "month_income" -> if (isBn) "মাসের আয়" else "Month Income"
            "month_expense" -> if (isBn) "মাসের ব্যয়" else "Month Expense"
            "expense" -> if (isBn) "ব্যয়" else "Expense"
            "income" -> if (isBn) "আয়" else "Income"
            "transfer" -> if (isBn) "স্থানান্তর" else "Transfer"
            "net_savings" -> if (isBn) "মোট সঞ্চয়" else "Net Savings"
            "savings_rate" -> if (isBn) "সঞ্চয়ের হার" else "Savings Rate"
            "remaining_budget" -> if (isBn) "অবশিষ্ট বাজেট" else "Remaining Budget"
            "used" -> if (isBn) "ব্যবহৃত" else "used"
            "avg_daily_spend" -> if (isBn) "গড় দৈনিক ব্যয়" else "Avg Daily Spend"
            "days_left" -> if (isBn) "দিন বাকি" else "days left"
            "month_end_forecast" -> if (isBn) "মাস শেষের পূর্বাভাস" else "Month-End Forecast"
            "est_balance" -> if (isBn) "আনুমানিক ব্যালেন্স" else "Est. Balance"
            "quick_add_hint" -> if (isBn) "লিখুন: বাজার ১২০০ অথবা বেতন ৩০০০০..." else "Type: Groceries 1200 or Salary 30000..."
            "recent_transactions" -> if (isBn) "সাম্প্রতিক লেনদেন" else "Recent Transactions"
            "view_all" -> if (isBn) "সব দেখুন" else "View All"
            "no_transactions_found" -> if (isBn) "কোনো লেনদেন পাওয়া যায়নি" else "No transactions found"
            "add_transaction" -> if (isBn) "+ লেনদেন যোগ করুন" else "+ Add Transaction"
            "home" -> if (isBn) "হোম" else "Home"
            "budget" -> if (isBn) "বাজেট" else "Budget"
            "transactions" -> if (isBn) "লেনদেন" else "Transactions"
            "reports" -> if (isBn) "রিপোর্ট" else "Reports"
            "settings" -> if (isBn) "সেটিংস" else "Settings"
            "salary_expense_plan" -> if (isBn) "বেতন ও ব্যয় পরিকল্পনা" else "Salary & Expense Plan"
            "salary_plan_subtitle" -> if (isBn) "প্রত্যাশিত বেতন ও নির্ধারিত ক্যাটাগরি বাজেটের সংক্ষিপ্ত বিবরণ" else "Overview of your expected salary and planned category budgets"
            "expected_salary" -> if (isBn) "প্রত্যাশিত বেতন" else "Expected Salary"
            "total_allocated" -> if (isBn) "মোট বরাদ্দ" else "Total Allocated"
            "planned_surplus" -> if (isBn) "পরিকল্পিত উদ্বৃত্ত" else "Planned Surplus"
            "add_budget_category" -> if (isBn) "+ বাজেট ক্যাটাগরি যোগ করুন" else "+ Add Budget Category"
            "category_budgets" -> if (isBn) "ক্যাটাগরি বাজেট" else "Category Budgets"
            "no_budgets_set" -> if (isBn) "এই মাসের জন্য কোনো বাজেট নির্ধারণ করা হয়নি।" else "No budgets set for this month yet."
            "categories" -> if (isBn) "টি ক্যাটাগরি" else "categories"
            "search_hint" -> if (isBn) "বিবরণ, পরিমাণ বা ব্যক্তি খুঁজুন..." else "Search description, amount or person..."
            "records_found" -> if (isBn) "টি রেকর্ড পাওয়া গেছে" else "records found"
            "all" -> if (isBn) "সব" else "All"
            "monthly" -> if (isBn) "মাসিক" else "Monthly"
            "yearly" -> if (isBn) "বাৎসরিক" else "Yearly"
            "expense_forecast" -> if (isBn) "ব্যয়ের পূর্বাভাস" else "Expense Forecast"
            "forecast_note" -> if (isBn) "*অতিক্রান্ত দিন এবং বর্তমান ব্যয়ের হারের ভিত্তিতে আনুমানিক হিসাব।" else "*Estimated trajectory based on elapsed days and current spending rate."
            "daily_burn_rate" -> if (isBn) "দৈনিক ব্যয়ের গতি" else "Daily Burn Rate"
            "projected_month_expense" -> if (isBn) "মাসের প্রাক্কলিত ব্যয়" else "Projected Month Expense"
            "income_vs_expense" -> if (isBn) "আয় বনাম ব্যয়" else "Income vs Expense"
            "current_month" -> if (isBn) "চলতি মাস" else "Current Month"
            "spending_breakdown" -> if (isBn) "ব্যয়ের বিবরণী" else "Spending Breakdown"
            "no_expenses_recorded" -> if (isBn) "এই মাসে কোনো ব্যয় পাওয়া যায়নি" else "No expenses recorded for this month yet"
            "savings_goals" -> if (isBn) "সঞ্চয় লক্ষ্য" else "Savings Goals"
            "new_goal" -> if (isBn) "+ নতুন লক্ষ্য" else "+ New Goal"
            "no_savings_goals" -> if (isBn) "কোনো সঞ্চয় লক্ষ্য যোগ করা হয়নি" else "No savings goals added"
            "debt_lending" -> if (isBn) "ধার ও দেনা-পাওনা লেজার" else "Debt & Lending Ledger"
            "new_entry" -> if (isBn) "+ নতুন এন্ট্রি" else "+ New Entry"
            "no_debts_recorded" -> if (isBn) "কোনো দেনা বা পাওনা রেকর্ড নেই" else "No debts or receivables recorded"
            "app_preferences" -> if (isBn) "অ্যাপ পছন্দসমূহ" else "App Preferences"
            "language" -> if (isBn) "ভাষা" else "Language"
            "choose_language" -> if (isBn) "অ্যাপের ভাষা নির্বাচন করুন" else "Choose app language"
            "currency" -> if (isBn) "মুদ্রা প্রতীক" else "Currency"
            "select_currency" -> if (isBn) "পছন্দের মুদ্রা প্রতীক নির্বাচন করুন" else "Select preferred currency symbol"
            "dark_theme" -> if (isBn) "ডার্ক থিম" else "Dark Theme"
            "dark_theme_desc" -> if (isBn) "চোখের জন্য আরামদায়ক অন্ধকার মোড" else "Comfortable dark background"
            "security_pin" -> if (isBn) "নিরাপত্তা ও পিন লক" else "Security & PIN Lock"
            "app_pin_lock" -> if (isBn) "অ্যাপ পিন লক" else "App PIN Lock"
            "pin_lock_desc" -> if (isBn) "অ্যাপ খোলার সময় ৪-ডিজিটের তাৎক্ষণিক সুরক্ষিত থ্রিডি ওয়েভ অ্যানিমেশন লক" else "Instant secure 4-digit lock on launch with 3D water wave animation"
            "change_remove_pin" -> if (isBn) "পিন পরিবর্তন / মুছুন" else "Change / Remove PIN"
            "set_pin" -> if (isBn) "পিন সেট করুন" else "Set PIN"
            "reset_data" -> if (isBn) "আর্থিক ডেটা রিসেট করুন" else "Reset Financial Data"
            "reset_desc" -> if (isBn) "ডিভাইস থেকে সমস্ত স্থানীয় লেনদেন, বাজেট, লক্ষ্য ও হিসাব স্থায়ীভাবে মুছে ফেলুন" else "Permanently clear local transactions, budgets, goals & accounts"
            "reset" -> if (isBn) "রিসেট" else "Reset"
            "cancel" -> if (isBn) "বাতিল" else "Cancel"
            "save_transaction" -> if (isBn) "সংরক্ষণ করুন" else "Save Transaction"
            "amount" -> if (isBn) "পরিমাণ *" else "Amount *"
            "category" -> if (isBn) "ক্যাটাগরি *" else "Category *"
            "account" -> if (isBn) "অ্যাকাউন্ট *" else "Account *"
            "date" -> if (isBn) "তারিখ *" else "Date *"
            "time" -> if (isBn) "সময়" else "Time"
            "description" -> if (isBn) "বিবরণ *" else "Description *"
            "person_contact" -> if (isBn) "ব্যক্তি / পরিচিতি (ঐচ্ছিক)" else "Person / Contact (Optional)"
            "bank_account" -> if (isBn) "ব্যাংক অ্যাকাউন্ট" else "bank account"
            "bank_tap_edit" -> if (isBn) "ব্যাংক • ট্যাপ করে পরিবর্তন করুন" else "BANK • TAP TO EDIT"
            "about_legal" -> if (isBn) "অ্যাপ সম্পর্কিত ও নীতিমালা" else "About & Legal"
            "privacy_policy" -> if (isBn) "গোপনীয়তা নীতি (Privacy Policy)" else "Privacy Policy"
            "privacy_policy_desc" -> if (isBn) "আপনার ডেটা সুরক্ষা নীতিমালা" else "Your data security & privacy policy"
            "terms_conditions" -> if (isBn) "শর্তাবলী (Terms & Conditions)" else "Terms & Conditions"
            "terms_conditions_desc" -> if (isBn) "অ্যাপ ব্যবহারের সাধারণ নিয়মাবলী" else "General terms of service"
            "app_version" -> if (isBn) "অ্যাপ সংস্করণ" else "App Version"
            "pin_title" -> "HisabFlow"
            "pin_unlock_subtitle" -> if (isBn) "আনলক করতে ৪-সংখ্যার পিন দিন" else "Enter 4-digit PIN to Unlock"
            "pin_setup_title" -> if (isBn) "নতুন পিন সেট করুন" else "Set Security PIN"
            "pin_setup_subtitle" -> if (isBn) "৪-সংখ্যার গোপন পিন লিখুন" else "Enter 4-digit new PIN"
            "pin_incorrect" -> if (isBn) "ভুল পিন! আবার চেষ্টা করুন" else "Incorrect PIN! Try again"
            "pin_lockout" -> if (isBn) "অতিরিক্ত ভুল পিন! অপেক্ষা করুন: " else "Too many failed attempts! Wait: "
            "pin_seconds" -> if (isBn) " সেকেন্ড" else " seconds"
            "pin_success_enabled" -> if (isBn) "পিন সফলভাবে সক্রিয় করা হয়েছে" else "PIN Lock enabled successfully"
            "pin_removed" -> if (isBn) "পিন সরানো হয়েছে" else "PIN Lock removed"
            else -> key
        }
    }
}
