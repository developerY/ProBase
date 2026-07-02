plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.kocolor.features.obf"
}

dependencies {
    // --- Internal Modules ---
    implementation(project(":core:model"))
    implementation(project(":core:util"))
    implementation(project(":core:network"))
    implementation(project(":applications:kocolor:model"))

    // --- Core Android ---
    implementation(libs.androidx.core.ktx)

    // --- Networking ---
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.converter.gson)
    implementation(libs.squareup.okhttp3.logging.interceptor)

    // --- Hilt ---
    // Handled by convention plugin

    // --- Coroutines ---
    implementation(libs.kotlinx.coroutines.android)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}
