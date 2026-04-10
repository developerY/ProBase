plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
    id("composetemplate.android.library.compose")
}

android {
    namespace = "com.zoewave.probase.photodo.features.calendar"
}

dependencies {
    // --- Shared Core Projects ---
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:util"))

    // --- Core Features ---
    implementation(project(":features:calendar"))

    // --- PhotoTodo Modules ---
    implementation(project(":applications:photodo:db"))
    implementation(project(":applications:photodo:model"))

    // --- Third Party ---
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose)
}
