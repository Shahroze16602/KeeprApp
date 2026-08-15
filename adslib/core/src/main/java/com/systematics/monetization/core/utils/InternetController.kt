package com.systematics.monetization.core.utils

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@SuppressLint("MissingPermission", "ObsoleteSdkInt")
class InternetController(
    private val connectivityManager: ConnectivityManager
) {

    val isInternetConnected: Boolean
        get() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val network = connectivityManager.activeNetwork
                    if (network != null) {
                        val nc = connectivityManager.getNetworkCapabilities(network)
                        if (nc != null && (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || nc.hasTransport(
                                NetworkCapabilities.TRANSPORT_CELLULAR
                            ) || nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                        ) {
                            return true
                        }
                    }
                } else {
                    val networkInfo = connectivityManager.activeNetworkInfo
                    return networkInfo != null && networkInfo.isConnected && networkInfo.isAvailable
                }
            } catch (_: Exception) {
            }
            return false
        }


    val isWifiConnected: Boolean
        get() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val network = connectivityManager.activeNetwork
                    if (network != null) {
                        val nc = connectivityManager.getNetworkCapabilities(network)
                        if (nc != null && nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return true
                        }
                    }
                } else {
                    val networkInfo = connectivityManager.activeNetworkInfo
                    return networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI
                }
            } catch (_: Exception) {
            }
            return false
        }

    fun observeInternet(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        trySend(isInternetConnected)
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
        try {
            connectivityManager.registerNetworkCallback(networkRequest, callback)
        } catch (_: Exception) {

        }
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    fun isWifiActive(): Boolean {
        val ni = connectivityManager.activeNetworkInfo
        return ni != null && ni.type == ConnectivityManager.TYPE_WIFI
    }
}