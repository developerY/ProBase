plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zoewave.probase.kocolor.data"
}

dependencies {
    implementation(project(":applications:kocolor:model"))
    implementation(project(":applications:kocolor:db"))
    api(project(":core:model"))
    api(project(":core:data"))
    implementation(project(":core:util"))
    implementation(project(":features:ai:configuration"))
    implementation(project(":features:ai:local"))

    implementation(libs.room.runtime)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.google.generative.ai)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.work.runtime.ktx)
    implementation("androidx.palette:palette-ktx:1.0.0")

    implementation(libs.hilt.android)
    implementation(libs.hilt.work)
    ksp(libs.hilt.android.compiler)

    implementation(libs.squareup.retrofit)
    implementation(libs.retrofit.kotlinx.serialization.converter)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.junit)
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("org.robolectric:robolectric:4.14")
}
