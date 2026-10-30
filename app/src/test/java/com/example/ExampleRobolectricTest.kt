package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CurrencyHelper
import com.example.model.QuickAddParser
import com.example.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("HisabFlow", appName)
    }

    @Test
    fun `quick add parser parses groceries expense`() {
        val parsed = QuickAddParser.parse("Groceries 1200")
        assertNotNull(parsed)
        assertEquals(TransactionType.EXPENSE, parsed!!.type)
        assertEquals(1200.0, parsed.amount, 0.01)
        assertEquals("Food & Groceries", parsed.category)
    }

    @Test
    fun `quick add parser parses salary income`() {
        val parsed = QuickAddParser.parse("Salary 30000")
        assertNotNull(parsed)
        assertEquals(TransactionType.INCOME, parsed!!.type)
        assertEquals(30000.0, parsed.amount, 0.01)
        assertEquals("Salary", parsed.category)
    }

    @Test
    fun `currency helper formats correctly and respects privacy`() {
        val formatted = CurrencyHelper.format(1500.0, "SAR (﷼)", false)
        assertEquals("1,500 ﷼", formatted)

        val masked = CurrencyHelper.format(1500.0, "SAR (﷼)", true)
        assertEquals("•••• ﷼", masked)
    }
}
