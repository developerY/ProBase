# Jetpack Compose Custom Canvas Graphics & Visual Blueprint Components
-keep class com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.** { *; }
-keep class com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.FashionistaMathBreakdown { *; }

# Google Generative AI SDK & Ktor Internal Client Rules
-dontwarn io.ktor.client.plugins.HttpTimeout$HttpTimeoutCapabilityConfiguration
-dontwarn io.ktor.client.plugins.HttpTimeout$Plugin
-dontwarn io.ktor.client.plugins.HttpTimeout
-dontwarn io.ktor.client.plugins.contentnegotiation.ContentNegotiation$Config
-dontwarn io.ktor.client.plugins.contentnegotiation.ContentNegotiation$Plugin
-dontwarn io.ktor.client.plugins.contentnegotiation.ContentNegotiation
-dontwarn io.ktor.**
-dontwarn com.google.ai.client.generativeai.**
-keep class io.ktor.** { *; }
-keep class com.google.ai.client.generativeai.** { *; }
