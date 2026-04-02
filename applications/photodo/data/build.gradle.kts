plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zoewave.probase.photodo.data"
}

dependencies {
    implementation(project(":applications:photodo:db"))
    implementation(project(":applications:photodo:model"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.google.play.services.wearable)
    implementation(libs.squareup.retrofit.converter.gson)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
}
