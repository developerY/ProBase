plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.kocolor.mobile.features.settings"
}

dependencies {
    implementation(project(":applications:kocolor:data"))
    implementation(project(":applications:kocolor:db"))
    implementation(project(":applications:kocolor:model"))
    implementation(project(":applications:kocolor:apps:mobile:core"))
    implementation(project(":features:ai:configuration"))
    implementation(project(":features:ai:capture"))
    implementation(project(":features:health:core"))
    implementation(project(":core:util"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.kotlinx.collections.immutable)
}
