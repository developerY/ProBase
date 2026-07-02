plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.kocolor.features.makeupapi"
}

dependencies {
    implementation(project(":core:util"))

    implementation(libs.squareup.retrofit)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.squareup.okhttp)
    implementation(libs.squareup.okhttp3.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
}
