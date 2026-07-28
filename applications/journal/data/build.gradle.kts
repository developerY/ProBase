plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.applications.journal.data"
}

dependencies {
    implementation(project(":applications:journal:model"))
    implementation(project(":applications:journal:database"))
}
