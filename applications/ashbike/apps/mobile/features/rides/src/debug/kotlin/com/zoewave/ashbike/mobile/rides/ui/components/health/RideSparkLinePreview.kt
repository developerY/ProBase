package com.zoewave.ashbike.mobile.rides.ui.components.health

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Make sure to import your RideSparkLine composable
// import com.zoewave.probase.ashbike.features.main.ui.components.RideSparkLine

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable fun RideSparkLinePreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("1. Flat Ride (Consistent Effort)")
            SparkLineContainer {
                RideSparkLine(
                    dataPoints = listOf(20f, 22f, 21f, 23f, 22f, 21f, 22f, 22f, 23f, 21f),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    lineColor = Color(0xFF2E7D32) // Green
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("2. Hill Climb (Increasing Effort)")
            SparkLineContainer {
                RideSparkLine(
                    dataPoints = listOf(10f, 12f, 15f, 18f, 20f, 25f, 30f, 15f, 40f, 10f),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    lineColor = Color(0xFFC62828) // Red
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("3. Interval Training (Spiky)")
            SparkLineContainer {
                RideSparkLine(
                    dataPoints = listOf(10f, 40f, 10f, 45f, 12f, 50f, 15f, 40f, 10f, 10f),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    lineColor = Color(0xFF1565C0) // Blue
                )
            }
        }
    }
}

// Helper container to mimic a Card
@Composable
fun SparkLineContainer(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}