package com.zoewave.probase.applications.photodo.db.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

data class ProjectTemplate(
    val title: String,
    val categoryName: String,
    val defaultBudget: Double,
    val icon: ImageVector
)

// The hardcoded list of templates for V1
val quickTemplates = listOf(
    ProjectTemplate("Grocery Run", "Home", 150.0, Icons.Default.ShoppingCart),
    ProjectTemplate("Quick Fix", "Home", 50.0, Icons.Default.Build),
    ProjectTemplate("Car Maintenance", "Home", 100.0, Icons.Default.DirectionsCar)
)