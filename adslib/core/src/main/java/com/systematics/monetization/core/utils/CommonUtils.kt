package com.systematics.monetization.core.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import androidx.core.graphics.toColorInt
import com.systematics.monetization.core.MonetizationApp
import com.systematics.monetization.core.interfaces.AdLoaderResponse
import com.systematics.monetization.core.managers.ad.AdManager
import com.systematics.monetization.core.models.AdGroupResult
import com.systematics.monetization.core.models.AdRequesterWaited
import com.systematics.monetization.core.models.ad.local.AdInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val TAG = "AdsCommonUtilsTAG"

suspend fun loadAdSynced(
    coroutineScope: CoroutineScope,
    adTag: String,
    adType: String
) = suspendCancellableCoroutine { continuation ->
    val adRequestWaitedModel = AdRequesterWaited(adType, adTag)
    coroutineScope.launch {
        try {
            Log.d(TAG, "loadAdSynced: $adTag waiting $adType")
            val result = waitForResult(adRequestWaitedModel)
            continuation.resume(result)
        } catch (ex: Exception) {
            continuation.cancel(ex)
            MonetizationApp.instance.removeRequester(adRequestWaitedModel)
        }
    }
}

private suspend fun waitForResult(adRequesterWaited: AdRequesterWaited) =
    suspendCancellableCoroutine {
        adRequesterWaited.adResponse = object : AdLoaderResponse {

            override fun onAdLoaded(adGroupResult: AdGroupResult) {
                Log.d(TAG, "onAdLoaded: result received")
                it.resume(adGroupResult)
            }

            override fun onAdLoadFailed(ex: Exception) {
                it.cancel()
            }
        }
        MonetizationApp.instance.addRequester(adRequesterWaited)
    }


val emptyAdLoaderResponse = object : AdLoaderResponse {
    override fun onAdLoaded(adGroupResult: AdGroupResult) {}

    override fun onAdLoadFailed(ex: Exception) {}
}

fun AdInfo.mapValidated(): AdInfo {
    return this
}

suspend fun createAdManager(adInfo: AdInfo): AdManager<*> =
    MonetizationApp.instance.integrationManager.createAdManager(adInfo)

val Number.dpToPx: Float
    get() {
        val density = Resources.getSystem().displayMetrics.density
        return (this.toFloat() * density)
    }

fun Context.isDarkMode(): Boolean =
    (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

fun String.toColor(): Int {
    return try {
        toColorInt()
    } catch (_: Exception) {
        0
    }
}