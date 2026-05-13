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
    implementation(project(":applications:rxlogic:features:reminders"))
    implementation(project(":core:ui"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
}
