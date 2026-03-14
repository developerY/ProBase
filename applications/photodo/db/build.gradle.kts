plugins {
    // ✅ 1. Apply Convention Plugins (Library, Hilt, Room)
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
    id("composetemplate.android.room") // Automatically handles Room dependencies and KSP schema generation
}

android {
    // Unique namespace for the PhotoTodo Database Module
    namespace = "com.zoewave.probase.photodo.database"
}

dependencies {
    // --- Shared Core Projects ---
    implementation(project(":core:model"))

    // --- DataStore ---
    // (Kept explicitly since these are specific to this module's storage needs)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)

    // Note: Hilt, Room, KSP, core-ktx, and standard testing dependencies
    // are automatically provided by the 'composetemplate' convention plugins.
}