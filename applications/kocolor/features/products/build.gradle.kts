plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.kocolor.features.products"
}

dependencies {
    implementation(project(":core:network"))
    implementation(project(":core:util"))

    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.converter.gson)
    implementation(libs.squareup.okhttp3.logging.interceptor)
}
