plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.seaweed.features.receiptcapture"
}

dependencies {
    // --- Shared Core Projects ---
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:data"))
    implementation(project(":features:ai:capture"))
    implementation(project(":features:ai:vision"))
    implementation(project(":features:ai:configuration"))

    // --- Seaweed Core ---
    implementation(project(":applications:seaweed:model"))
    implementation(project(":applications:seaweed:database"))
    implementation(project(":applications:seaweed:data"))

    // --- ML & AI ---
    implementation(libs.mlkit.text.recognition)
    implementation(libs.google.generative.ai)

    implementation(libs.androidx.activity.compose)


    // --- Third Party ---
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
}
