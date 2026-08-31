# FASHIONISTA Core Domain, Scoring, & Presentation Data Models
-keep class com.zoewave.probase.kocolor.fashionista.domain.** { *; }
-keep class com.zoewave.probase.kocolor.fashionista.scoring.** { *; }
-keep class com.zoewave.probase.kocolor.fashionista.presentation.** { *; }

# Jetpack Compose Custom Canvas Graphics & Visual Blueprint Components
-keep class com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.** { *; }
-keep class com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.FashionistaMathBreakdown { *; }

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

# R8 Automatic Log Stripping for Release Builds
# Strips all Log.v(), Log.d(), and Log.isLoggable(..., DEBUG) calls in production
-maximumremovedandroidloglevel 3

-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
}
