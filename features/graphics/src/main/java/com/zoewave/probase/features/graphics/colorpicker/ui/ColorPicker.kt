package com.zoewave.probase.features.graphics.colorpicker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismissRequest: () -> Unit,
    title: String = "Pick a Color"
) {
    val controller = rememberColorPickerController(initialColor = initialColor)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Preview Tile
                AlphaTile(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    controller = controller
                )

                // The Main HSV Color Wheel/Picker
                HsvColorPicker(
                    modifier = Modifier
                        .size(240.dp)
                        .padding(10.dp),
                    controller = controller
                )

                // Brightness Slider
                Column {
                    Text("Brightness", style = MaterialTheme.typography.labelSmall)
                    BrightnessSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp),
                        controller = controller
                    )
                }

                Text(
                    text = String.format("#%06X", (0xFFFFFF and controller.selectedColor.value.toArgb())),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                onColorSelected(controller.selectedColor.value)
                onDismissRequest()
            }) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel") }
        }
    )
}

@Stable
class ColorPickerController(initialColor: Color) {
    var selectedColor = mutableStateOf(initialColor)
    
    // Internal HSV state for the picker logic
    var hue = mutableFloatStateOf(0f)
    var saturation = mutableFloatStateOf(0f)
    var value = mutableFloatStateOf(1f)

    init {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(
            android.graphics.Color.rgb(
                (initialColor.red * 255).toInt(),
                (initialColor.green * 255).toInt(),
                (initialColor.blue * 255).toInt()
            ),
            hsv
        )
        hue.floatValue = hsv[0]
        saturation.floatValue = hsv[1]
        value.floatValue = hsv[2]
    }

    fun updateFromHsv() {
        val argb = android.graphics.Color.HSVToColor(floatArrayOf(hue.floatValue, saturation.floatValue, value.floatValue))
        selectedColor.value = Color(argb)
    }
}

@Composable
fun rememberColorPickerController(initialColor: Color): ColorPickerController {
    return remember(initialColor) { ColorPickerController(initialColor) }
}

@Composable
fun AlphaTile(
    modifier: Modifier,
    controller: ColorPickerController
) {
    Box(
        modifier = modifier.background(controller.selectedColor.value)
    )
}

@Composable
fun HsvColorPicker(
    modifier: Modifier,
    controller: ColorPickerController
) {
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val center = Offset(w / 2f, h / 2f)
                    val pos = change.position
                    
                    val dx = pos.x - center.x
                    val dy = pos.y - center.y
                    val radius = w / 2f
                    
                    val dist = kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                    val angle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    
                    controller.hue.floatValue = (if (angle < 0) angle + 360 else angle)
                    controller.saturation.floatValue = (dist / radius).coerceIn(0f, 1f)
                    controller.updateFromHsv()
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val radius = w / 2f
        val center = Offset(w / 2f, h / 2f)
        
        // Draw the color wheel background (simple radial sweep)
        for (i in 0 until 360) {
            val angle = Math.toRadians(i.toDouble()).toFloat()
            val start = center
            val end = Offset(
                center.x + kotlin.math.cos(angle.toDouble()).toFloat() * radius,
                center.y + kotlin.math.sin(angle.toDouble()).toFloat() * radius
            )
            val hsvColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f)))
            
            drawLine(
                brush = Brush.linearGradient(listOf(Color.White, hsvColor), start, end),
                start = start,
                end = end,
                strokeWidth = 2f
            )
        }
        
        // Draw the indicator
        val indicatorAngle = Math.toRadians(controller.hue.floatValue.toDouble()).toFloat()
        val indicatorRadius = controller.saturation.floatValue * radius
        val indicatorOffset = Offset(
            center.x + kotlin.math.cos(indicatorAngle.toDouble()).toFloat() * indicatorRadius,
            center.y + kotlin.math.sin(indicatorAngle.toDouble()).toFloat() * indicatorRadius
        )
        
        drawCircle(
            color = if (controller.value.floatValue > 0.5f) Color.Black else Color.White,
            radius = 8.dp.toPx(),
            center = indicatorOffset,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun BrightnessSlider(
    modifier: Modifier,
    controller: ColorPickerController
) {
    Slider(
        value = controller.value.floatValue,
        onValueChange = { 
            controller.value.floatValue = it
            controller.updateFromHsv()
        },
        valueRange = 0f..1f,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = controller.selectedColor.value,
            activeTrackColor = controller.selectedColor.value.copy(alpha = 0.5f)
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun ColorPickerDialogPreview() {
    MaterialTheme {
        ColorPickerDialog(
            initialColor = Color.Red,
            onColorSelected = {},
            onDismissRequest = {}
        )
    }
}
