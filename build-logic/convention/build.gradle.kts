plugins {
    `kotlin-dsl`
}

group = "com.zoewave.probase.convention"

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.compose.compiler.gradlePlugin)

    implementation(libs.hilt.gradlePlugin)
    implementation(libs.kotlin.serialization.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("createNewApp") {
            id = "composetemplate.create.new.app"
            implementationClass = "com.zoewave.probase.convention.CreateNewAppPlugin"
        }
        register("androidApplication") {
            id = "composetemplate.android.application"
            implementationClass = "com.zoewave.probase.convention.AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "composetemplate.android.application.compose"
            implementationClass = "com.zoewave.probase.convention.AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "composetemplate.android.library"
            implementationClass = "com.zoewave.probase.convention.AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "composetemplate.android.library.compose"
            implementationClass = "com.zoewave.probase.convention.AndroidLibraryComposeConventionPlugin"
        }
        register("androidRoom") {
            id = "composetemplate.android.room"
            implementationClass = "com.zoewave.probase.convention.AndroidRoomConventionPlugin"
        }
        register("androidHilt") {
            id = "composetemplate.android.hilt"
            implementationClass = "com.zoewave.probase.convention.AndroidHiltConventionPlugin"
        }
        register("kotlinSerialization") {
            id = "composetemplate.kotlin.serialization"
            implementationClass = "com.zoewave.probase.convention.KotlinSerializationConventionPlugin"
        }
        register("test") {
            id = "composetemplate.test"
            implementationClass = "com.zoewave.probase.convention.TestConventionPlugin"
        }
        register("feature") {
            id = "composetemplate.feature"
            implementationClass = "com.zoewave.probase.convention.FeatureConventionPlugin"
        }
    }
}
