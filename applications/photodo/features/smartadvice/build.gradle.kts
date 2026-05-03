plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.photodo.features.smartadvice"
}

dependencies {
    // --- Shared Core Projects ---
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:util"))

    // --- PhotoTodo Core ---
    implementation(project(":applications:photodo:db"))
    implementation(project(":applications:photodo:model"))
    implementation(project(":features:ai:configuration"))

    // --- ML & AI ---
    implementation(libs.google.generative.ai)

    // --- Third Party ---
    implementation(libs.compose.markdown)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.serialization.json)
}
