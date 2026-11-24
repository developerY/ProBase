plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.ashbike.features.ble"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":applications:ashbike:model"))

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
