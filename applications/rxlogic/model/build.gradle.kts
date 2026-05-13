plugins {
    id("composetemplate.android.library")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.rxlogic.model"
}

dependencies {
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
}
