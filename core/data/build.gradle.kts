plugins {
    // ✅ Apply Convention Plugins to standardize SDK versions & setup
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
    id("composetemplate.android.room")
}

android {
    namespace = "com.zoewave.probase.core.data"

    // Removed 'compileSdk', 'defaultConfig', 'compileOptions'
    // -> These are now handled centrally by the Convention Plugin.
}

dependencies {
    // --- Internal Modules (The Architecture "Glue") ---
    implementation(project(":core:model"))    // Shared Data Models
    implementation(project(":core:database")) // Access to Room DAOs & Entities
    implementation(project(":core:network"))  // Access to Apollo/Retrofit APIs

    // --- Coroutines (Essential for Repositories) ---
    implementation(libs.kotlinx.coroutines.android)

    // Health Connect
    implementation(libs.androidx.health.connect.client)
    implementation(libs.androidx.health.services.client)

    // Maps
    implementation(libs.google.maps.compose)
    implementation(libs.google.play.services.location)

    implementation(libs.kotlinx.coroutines.play.services)

    // GraphQL
    implementation(libs.squareup.okhttp)
    implementation(libs.apollo.graphql.runtime)
    implementation(libs.apollo.graphql.normalized.cache)

    // Retrofit
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.converter.gson)
    implementation(libs.squareup.okhttp3.logging.interceptor)


    // Datastore
    implementation(libs.androidx.datastore.preferences)
    // optional:
    implementation(libs.androidx.datastore.core)
    implementation(libs.kotlinx.serialization.json)

    // --- External Utilities (Optional, based on your Repos) ---
    // If your repositories parse dates/times:
    // implementation(libs.kotlinx.datetime)

    // --- Hilt (Dependency Injection) ---
    // Dependencies are AUTOMATICALLY added by the 'hilt' plugin above.

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}