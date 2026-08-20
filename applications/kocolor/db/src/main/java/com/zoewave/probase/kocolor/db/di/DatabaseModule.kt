package com.zoewave.probase.kocolor.db.di

import android.content.Context
import androidx.room3.Room
import com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.core.data.repository.HydrationSettings
import com.zoewave.probase.core.data.di.AppHydration
import com.zoewave.probase.kocolor.db.KoColorDatabase
import com.zoewave.probase.kocolor.db.KoColorSettings
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.FashionProfileDao
import com.zoewave.probase.kocolor.db.dao.GarmentRotationDao
import com.zoewave.probase.kocolor.db.dao.InstalledPackDao
import com.zoewave.probase.kocolor.db.dao.InventoryDao
import com.zoewave.probase.kocolor.db.dao.ProductDao
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.kocolor.db.dao.SavedSuggestionDao
import com.zoewave.probase.kocolor.db.dao.ShoppingCartDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindAiConfigurationSettings(impl: KoColorSettings): AiConfigurationSettings

    @Binds
    @Singleton
    abstract fun bindSmartCaptureSettings(impl: KoColorSettings): SmartCaptureSettings

    @Binds
    @Singleton
    @AppHydration
    abstract fun bindHydrationSettings(impl: KoColorSettings): HydrationSettings

    companion object {
        @Provides
        @Singleton
        fun provideKoColorDatabase(@ApplicationContext context: Context): KoColorDatabase {
            return Room.databaseBuilder(
                context,
                KoColorDatabase::class.java,
                "kocolor_database"
            )
            .build()
        }

        @Provides
        fun provideFashionProfileDao(db: KoColorDatabase): FashionProfileDao = db.fashionProfileDao

        @Provides
        fun provideSavedSuggestionDao(db: KoColorDatabase): SavedSuggestionDao = db.savedSuggestionDao

        @Provides
        fun provideInventoryDao(db: KoColorDatabase): InventoryDao = db.inventoryDao

        @Provides
        fun provideRoutineDao(db: KoColorDatabase): RoutineDao = db.routineDao

        @Provides
        fun provideCosmeticDao(db: KoColorDatabase): CosmeticDao = db.cosmeticDao

        @Provides
        fun provideClothingDao(db: KoColorDatabase): ClothingDao = db.clothingDao

        @Provides
        fun provideProductDao(db: KoColorDatabase): ProductDao = db.productDao

        @Provides
        fun provideInstalledPackDao(db: KoColorDatabase): InstalledPackDao = db.installedPackDao

        @Provides
        fun provideShoppingCartDao(db: KoColorDatabase): ShoppingCartDao = db.shoppingCartDao

        @Provides
        fun provideGarmentRotationDao(db: KoColorDatabase): GarmentRotationDao = db.garmentRotationDao
    }
}
