plugins {
    // 1. Android Application Convention (replaces 'com.android.application')
    id("composetemplate.android.application")

    // 2. Compose Convention (adds UI, Graphics, Tooling, Manifest, buildFeatures)
    id("composetemplate.android.application.compose")

    // 3. Hilt Convention (adds Hilt plugin, KSP, and dependencies)
    id("composetemplate.android.hilt")

    // 4. Serialization (Useful for Nav3 arguments)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.zoewave.probase"

    // Convention plugins handle compileSdk, minSdk, and compileOptions.
    // Only keep what is specific to this App artifact.
    defaultConfig {
        applicationId = "com.zoewave.probase"
        versionCode = 1
        versionName = "1.0"

        // You can override the convention default here if needed:
        // minSdk = 36

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // 'compileOptions' and 'buildFeatures { compose = true }'
    // are now removed because the Convention Plugins handle them.
}

dependencies {
    // ✅ FEATURE MODULES
    // This connects your MainActivity to the Feature code we just wrote.
    implementation(project(":features:nav3"))
    // implementation(project(":core:ui"))

    // ✅ CORE DEPENDENCIES
    // (Compose UI/Material3 are added automatically by the Compose Convention)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ✅ TESTING
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}