package com.zoewave.probase.kocolor.features.products.di

import com.zoewave.probase.kocolor.features.products.data.remote.OpenProductsFactsApi
import com.zoewave.probase.kocolor.features.products.data.repository.ProductRepositoryImpl
import com.zoewave.probase.kocolor.features.products.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProductsDataModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    companion object {
        @Provides
        @Singleton
        fun provideOpenProductsFactsApi(): OpenProductsFactsApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(OpenProductsFactsApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(OpenProductsFactsApi::class.java)
        }
    }
}
