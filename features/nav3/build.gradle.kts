plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    // From your TOML: [plugins] jetbrains-kotlin-serialization
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.zoewave.probase.features.nav3"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi")
    }
}

dependencies {
    // Nav 3 Libraries from your TOML
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    // Serialization for Keys
    implementation(libs.kotlinx.serialization.core)

    // Optional: ViewModel support (You have this in TOML, good to include)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // The library containing the Adaptive APIs
    implementation(libs.androidx.material3.adaptive.navigation3)
}