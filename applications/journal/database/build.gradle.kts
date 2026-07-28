plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.room")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.applications.journal.database"
}

dependencies {
    implementation(project(":applications:journal:model"))
}
