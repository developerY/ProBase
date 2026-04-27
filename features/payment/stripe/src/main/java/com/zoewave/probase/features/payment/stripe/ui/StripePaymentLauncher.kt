package com.zoewave.probase.features.payment.stripe.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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

/**
 * A thread-safe proxy to route payment results from the Activity back to the active Composable.
 */
object StripeResultProxy {
    private var activeCallback: ((PaymentSheetResult) -> Unit)? = null

    fun register(callback: (PaymentSheetResult) -> Unit) {
        activeCallback = callback
    }

    fun unregister() {
        activeCallback = null
    }

    fun onResult(result: PaymentSheetResult) {
        activeCallback?.invoke(result)
    }
}

@Composable
fun StripePaymentProvider(
    launcher: PaymentSheet?,
    publishableKey: String = BuildConfig.STRIPE_PUBLISHABLE_KEY,
    onResult: (PaymentSheetResult) -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    // 1. Initialize Stripe Configuration
    LaunchedEffect(publishableKey) {
        if (publishableKey.isNotBlank()) {
            PaymentConfiguration.init(context, publishableKey)
        }
    }

    // 2. Register the callback proxy
    DisposableEffect(onResult) {
        StripeResultProxy.register(onResult)
        onDispose {
            StripeResultProxy.unregister()
        }
    }

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
