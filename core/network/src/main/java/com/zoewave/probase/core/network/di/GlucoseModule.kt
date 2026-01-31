package com.zoewave.probase.core.network.di

// Note the specific imports
import com.zoewave.probase.core.network.repository.sensor.glucose.BleGlucoseRepository
import com.zoewave.probase.core.network.repository.sensor.glucose.GlucoseRepository
import com.zoewave.probase.core.network.repository.sensor.glucose.LibreNfcRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GlucoseModule {

    @Provides
    @Singleton
    fun provideGlucoseRepository(
        // You can inject both implementations if you need a switching logic
        bleRepo: BleGlucoseRepository,
        libreRepo: LibreNfcRepository
    ): GlucoseRepository {
        // For now, returning Libre as the primary source
        return libreRepo
    }
}