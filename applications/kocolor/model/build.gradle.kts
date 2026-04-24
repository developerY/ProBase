plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.kotlin.serialization")
    id("kotlin-parcelize")
}

android {
    namespace = "com.zoewave.probase.kocolor.model"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.androidx.compose.material.icons.extended)
}
