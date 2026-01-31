plugins {
    // ✅ 1. Apply Convention Plugin
    // Handles AGP 9, Java 21, and standard Android configuration
    id("composetemplate.android.library")
}

android {
    // ✅ 2. Update Namespace
    namespace = "com.zoewave.probase.core.util"

    defaultConfig {
        // compileSdk & minSdk are handled automatically by the plugin
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    // ✅ 3. Cleanup
    // Removed 'buildTypes', 'compileOptions', and 'java { toolchain }'
    // because 'composetemplate.android.library' configures them for you.
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Kept these as they were in your original file,
    // but if this is a "pure" utility module (no UI), you might not need them.
    // implementation(libs.androidx.appcompat)
    // implementation(libs.material.legacy)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}