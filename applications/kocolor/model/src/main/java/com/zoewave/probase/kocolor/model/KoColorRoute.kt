package com.zoewave.probase.kocolor.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
sealed class KoColorRoute {
    @Serializable
    data object Home : KoColorRoute()

    @Serializable
    data object CollectionHub : KoColorRoute()
    
    @Serializable
    data object VanityLanding : KoColorRoute()

    @Serializable
    data class BoxCapture(val mode: String = "BOX") : KoColorRoute()

    @Serializable
    data object ClothingCapture : KoColorRoute()

    @Serializable
    data object DiscoveryStatus : KoColorRoute()

    @Serializable
    data object CosmeticAnalytics : KoColorRoute()
    
    @Serializable
    data object StyleSimulator : KoColorRoute()

    @Serializable
    data object ColorSearch : KoColorRoute()

    @Serializable
    data object ColorHub : KoColorRoute()

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
    data object ExpiringSoon : KoColorRoute()

    @Serializable
    data class CollectionDetail(val collectionId: Long) : KoColorRoute()

    @Serializable
    data class Stitch(val id: Long = 0, val isCopy: Boolean = false) : KoColorRoute()

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
    data class Settings(val section: String? = null) : KoColorRoute()

    @Serializable
    data object Health : KoColorRoute()

    @Serializable
    data object Hydration : KoColorRoute()
    
    @Serializable
    data object Weather : KoColorRoute()

    @Serializable
    data object SunIntelligence : KoColorRoute()
    
    @Serializable
    data class Nutrition(val mealId: String? = null, val isCooking: Boolean = false) : KoColorRoute()

    @Serializable
    data class MealsHub(val mealId: String? = null, val isCooking: Boolean = false) : KoColorRoute()

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

    /*@Serializable
    data class NailLab(val colorHex: String, val finish: String) : KoColorRoute()

    @Serializable
    data class FaceLab(val colorHex: String, val category: String) : KoColorRoute()*/

    @Serializable
    data class Camera(val target: String) : KoColorRoute()

    @Serializable
    data object QRScanner : KoColorRoute()

    @Serializable
    data object BarcodeScanner : KoColorRoute()

    @Serializable
    data object GoogleXRTest : KoColorRoute()

    @Serializable
    data object StarterPack : KoColorRoute()

    @Serializable
    data class PackPreview(
        val packId: String, 
        val targetItemId: String? = null,
        val sha256: String? = null,
        val publisher: String? = null
    ) : KoColorRoute()

    val icon: ImageVector?
        get() = when (this) {
            Home -> Icons.Default.Home
            Color -> Icons.Default.AutoAwesome
            is Settings -> Icons.Default.Settings
            else -> null
        }

    val label: String?
        get() = when (this) {
            Home -> "Home"
            Color -> "Collection"
            is Settings -> "Settings"
            else -> null
        }
}

val topLevelRoutes = listOf(
    KoColorRoute.Home,
    KoColorRoute.Color,
    KoColorRoute.Settings()
)
