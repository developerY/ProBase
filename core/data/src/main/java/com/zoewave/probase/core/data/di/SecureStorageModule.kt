package com.zoewave.probase.core.data.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecureStorageModule {

    private const val SECURE_PREFS_NAME = "photodo_secure_secrets"

    @Provides
    @Singleton
    @Named("SecureStorage")
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return try {
            createEncryptedSharedPreferences(context, masterKey)
        } catch (e: Exception) {
            Log.e("SecureStorageModule", "Error creating EncryptedSharedPreferences, recreating...", e)
            context.deleteSharedPreferences(SECURE_PREFS_NAME)
            createEncryptedSharedPreferences(context, masterKey)
        }
    }

    private fun createEncryptedSharedPreferences(context: Context, masterKey: MasterKey): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
