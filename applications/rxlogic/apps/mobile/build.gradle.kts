plugins {
    id("composetemplate.android.application.compose")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.rxlogic.apps.mobile"

    defaultConfig {
        applicationId = "com.zoewave.probase.rxlogic"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":applications:rxlogic:model"))
    implementation(project(":applications:rxlogic:data"))
    implementation(project(":applications:rxlogic:features:daily"))
    implementation(project(":applications:rxlogic:features:medications"))
    implementation(project(":applications:rxlogic:features:settings"))
    implementation(project(":core:ui"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.window.size)
}
