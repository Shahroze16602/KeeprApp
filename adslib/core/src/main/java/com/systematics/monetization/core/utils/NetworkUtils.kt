package com.systematics.monetization.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.systematics.monetization.core.MonetizationApp

fun isNetworkAvailable(): Boolean {
    val connectivityManager =
        MonetizationApp.instance.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.let {
        when {
            it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } ?: false
}