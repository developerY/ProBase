plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.kocolor.mobile.features.home"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":applications:kocolor:data"))
    implementation(project(":applications:kocolor:model"))
    implementation(project(":applications:kocolor:db"))
    implementation(project(":applications:kocolor:apps:mobile:core"))
    implementation(project(":applications:kocolor:features:analyzer"))
    implementation(project(":applications:kocolor:features:suggestions"))
    implementation(project(":applications:kocolor:features:routines"))
    implementation(project(":features:ai:configuration"))
    implementation(project(":features:graphics"))
    implementation(project(":features:health:core"))
    implementation(project(":features:weather"))
    implementation(project(":core:network"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))

    implementation(libs.androidx.health.connect.client)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.material3.expressive)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.coil.compose)
    implementation(libs.google.play.services.maps)

    implementation(libs.kotlinx.collections.immutable)
}
