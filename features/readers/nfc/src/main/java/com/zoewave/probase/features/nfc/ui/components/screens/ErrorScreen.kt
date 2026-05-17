package com.zoewave.probase.features.readers.nfc.ui.components.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
////import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.features.readers.nfc.R
import com.zoewave.probase.core.ui.R as CoreUiR

@Composable
fun ErrorScreen(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black), // Background color can be adjusted
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.features_nfc_nfc_status_error, message),
                color = Color.Red,
                fontSize = 18.sp,
                //fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            onRetry?.let {
                Button(
                    onClick = it,
                    colors = ButtonDefaults.buttonColors(
                        //backgroundColor = Color.Red
                    )
                ) {
                    Text(
                        text = stringResource(CoreUiR.string.core_ui_action_retry),
                        color = Color.White,
                        fontSize = 18.sp,
                        //fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                        //fontSize = 16.sp,
                        //fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
/*
@Preview
@Composable
fun ErrorScreenPreview() {
    ErrorScreen(
        message = "Some Error",
        onRetry = { println("Retrying...") }
    )
}
*/
