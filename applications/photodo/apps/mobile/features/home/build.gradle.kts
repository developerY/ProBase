plugins {
    // ✅ 1. Apply Convention Plugins (Library, Compose, Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    // Unique namespace for the PhotoTodo Home Feature
    namespace = "com.zoewave.probase.photodo.mobile.features.home"
}

dependencies {
    // --- Shared Core Projects ---
    // implementation(project(":core:model"))
    // implementation(project(":core:ui"))
    // implementation(project(":core:network"))
    // implementation(project(":core:data"))
    implementation(project(":applications:photodo:db"))
    implementation(project(":features:ai:configuration"))

    implementation(project(":applications:photodo:model"))
    implementation(project(":applications:photodo:apps:mobile:core"))
    implementation(project(":applications:photodo:apps:mobile:features:tasks"))

    // --- Navigation 3 (Strict) ---
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.window.size)

    // --- Standard UI & Compose ---
    // Note: 'library.compose' plugin adds the BOM and basic UI/Tooling automatically.
    // We only add specific extras here.
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)

    // androidx-compose-material3-expressive
    implementation(libs.androidx.compose.material3.expressive)

    // --- Hilt ---
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    // Note: The Hilt Convention Plugin handles 'ksp(libs.hilt.compiler)' automatically

    // --- Lifecycle ---
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.service)

    // --- Collections ---
    implementation(libs.kotlinx.collections.immutable)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}