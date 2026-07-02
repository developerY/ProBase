plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.kocolor.features.cosmetics"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:network"))
    implementation(project(":core:data"))
    implementation(project(":applications:kocolor:model"))
    implementation(project(":applications:kocolor:db"))
    implementation(project(":applications:kocolor:data"))
    implementation(project(":applications:kocolor:features:analyzer"))
    implementation(project(":features:camera:productcapture"))
    implementation(project(":features:ai:configuration"))
    implementation(project(":features:graphics"))
    implementation(project(":features:obf"))
    implementation(project(":applications:kocolor:features:fda"))
    implementation(project(":applications:kocolor:features:chemicals"))
    implementation(project(":applications:kocolor:features:makeupapi"))

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.coil.compose)
    implementation(libs.squareup.retrofit)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.squareup.okhttp)
    implementation(libs.squareup.okhttp3.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collections.immutable)
}
