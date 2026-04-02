plugins {
    id("composetemplate.android.application")
    id("composetemplate.android.application.compose")
    id("composetemplate.android.hilt")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zoewave.probase.photodo.wear"

    defaultConfig {
        applicationId = "com.zoewave.probase.photodo"
        versionCode = 1
        versionName = "0.0.1"
        minSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
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
            isShrinkResources = providers.gradleProperty("isShrinkResources").getOrElse("false").toBoolean()
        }
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:model"))

    implementation(project(":applications:photodo:model"))
    implementation(project(":applications:photodo:db"))
    implementation(project(":applications:photodo:data"))

    // Wear UI & Navigation
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.hilt.navigation.compose)

    // Horologist
    implementation(libs.horologist.compose.layout)
    implementation(libs.horologist.compose.tools)

    // Image loading
    implementation(libs.coil.compose)
}
