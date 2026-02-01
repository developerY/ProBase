package com.zoewave.probase.ashbike.database.di

import android.content.Context
import androidx.room.Room
import com.zoewave.probase.ashbike.database.BikeRideDao
import com.zoewave.probase.ashbike.database.BikeRideDatabase
import com.zoewave.probase.ashbike.database.BikeRideRepo
import com.zoewave.probase.ashbike.database.repository.BikeRideRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RideDatabase

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @RideDatabase
    @Provides
    @Singleton
    fun provideBikeRideDB(@ApplicationContext ctx: Context): BikeRideDatabase =
        Room.databaseBuilder(ctx, BikeRideDatabase::class.java, BikeRideDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    @Singleton
    fun provideBikeRideDao(@RideDatabase db: BikeRideDatabase): BikeRideDao =
        db.bikeRideDao

    @Provides
    @Singleton
    fun provideRealBikeRideRepository(
        rideDao: BikeRideDao
    ): BikeRideRepo = BikeRideRepoImpl(rideDao)

    /*@Provides
    @Singleton
    @Named("demo")
    fun provideDemoBikeRideRepository(
    ): BikeRideRepo = DemoBikeRideRepo()*/
}
