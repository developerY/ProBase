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
    api(project(":features:ai:configuration"))
    api(project(":features:ai:capture"))
    implementation(project(":core:data"))
    implementation(project(":core:util"))
    implementation(libs.androidx.core.ktx)
    
    implementation(libs.tinkAndroid)
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")
    
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    implementation(project(":features:readers:ocr"))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":features:ai:core"))
}
