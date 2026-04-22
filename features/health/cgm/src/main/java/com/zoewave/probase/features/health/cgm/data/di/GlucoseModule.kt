package com.zoewave.probase.features.health.cgm.data.di

import com.zoewave.probase.features.health.cgm.data.repository.*
import com.zoewave.probase.core.model.health.GlucoseSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GlucoseModule {

    @Provides
    @Singleton
    @DexcomShare
    fun provideDexcomRepository(repo: DexcomShareRepository): GlucoseRepository = repo

    @Provides
    @Singleton
    @LifeScan
    fun provideLifeScanRepository(repo: LifeScanRepository): GlucoseRepository = repo

    @Provides
    @Singleton
    @AbbottLinkUp
    fun provideAbbottRepository(repo: AbbottLibreLinkUpRepository): GlucoseRepository = repo

    @Provides
    @Singleton
    @MedtronicCareLink
    fun provideMedtronicRepository(repo: MedtronicCareLinkRepository): GlucoseRepository = repo

    @Provides
    @Singleton
    @SiBionics
    fun provideSiBionicsRepository(repo: SiBionicsRepository): GlucoseRepository = repo

    @Provides
    @Singleton
    @Medtrum
    fun provideMedtrumRepository(repo: MedtrumRepository): GlucoseRepository = repo

    @Provides
    @Singleton
    @Ascensia
    fun provideAscensiaRepository(repo: AscensiaRepository): GlucoseRepository = repo

    @Provides
    @Singleton
    @Trividia
    fun provideTrividiaRepository(repo: TrividiaRepository): GlucoseRepository = repo

    @Provides
    @Singleton
    fun provideGlucoseRepositoryFactory(
        @DexcomShare dexcom: Provider<GlucoseRepository>,
        @LifeScan lifeScan: Provider<GlucoseRepository>,
        @AbbottLinkUp abbott: Provider<GlucoseRepository>,
        @MedtronicCareLink medtronic: Provider<GlucoseRepository>,
        @SiBionics siBionics: Provider<GlucoseRepository>,
        @Medtrum medtrum: Provider<GlucoseRepository>,
        @Ascensia ascensia: Provider<GlucoseRepository>,
        @Trividia trividia: Provider<GlucoseRepository>,
        bleRepo: Provider<BleGlucoseRepository>,
        libreRepo: Provider<LibreNfcRepository>
    ): GlucoseRepositoryFactory {
        return object : GlucoseRepositoryFactory {
            override fun create(source: GlucoseSource): GlucoseRepository {
                return when (source) {
                    GlucoseSource.DEXCOM_SHARE -> dexcom.get()
                    GlucoseSource.LIFESCAN_ONETOUCH -> lifeScan.get()
                    GlucoseSource.ABBOTT_LIBRE_LINK_UP -> abbott.get()
                    GlucoseSource.MEDTRONIC_CARELINK -> medtronic.get()
                    GlucoseSource.SIBIONICS -> siBionics.get()
                    GlucoseSource.MEDTRUM -> medtrum.get()
                    GlucoseSource.ASCENSIA_CONTOUR -> ascensia.get()
                    GlucoseSource.TRIVIDIA_TRUE_METRIX -> trividia.get()
                    GlucoseSource.BLE_STANDARD -> bleRepo.get()
                    GlucoseSource.LIBRE_NFC -> libreRepo.get()
                    else -> bleRepo.get() // Default
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideDefaultGlucoseRepository(
        factory: GlucoseRepositoryFactory
    ): GlucoseRepository {
        return factory.create(GlucoseSource.BLE_STANDARD)
    }
}

interface GlucoseRepositoryFactory {
    fun create(source: GlucoseSource): GlucoseRepository
}
