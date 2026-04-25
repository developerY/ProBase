package com.zoewave.probase.seaweed.data

import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.seaweed.model.BudgetTarget
import com.zoewave.probase.seaweed.model.Transaction
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class TestDataGenerator @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetTargetRepository,
    private val categoryRepository: CategoryRepository,
) {
    suspend fun generateThreeMonthsOfData() {
        val categories = categoryRepository.getAllCategories().first()
        
        // Generate random budgets
        categories.forEach { category ->
            val limitAmount = when (category.name) {
                "Rent" -> 1500.0
                "Groceries" -> 500.0
                "Healthcare" -> 200.0
                "Utilities" -> 300.0
                "Netflix", "Spotify" -> 20.0
                "Dining Out" -> 400.0
                "Shopping" -> 500.0
                else -> 100.0
            }
            budgetRepository.saveBudget(BudgetTarget(category.name, CurrencyUtils.toCents(limitAmount)))
        }

        val now = System.currentTimeMillis()
        val ninetyDaysMillis = 90L * 24 * 60 * 60 * 1000

        repeat(150) {
            val randomTimeOffset = Random.nextLong(0, ninetyDaysMillis)
            val timestamp = now - randomTimeOffset
            val category = categories.random()
            
            // Use negative amounts for expenses to match the app logic
            val amount = when (category.name) {
                "Rent" -> 1500.0
                "Groceries" -> Random.nextDouble(20.0, 150.0)
                "Healthcare" -> Random.nextDouble(10.0, 100.0)
                "Utilities" -> Random.nextDouble(50.0, 200.0)
                "Netflix" -> 15.99
                "Spotify" -> 10.99
                "Dining Out" -> Random.nextDouble(10.0, 80.0)
                "Shopping" -> Random.nextDouble(5.0, 200.0)
                else -> Random.nextDouble(1.0, 100.0)
            } * -1.0 

            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                amountCents = CurrencyUtils.toCents(amount),
                categoryId = category.id,
                description = "Random ${category.name} expense",
                timestamp = timestamp,
                defaultType = category.defaultType
            )
            transactionRepository.addTransaction(transaction)
        }
    }

    suspend fun generateSingleRandomTransaction() {
        val categories = categoryRepository.getAllCategories().first()
        if (categories.isEmpty()) return

        val category = categories.random()
        val amount = Random.nextDouble(5.0, 100.0) * -1.0
        
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            amountCents = CurrencyUtils.toCents(amount),
            categoryId = category.id,
            description = "Quick random expense",
            timestamp = System.currentTimeMillis(),
            defaultType = category.defaultType
        )
        transactionRepository.addTransaction(transaction)
    }
}
