package com.zoewave.probase.features.payment.stripe.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

@Composable
fun rememberSeaweedStripeLauncher(
    onResult: (PaymentSheetResult) -> Unit
): PaymentSheet {
    val context = LocalContext.current as ComponentActivity
    return remember {
        PaymentSheet.Builder(onResult).build(context)
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
