plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.seaweed.mobile.transaction"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":core:util"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":applications:seaweed:model"))
    implementation(project(":applications:seaweed:data"))
    implementation(project(":applications:seaweed:features:main"))
    implementation(project(":applications:seaweed:apps:mobile:features:bills"))
    implementation(project(":features:payment:googlepay"))
    implementation(project(":features:payment:stripe"))
    implementation(project(":features:ai:vision"))
    implementation(project(":features:ai:capture"))
    implementation(project(":features:ai:configuration"))

    implementation(libs.coil.compose)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.google.ai.edge.aicore)
    implementation(libs.google.generative.ai)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.google.play.services.location)
    implementation(libs.google.maps.compose)
}
