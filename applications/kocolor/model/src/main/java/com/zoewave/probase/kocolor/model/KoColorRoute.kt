package com.zoewave.probase.kocolor.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
sealed class KoColorRoute {
    @Serializable
    data object Home : KoColorRoute()
    
    @Serializable
    data object VanityLanding : KoColorRoute()

    @Serializable
    data object CosmeticAnalytics : KoColorRoute()
    
    @Serializable
    data object StyleSimulator : KoColorRoute()

    @Serializable
    data object WardrobeLanding : KoColorRoute()

    @Serializable
    data object Color : KoColorRoute()

    @Serializable
    data object Routines : KoColorRoute()

    @Serializable
    data class RoutineDetail(val routineId: Long) : KoColorRoute()

    @Serializable
    data class RoutineEditor(val routineId: Long, val stepId: String? = null) : KoColorRoute()

    @Serializable
    data object InventoryManagement : KoColorRoute()

    @Serializable
    data class Cosmetics(val filter: String? = null) : KoColorRoute()

    @Serializable
    data class CosmeticCategoryCover(val categoryName: String) : KoColorRoute()

    @Serializable
    data object Wardrobe : KoColorRoute()

    @Serializable
    data object WardrobeAnalytics : KoColorRoute()
    
    @Serializable
    data class WardrobeCategoryCover(val categoryName: String) : KoColorRoute()

    @Serializable
    data class WardrobeDetail(val itemId: Long) : KoColorRoute()

    @Serializable
    data class WardrobeEdit(val itemId: Long) : KoColorRoute()
    
    @Serializable
    data object WardrobeColorVerification : KoColorRoute()

    @Serializable
    data class Analyzer(val uri: String? = null) : KoColorRoute()
    
    @Serializable
    data object Suggestions : KoColorRoute()
    
    @Serializable
    data object Settings : KoColorRoute()

    @Serializable
    data object Health : KoColorRoute()

    @Serializable
    data object Back : KoColorRoute()

    @Serializable
    data class ColorDetail(val suggestionId: Long) : KoColorRoute()

    @Serializable
    data class CosmeticAdd(val categoryFilter: String? = null) : KoColorRoute()

    @Serializable
    data class CosmeticDetail(val itemId: Long) : KoColorRoute()

    @Serializable
    data class CosmeticEdit(val itemId: Long) : KoColorRoute()

    @Serializable
    data class NailLab(val colorHex: String, val finish: String) : KoColorRoute()

    @Serializable
    data class FaceLab(val colorHex: String, val category: String) : KoColorRoute()

    @Serializable
    data class Camera(val target: String) : KoColorRoute()

    @Serializable
    data object QRScanner : KoColorRoute()

    @Serializable
    data object BarcodeScanner : KoColorRoute()

    @Serializable
    data object GoogleXRTest : KoColorRoute()

    val icon: ImageVector?
        get() = when (this) {
            Home -> Icons.Default.Home
            InventoryManagement -> Icons.Default.Inventory
            CosmeticAnalytics -> Icons.Default.Insights
            Color -> Icons.Default.ColorLens
            Routines -> Icons.Default.Face
            Settings -> Icons.Default.Settings
            else -> null
        }

    val label: String?
        get() = when (this) {
            Home -> "Main"
            InventoryManagement -> "Inventory"
            CosmeticAnalytics -> "Analytics"
            Color -> "Color"
            Routines -> "Routines"
            Settings -> "Settings"
            else -> null
        }
}

val topLevelRoutes = listOf(
    KoColorRoute.Home,
    KoColorRoute.InventoryManagement,
    KoColorRoute.CosmeticAnalytics,
    KoColorRoute.Settings
)
