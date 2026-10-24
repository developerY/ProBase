package com.zoewave.probase.goswift.data

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light Theme"),
    DARK("Dark Theme");

    companion object {
        fun fromLabel(label: String): ThemeMode {
            return entries.find { it.label.equals(label, ignoreCase = true) } ?: SYSTEM
        }
    }
}
