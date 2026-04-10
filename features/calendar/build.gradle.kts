plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
    id("composetemplate.android.room")
}

android {
    namespace = "com.zoewave.probase.features.calendar"
}

dependencies {
    // --- Shared Core Projects ---
    implementation(project(":core:model"))
    implementation(project(":core:util"))

    // --- Third Party ---
    implementation(libs.kotlinx.coroutines.android)
}
