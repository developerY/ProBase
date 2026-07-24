plugins {
    id("composetemplate.android.application")
    id("composetemplate.android.application.compose")
    id("composetemplate.android.hilt")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zoewave.probase.seaweed.wear"

    defaultConfig {
        applicationId = "com.zoewave.probase.seaweed"
        versionCode = 4
        versionName = "0.0.1"
        minSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            val storeFileProp = providers.gradleProperty("RELEASE_STORE_FILE").orNull
            val storePasswordProp = providers.gradleProperty("RELEASE_STORE_PASSWORD").orNull
            val keyAliasProp = providers.gradleProperty("RELEASE_KEY_ALIAS").orNull
            val keyPasswordProp = providers.gradleProperty("RELEASE_KEY_PASSWORD").orNull

            if (!storeFileProp.isNullOrEmpty() && !storePasswordProp.isNullOrEmpty()) {
                storeFile = file(storeFileProp)
                storePassword = storePasswordProp
                keyAlias = keyAliasProp
                keyPassword = keyPasswordProp
            }
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:model"))

    implementation(project(":applications:seaweed:model"))
    implementation(project(":applications:seaweed:database"))
    implementation(project(":applications:seaweed:data"))
    implementation(project(":applications:seaweed:features:main"))

    // Wear Feature Modules
    implementation(project(":applications:seaweed:apps:wear:features:home"))
    implementation(project(":applications:seaweed:apps:wear:features:transactions"))
    implementation(project(":applications:seaweed:apps:wear:features:bills"))

    // Wear UI & Navigation
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    // Horologist
    implementation(libs.horologist.compose.layout)
    implementation(libs.horologist.compose.tools)
}
