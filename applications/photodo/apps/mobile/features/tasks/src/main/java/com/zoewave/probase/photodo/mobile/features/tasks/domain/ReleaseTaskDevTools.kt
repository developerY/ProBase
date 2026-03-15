package com.zoewave.probase.photodo.mobile.features.tasks.domain

import javax.inject.Inject

class ReleaseTaskDevTools @Inject constructor() : TaskDevTools {
    override suspend fun seedDatabase() {
        // Do absolutely nothing in production.
    }
}