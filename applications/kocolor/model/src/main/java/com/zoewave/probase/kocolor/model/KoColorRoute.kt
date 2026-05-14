package com.zoewave.probase.kocolor.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
sealed class KoColorRoute {
    @Serializable
    data object Home : KoColorRoute()
    
    @Serializable
    data object Color : KoColorRoute()

    @Serializable
    data object Routines : KoColorRoute()

    @Serializable
    data object Cosmetics : KoColorRoute()

    @Serializable
    data object Wardrobe : KoColorRoute()
    
    @Serializable
    data class Analyzer(val uri: String? = null) : KoColorRoute()
    
    @Serializable
    data object Suggestions : KoColorRoute()
    
    @Serializable
    data object Settings : KoColorRoute()

    @Serializable
    data class ColorDetail(val suggestionId: Long) : KoColorRoute()

    @Serializable
    data class NailLab(val colorHex: String, val finish: String) : KoColorRoute()

    @Serializable
    data class FaceLab(val colorHex: String, val category: String) : KoColorRoute()

    @Serializable
    data class Camera(val target: String) : KoColorRoute()

    val icon: ImageVector?
        get() = when (this) {
            Home -> Icons.Default.Home
            Color -> Icons.Default.ColorLens
            Routines -> Icons.Default.Face
            Settings -> Icons.Default.Settings
            else -> null
        }

    val label: String?
        get() = when (this) {
            Home -> "Main"
            Color -> "Color"
            Routines -> "Routines"
            Settings -> "Settings"
            else -> null
        }
}

val topLevelRoutes = listOf(
    KoColorRoute.Home,
    KoColorRoute.Color,
    KoColorRoute.Settings
)
