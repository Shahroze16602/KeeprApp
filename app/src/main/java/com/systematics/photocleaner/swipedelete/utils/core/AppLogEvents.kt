package com.systematics.photocleaner.swipedelete.utils.core

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.systematics.photocleaner.swipedelete.BuildConfig

class AppLogEvents(private val context: Context) {
    fun loadEvents(events: String) {
        loadEvents(events, null)
    }

    @SuppressLint("MissingPermission")
    fun loadEvents(events: String, bundle: Bundle? = null) {
        if (!BuildConfig.DEBUG) {
            FirebaseAnalytics.getInstance(context).logEvent(events, bundle)
        } else {
            Log.d(TAG, "loadEvents: $events $bundle")
        }
    }

    companion object {
        private const val TAG = "AppLogEventsTAG"
    }
}
