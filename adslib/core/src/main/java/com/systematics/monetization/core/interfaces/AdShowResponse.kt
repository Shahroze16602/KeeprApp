package com.systematics.monetization.core.interfaces

import android.app.Activity
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import java.lang.Exception

interface AdShowResponse : AdResponse, AdShowListener {

    suspend fun beforeAdShow()
    fun getCoroutineScope(): CoroutineScope
    fun getLifeLifecycle(): Lifecycle
    fun getShowingActivity(): Activity
    override fun onAdLoadFailed(ex: Exception) {}
}