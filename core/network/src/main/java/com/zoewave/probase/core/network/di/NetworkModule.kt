package com.zoewave.probase.core.network.di

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.zoewave.probase.core.network.api.apollo.apolloClient
import com.zoewave.probase.core.network.api.client.MapsClient
import com.zoewave.probase.core.network.api.client.YelpClient
import com.zoewave.probase.core.network.api.interfaces.MapsAPI
import com.zoewave.probase.core.network.api.interfaces.YelpAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    @Provides
    @Singleton
    fun provideApolloClient(
        @ApplicationContext context: Context
    ): ApolloClient {
        return apolloClient(context)
    }

    @Provides
    @Singleton
    fun bindsYelpAPI(
        apolloClient: ApolloClient
    ): YelpAPI {
        return YelpClient(apolloClient)
    }

    @Provides
    @Singleton
    fun bindsMapsAPI(): MapsAPI {
        return MapsClient()
    }
}