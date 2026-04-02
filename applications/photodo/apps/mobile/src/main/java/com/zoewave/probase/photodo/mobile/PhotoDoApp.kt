package com.zoewave.probase.photodo.mobile

import android.app.Application
import com.zoewave.probase.photodo.data.PhotoDoSyncEngine
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PhotoDoApp : Application() {

    @Inject
    lateinit var syncEngine: PhotoDoSyncEngine

    override fun onCreate() {
        super.onCreate()
        syncEngine.startSyncing()
    }
}
