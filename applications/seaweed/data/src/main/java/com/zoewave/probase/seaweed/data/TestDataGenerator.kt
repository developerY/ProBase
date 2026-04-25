package com.zoewave.probase.seaweed.data

import com.zoewave.probase.seaweed.model.BudgetTarget
import com.zoewave.probase.seaweed.model.SpendingImportance
import com.zoewave.probase.seaweed.model.Transaction
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class TestDataGenerator @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetTargetRepository,
) {
    private val categories = listOf("Food", "Transport", "Shopping", "Entertainment", "Health", "Utilities")

    suspend fun generateThreeMonthsOfData() {
        // Generate random budgets
        categories.forEach { category ->
            val limit = when (category) {
                "Food" -> Random.nextDouble(300.0, 600.0)
                "Transport" -> Random.nextDouble(100.0, 300.0)
                "Shopping" -> Random.nextDouble(200.0, 800.0)
                "Entertainment" -> Random.nextDouble(100.0, 400.0)
                "Health" -> Random.nextDouble(50.0, 200.0)
                "Utilities" -> Random.nextDouble(200.0, 500.0)
                else -> 500.0
            }
            budgetRepository.saveBudget(BudgetTarget(category, limit))
        }

        val now = System.currentTimeMillis()
        val ninetyDaysMillis = 90L * 24 * 60 * 60 * 1000

        repeat(150) {
            val randomTimeOffset = Random.nextLong(0, ninetyDaysMillis)
            val date = now - randomTimeOffset
            val category = categories.random()
            
            // Crucial: Use negative amounts for expenses to match the app logic
            val amount = when (category) {
                "Food" -> Random.nextDouble(5.0, 50.0)
                "Transport" -> Random.nextDouble(2.0, 30.0)
                "Shopping" -> Random.nextDouble(10.0, 200.0)
                "Entertainment" -> Random.nextDouble(10.0, 100.0)
                "Health" -> Random.nextDouble(20.0, 150.0)
                "Utilities" -> Random.nextDouble(50.0, 300.0)
                else -> Random.nextDouble(1.0, 100.0)
            } * -1.0 

            val importance = when (category) {
                "Shopping", "Entertainment" -> SpendingImportance.OPTIONAL
                else -> SpendingImportance.REQUIRED
            }

            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = amount,
                category = category,
                description = "Random $category expense",
                date = date,
                importance = importance
            )
            transactionRepository.addTransaction(transaction)
        }
    }

    suspend fun generateSingleRandomTransaction() {
        val category = categories.random()
        val amount = Random.nextDouble(5.0, 100.0) * -1.0
        val importance = when (category) {
            "Shopping", "Entertainment" -> SpendingImportance.OPTIONAL
            else -> SpendingImportance.REQUIRED
        }
        
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            category = category,
            description = "Quick random expense",
            date = System.currentTimeMillis(),
            importance = importance
        )
        transactionRepository.addTransaction(transaction)
    }
}
