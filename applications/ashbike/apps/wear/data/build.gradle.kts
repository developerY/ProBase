plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
}

android {
    // Unique namespace for the Wear Health Data module
    namespace = "com.zoewave.ashbike.wear.data.health"
}

dependencies {

    // --- Shared Projects ---
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:data"))
    implementation(project(":core:util"))

    // --- AshBike Architecture ---
    // If your shared RideTrackingEngine interface is here, uncomment this:
    // implementation(project(":applications:ashbike:data"))

    implementation(project(":applications:ashbike:database"))
    implementation(project(":applications:ashbike:model"))
    implementation(project(":applications:ashbike:data"))

    // --- Health & Wear Sensors ---
    // Replaced Health Connect with Health Services for live hardware sensor reading
    implementation(libs.androidx.health.wear.services.client)

    // --- Android Base ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.google.play.services.location)
    implementation(libs.google.play.services.wearable)

    implementation(libs.squareup.retrofit.converter.gson)
    implementation((libs.kotlinx.coroutines.play.services))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}