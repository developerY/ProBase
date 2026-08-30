# FASHIONISTA Core Domain, Scoring, & Presentation Data Models
-keep class com.zoewave.probase.kocolor.fashionista.domain.** { *; }
-keep class com.zoewave.probase.kocolor.fashionista.scoring.** { *; }
-keep class com.zoewave.probase.kocolor.fashionista.presentation.** { *; }

# KoColor Style & Color Models
-keep class com.zoewave.probase.kocolor.data.usecase.StyleBlueprint { *; }
-keep class com.zoewave.probase.kocolor.data.usecase.StyleRequestContext { *; }
-keep class com.zoewave.probase.kocolor.data.color.CandidateProvenance { *; }
-keep class com.zoewave.probase.kocolor.data.color.CompositeColorProfile { *; }

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
