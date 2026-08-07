plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zoewave.probase.kocolor.features.starterpack"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":applications:kocolor:model"))
    implementation(project(":applications:kocolor:db"))
    implementation(project(":applications:kocolor:data"))
    implementation(project(":applications:kocolor:apps:mobile:core"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:util"))

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.squareup.retrofit)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.coil.compose)

    implementation("org.bouncycastle:bcprov-jdk18on:1.77")
    implementation(libs.zstdJni)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
}
