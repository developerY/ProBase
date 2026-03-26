package com.zoewave.probase.applications.photodo.db.seed

import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity

object PhotoDoOnboardingData {

    // 1. The Top-Level Categories (Using your new colorHex and isFavorite flags)
    val defaultCategories = listOf(
        CategoryEntity(categoryId = 1, name = "Home", colorHex = "#FFB4A2", isFavorite = true),
        CategoryEntity(categoryId = 2, name = "Work", colorHex = "#B5EAD7", isFavorite = true),
        CategoryEntity(categoryId = 3, name = "Life", colorHex = "#C7CEEA", isFavorite = false)
    )

    // 2. The Projects (Using your new projectBudget flag!)
    val defaultProjects = listOf(
        // HOME (Category 1)
        ProjectEntity(projectId = 1, categoryId = 1, name = "Cleaning & Maintenance"),
        ProjectEntity(projectId = 2, categoryId = 1, name = "Food & Groceries", projectBudget = 250.0),
        ProjectEntity(projectId = 3, categoryId = 1, name = "Repairs & Projects", isUrgent = true),

        // WORK (Category 2)
        ProjectEntity(projectId = 4, categoryId = 2, name = "Active Sprints", projectBudget = 0.0),
        ProjectEntity(projectId = 5, categoryId = 2, name = "Admin & Expenses", projectBudget = 500.0),
        ProjectEntity(projectId = 6, categoryId = 2, name = "Assets & Branding"),

        // LIFE (Category 3)
        ProjectEntity(projectId = 7, categoryId = 3, name = "Finances & Admin"),
        ProjectEntity(projectId = 8, categoryId = 3, name = "Travel & Logistics", projectBudget = 1200.0),
        ProjectEntity(projectId = 9, categoryId = 3, name = "Health & Fitness")
    )

    // 3. The Visual Prompts (Tasks) to train the user
    val defaultTasks = listOf(
        // Under "Food & Groceries"
        TaskEntity(taskId = 1, projectId = 2, text = "Tap the Camera to snap a pic of an empty carton"),
        TaskEntity(taskId = 2, projectId = 2, text = "Take a photo of a recipe you want to try"),

        // Under "Repairs & Projects"
        TaskEntity(taskId = 3, projectId = 3, text = "Snap a photo of the broken pipe under the sink"),

        // Under "Admin & Expenses"
        TaskEntity(taskId = 4, projectId = 5, text = "Tap the Camera to scan a cafe receipt"),

        // Under "Travel & Logistics"
        TaskEntity(taskId = 5, projectId = 8, text = "Take a picture of your passport")
    )
}