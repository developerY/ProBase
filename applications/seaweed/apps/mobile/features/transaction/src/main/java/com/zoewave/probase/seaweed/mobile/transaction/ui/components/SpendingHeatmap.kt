package com.zoewave.probase.seaweed.mobile.transaction.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun SpendingHeatmap(
    heatmapData: Map<LocalDate, Long>,
    modifier: Modifier = Modifier,
    monthsToDisplay: Int = 3,
    selectedDate: LocalDate? = null,
    onDayClick: (LocalDate) -> Unit = {}
) {
    val currentMonth = YearMonth.now()
    val months = (0 until monthsToDisplay).map { currentMonth.minusMonths(it.toLong()) }.reversed()

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(months) { month ->
            Card(
                modifier = Modifier.width(300.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                MonthHeatmap(
                    month = month,
                    heatmapData = heatmapData,
                    selectedDate = selectedDate,
                    onDayClick = onDayClick,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun MonthHeatmap(
    month: YearMonth,
    heatmapData: Map<LocalDate, Long>,
    selectedDate: LocalDate?,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 for Sunday, 1 for Monday, ..., 6 for Saturday
    
    val maxSpending = (heatmapData.values.maxOrNull() ?: 1L).toFloat()

    Column(modifier = modifier) {
        Text(
            text = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + month.year,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DayOfWeek.entries.rotate(1).forEach { day ->
                Text(
                    text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        var currentDay = 1
        for (week in 0 until 6) {
            if (currentDay > daysInMonth) break
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (dayOfWeek in 0 until 7) {
                    val isDayInMonth = (week > 0 || dayOfWeek >= firstDayOfWeek) && currentDay <= daysInMonth
                    if (isDayInMonth) {
                        val date = month.atDay(currentDay)
                        val spending = heatmapData[date] ?: 0L
                        DayBox(
                            day = currentDay,
                            intensity = (spending.toFloat() / maxSpending).coerceIn(0f, 1f),
                            isSelected = date == selectedDate,
                            onClick = { onDayClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                        currentDay++
                    } else {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

private fun <T> List<T>.rotate(n: Int): List<T> {
    if (isEmpty()) return this
    val shift = n % size
    return drop(shift) + take(shift)
}

@Composable
private fun DayBox(
    day: Int,
    intensity: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseColor = MaterialTheme.colorScheme.primary
    val color = if (intensity == 0f) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    } else {
        baseColor.copy(alpha = 0.2f + (intensity * 0.8f))
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 0.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 10.sp,
            color = when {
                isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
                intensity > 0.6f -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontWeight = if (intensity > 0f || isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
