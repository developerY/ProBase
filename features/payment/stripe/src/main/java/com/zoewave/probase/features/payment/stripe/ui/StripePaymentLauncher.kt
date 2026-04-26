package com.zoewave.probase.features.payment.stripe.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.zoewave.probase.features.payment.stripe.BuildConfig

/**
 * Global access to a pre-registered Stripe PaymentSheet.
 * This must be initialized at the Activity level to avoid lifecycle registration crashes.
 */
val LocalStripeLauncher = staticCompositionLocalOf<PaymentSheet?> { null }

@Composable
fun StripePaymentProvider(
    publishableKey: String = BuildConfig.STRIPE_PUBLISHABLE_KEY,
    onResult: (PaymentSheetResult) -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    
    // 1. Initialize Stripe Configuration (Required by Docs)
    LaunchedEffect(publishableKey) {
        if (publishableKey.isNotBlank()) {
            PaymentConfiguration.init(context, publishableKey)
        }
    }

    // 2. Register the launcher during early composition
    val launcher = if (activity != null) {
        remember {
            PaymentSheet.Builder(onResult).build(activity)
        }
    } else null

    CompositionLocalProvider(LocalStripeLauncher provides launcher) {
        content()
    }
}

fun PaymentSheet.presentSeaweedPayment(
    clientSecret: String,
    merchantName: String = "Seaweed Finance"
) {
    this.presentWithPaymentIntent(
        paymentIntentClientSecret = clientSecret,
        configuration = PaymentSheet.Configuration(
            merchantDisplayName = merchantName,
            allowsDelayedPaymentMethods = true
        )
    )
}
