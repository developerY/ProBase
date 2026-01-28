plugins {
    `kotlin-dsl`
}

group = "com.zoewave.probase.convention"

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
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
            implementationClass = "com.zoewave.probase.convention.AndroidComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "composetemplate.android.library"
            implementationClass = "com.zoewave.probase.convention.AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "composetemplate.android.library.compose"
            implementationClass = "com.zoewave.probase.convention.AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "composetemplate.android.hilt"
            implementationClass = "com.zoewave.probase.convention.AndroidHiltConventionPlugin"
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
