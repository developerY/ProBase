package com.zoewave.probase.seaweed.model.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.zoewave.probase.seaweed.model.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
enum class TransactionTab {
    RECENT, CYCLIC
}

@Serializable
sealed class SeaweedDestination(
    val title: String? = null,
    @StringRes val titleRes: Int? = null,
    @Transient val icon: ImageVector = Icons.Default.Home
) {
    @Serializable
    data object Home : SeaweedDestination(
        titleRes = R.string.applications_seaweed_model_route_home,
        icon = Icons.Default.Home
    )

    @Serializable
    data object CategoryGrid : SeaweedDestination(
        titleRes = R.string.applications_seaweed_model_route_categories,
        icon = Icons.Default.GridView
    )

    @Serializable
    data object Bills : SeaweedDestination(
        titleRes = R.string.applications_seaweed_model_route_bills,
        icon = Icons.AutoMirrored.Filled.ReceiptLong
    )

    @Serializable
    data object Budget : SeaweedDestination(
        titleRes = R.string.applications_seaweed_model_route_budget,
        icon = Icons.Default.PieChart
    )

    @Serializable
    data class Transactions(
        val category: String? = null,
        val transactionId: String? = null,
        val initialTab: TransactionTab = TransactionTab.RECENT
    ) : SeaweedDestination(
        titleRes = R.string.applications_seaweed_model_route_transactions,
        icon = Icons.AutoMirrored.Filled.List
    )

    @Serializable
    data object AddTransaction : SeaweedDestination(
        titleRes = R.string.applications_seaweed_model_route_add_transaction,
        icon = Icons.Default.Add
    )

    @Serializable
    data object Settings : SeaweedDestination(
        titleRes = R.string.applications_seaweed_model_route_settings,
        icon = Icons.Default.Settings
    )

    @Serializable
    data object Analytics : SeaweedDestination(
        titleRes = R.string.applications_seaweed_model_route_analytics,
        icon = Icons.Default.PieChart // We can use Analytics or PieChart
    )

    @Serializable
    data object Camera : SeaweedDestination(
        titleRes = R.string.applications_seaweed_model_route_camera,
        icon = Icons.Default.Home // Fallback icon
    )

    @Serializable
    data class SmartReceipt(val photoUri: String) : SeaweedDestination(
        titleRes = R.string.applications_seaweed_model_route_smart_receipt,
        icon = Icons.Default.Home
    )

    @Serializable
    data class SmartReceiptDebug(
        val rawResponse: String,
        val logs: List<String>,
        val engineUsed: String,
        val whatIsThis: String? = null
    ) : SeaweedDestination(
        titleRes = R.string.applications_seaweed_model_route_debug_ai,
        icon = Icons.Default.Settings
    )
}

val topLevelDestinations = listOf(
    SeaweedDestination.Home,
    SeaweedDestination.Transactions(),
    SeaweedDestination.Settings
)
