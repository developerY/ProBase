plugins {
    // ✅ 1. Apply Convention Plugins (Handles AGP 9, Java 21, Hilt, KSP)
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")

    // ✅ 2. Module-Specific Plugins
    alias(libs.plugins.apollo.graphql)
    alias(libs.plugins.mapsplatform.secrets)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    // ✅ 3. Update Namespace to 'zoewave'
    namespace = "com.zoewave.probase.core.network"

    defaultConfig {
        // minSdk & compileSdk are handled by your Convention Plugin
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    // Removed 'compileOptions' & 'buildTypes' -> Handled by Convention Plugin

    buildFeatures {
        buildConfig = true
        // aidl, renderScript, shaders are disabled by default in AGP 9
    }

    // ✅ 4. Update Apollo Package
    apollo {
        service("service") {
            // Generated GraphQL classes will land here
            packageName.set("com.zoewave.probase.core.network")
        }
    }

    secrets {
        defaultPropertiesFileName = "secrets.defaults.properties"
    }
}

dependencies {
    // --- Internal Modules ---
    implementation(project(":core:model"))
    implementation(project(":core:util"))

    // --- Core Android ---
    implementation(libs.androidx.core.ktx)
    // Note: Removed 'appcompat' & 'material.legacy' unless you specifically need Views.
    // Ideally, a pure network module shouldn't depend on UI libraries.

    // --- Data Persistence ---
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)
    implementation(libs.kotlinx.serialization.json)

    // --- Lifecycle ---
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)

    // --- Networking (REST) ---
    implementation(libs.squareup.okhttp)
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.converter.gson)
    implementation(libs.squareup.okhttp3.logging.interceptor)

    // --- Networking (GraphQL) ---
    implementation(libs.apollo.graphql.runtime)
    implementation(libs.apollo.graphql.normalized.cache)

    // --- Platform Integration ---
    implementation(libs.androidx.health.connect.client)
    implementation(libs.androidx.health.services.client)
    implementation(libs.google.maps.compose)
    implementation(libs.google.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)

    // --- Hilt ---
    // Core Hilt dependencies are automatically added by 'composetemplate.android.hilt'

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}