plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
    id("composetemplate.android.room")
}

android {
    namespace = "com.zoewave.probase.rxlogic.db"
}

dependencies {
    implementation(project(":applications:rxlogic:model"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
}
