plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.seaweed.features.affordability"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:model"))

    implementation(project(":applications:seaweed:model"))
    implementation(project(":applications:seaweed:data"))
    implementation(project(":applications:seaweed:features:spendingcontrol"))
    
    implementation(project(":features:ai:capture"))
    implementation(project(":features:ai:vision"))
    implementation(project(":features:ai:configuration"))

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
}
