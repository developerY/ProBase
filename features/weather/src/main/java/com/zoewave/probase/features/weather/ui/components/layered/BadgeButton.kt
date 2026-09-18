package com.zoewave.probase.features.weather.ui.components.layered

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class BadgeButton {

    @Composable
    fun ShortHeader(
        onUvClick: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP HALF: Weather Widget
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    //.clickable(onClick = onWeatherClick)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.Center
            ) {


                // UV Badge Button (Tactile 3D push-button feel)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = (-12).dp)
                        .size(56.dp)
                        .clickable { onUvClick() },
                    shape = CircleShape,
                    color = Color(0xFFFAF6F0),
                    border = BorderStroke(1.5.dp, Color.White),
                    shadowElevation = 6.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0xFFDCC8A8).copy(alpha = 0.5f), CircleShape)
                        )

                        // Top-Right Micro Action Arrow ↗
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open UV Page",
                            tint = Color(0xFF4A2840),
                            modifier = Modifier
                                .size(9.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-10).dp, y = 10.dp)
                                .rotate(-45f)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "UV",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B5A52),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text =  "3",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1A1A1A)
                            )
                            // Base Tap Indicator Bar
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(width = 12.dp, height = 2.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFBCAAA4))
                            )
                        }
                    }
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun ShortHeaderPreview() {
    MaterialTheme {
        BadgeButton().ShortHeader(
            onUvClick = {}
        )
    }
}