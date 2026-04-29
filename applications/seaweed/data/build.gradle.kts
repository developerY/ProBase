plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.seaweed.data"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":applications:seaweed:model"))
    implementation(project(":applications:seaweed:database"))
    implementation(project(":features:ai:configuration"))
    implementation(project(":features:ai:capture"))
    implementation(project(":core:data"))
    implementation(project(":core:util"))
    implementation(libs.androidx.core.ktx)
}
