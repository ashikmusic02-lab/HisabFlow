package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        MonthPlanEntity::class,
        SavingsGoalEntity::class,
        DebtEntity::class,
        AccountEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun monthPlanDao(): MonthPlanDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun debtDao(): DebtDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hisabflow_secure.db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial default accounts
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.accountDao()?.insertAccount(
                                    AccountEntity(
                                        name = "bank account",
                                        type = "BANK",
                                        balance = 0.0
                                    )
                                )
                                INSTANCE?.accountDao()?.insertAccount(
                                    AccountEntity(
                                        name = "Cash",
                                        type = "CASH",
                                        balance = 0.0
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
