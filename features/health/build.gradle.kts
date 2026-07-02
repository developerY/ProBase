plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.features.health"
}

dependencies {
    // This is a container module. Sub-modules are listed below for visibility.
    api(project(":features:health:core"))
    api(project(":features:health:cgm"))

    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
