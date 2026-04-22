plugins {
    id("composetemplate.android.library")
}

android {
    namespace = "com.zoewave.probase.features.health"
}

dependencies {
    // This is a container module. Sub-modules are listed below for visibility.
    // It doesn't necessarily need to depend on them if nothing uses this module directly.
    // However, typically the 'app' or other modules will depend on the sub-modules directly.
    api(project(":features:health:core"))
    api(project(":features:health:cgm"))
}
