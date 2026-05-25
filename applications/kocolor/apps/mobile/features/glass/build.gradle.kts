plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.kocolor.mobile.glass"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":applications:kocolor:data"))
    implementation(project(":applications:kocolor:model"))
    implementation(project(":applications:kocolor:db"))

    implementation(libs.androidx.glimmer.core)
    implementation(libs.androidx.glimmer.google.fonts)
    implementation(libs.androidx.projected)
    implementation(libs.androidx.xr.runtime)
    implementation(libs.androidx.xr.arcore)

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    implementation(libs.androidx.lifecycle.process)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
