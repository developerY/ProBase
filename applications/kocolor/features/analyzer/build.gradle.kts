plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.kocolor.features.analyzer"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":applications:kocolor:model"))
    implementation(project(":applications:kocolor:data"))
    implementation(project(":applications:kocolor:db"))
    implementation(project(":features:ai:configuration"))
    implementation(project(":features:camera"))
    implementation(project(":features:graphics"))

    implementation(libs.google.generative.ai)
    implementation(libs.google.play.services.maps)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.coil.compose)
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}
