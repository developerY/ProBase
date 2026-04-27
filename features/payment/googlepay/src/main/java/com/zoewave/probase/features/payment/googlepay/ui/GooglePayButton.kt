package com.zoewave.probase.features.payment.googlepay.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.pay.button.PayButton
import com.google.pay.button.ButtonType
import com.google.pay.button.ButtonTheme

@Composable
fun SeaweedGooglePayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    PayButton(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        onClick = onClick,
        enabled = enabled,
        allowedPaymentMethods = """[{"type":"CARD","parameters":{"allowedAuthMethods":["PAN_ONLY","CRYPTOGRAM_3DS"],"allowedCardNetworks":["AMEX","DISCOVER","INTERAC","JCB","MASTERCARD","VISA"]}}]""",
        theme = ButtonTheme.Dark,
        type = ButtonType.Buy
    )
}
