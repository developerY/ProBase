package com.zoewave.probase.core.util.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStatsProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getNetworkType(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = cm.activeNetwork ?: return "Offline"
        val actNw = cm.getNetworkCapabilities(nw) ?: return "Offline"
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Other"
        }
    }
}
