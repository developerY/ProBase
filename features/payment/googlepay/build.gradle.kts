plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.features.payment.googlepay"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    
    implementation(libs.google.play.services.wallet)
    implementation(libs.google.pay.button.compose)
    implementation(libs.androidx.compose.material3)
}
