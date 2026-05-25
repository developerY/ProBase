package com.zoewave.probase.features.xr.glass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.ListItem
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.list.GlimmerLazyColumn

enum class GlimmerSample(val title: String) {
    Buttons("Buttons"),
    Cards("Cards"),
    Colors("Colors"),
    Depth("Depth Effect Levels"),
    LazyList("Glimmer Lazy List"),
    Pager("Glimmer Pager"),
    IconButtons("Icon Buttons"),
    Icons("Icons"),
    IconToggleButtons("Icon Toggle Buttons"),
    IndirectPointer("Indirect Pointer Gestures"),
    ListItems("List Items"),
    Shapes("Shapes"),
    Stacks("Stacks"),
    Surface("Surface"),
    TitleChips("Title Chips"),
    ToggleButtons("Toggle Buttons"),
    Typography("Typography"),
    VoiceIndicator("Voice Input Indicator"),
    Ritual("Morning Ritual Layout");

    fun next(): GlimmerSample {
        val entries = entries
        val index = entries.indexOf(this)
        return entries[(index + 1) % entries.size]
    }

    fun previous(): GlimmerSample {
        val entries = entries
        val index = entries.indexOf(this)
        return entries[(index - 1 + entries.size) % entries.size]
    }
}

@Composable
fun SamplesMenu(
    onSampleSelected: (GlimmerSample) -> Unit,
    modifier: Modifier = Modifier
) {
    GlimmerLazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "Glimmer Samples",
                style = GlimmerTheme.typography.titleLarge,
                color = GlimmerTheme.colors.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        GlimmerSample.entries.forEach { sample ->
            item {
                ListItem(
                    onClick = { onSampleSelected(sample) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = sample.title,
                        style = GlimmerTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}
