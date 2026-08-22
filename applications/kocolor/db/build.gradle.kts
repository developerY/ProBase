plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
    id("composetemplate.android.room")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.kocolor.db"
}

dependencies {
    implementation(project(":applications:kocolor:model"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":features:ai:capture"))
    implementation(project(":features:ai:configuration"))
    
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)
    implementation(libs.kotlinx.datetime)

    testImplementation(libs.junit)
}
