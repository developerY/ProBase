package com.zoewave.probase.core.network.api.apollo

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.apollo.network.okHttpClient
import com.zoewave.probase.core.network.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

// Ideally, this should be managed by Hilt (@Singleton), but here is the direct conversion.
@Volatile
private var instance: ApolloClient? = null

fun apolloClient(context: Context): ApolloClient {
    return instance ?: synchronized(Any()) {
        instance ?: buildApolloClient(context).also { instance = it }
    }
}

private fun buildApolloClient(context: Context): ApolloClient {
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthorizationInterceptor(context))
        .build()

    return ApolloClient.Builder()
        .serverUrl("https://api.yelp.com/v3/graphql")
        .webSocketServerUrl("wss://api.yelp.com/v3/graphql")
        .okHttpClient(okHttpClient)
        // In v4, the normalized cache API is very similar, just package changes
        .normalizedCache(MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024))
        .build()
}

private class AuthorizationInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("cache-control", "no-cache")
            .addHeader("Authorization", "Bearer ${BuildConfig.YELP_API_KEY}")
            .build()

        return chain.proceed(request)
    }
}