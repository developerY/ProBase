package com.zoewave.probase.core.data.di

import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.zoewave.probase.core.data.api.apollo.apolloClient
import com.zoewave.probase.core.data.api.client.MapsClient
import com.zoewave.probase.core.data.api.client.YelpClient
import com.zoewave.probase.core.data.api.interfaces.MapsAPI
import com.zoewave.probase.core.data.api.interfaces.YelpAPI
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