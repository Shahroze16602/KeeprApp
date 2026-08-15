package com.systematics.monetization.core.utils

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.systematics.monetization.core.interfaces.AdLoaderResponse
import com.systematics.monetization.core.interfaces.AdShowResponse
import com.systematics.monetization.core.models.AdGroupResult
import com.systematics.monetization.core.models.AdRequester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "AdResponseHandlerTAG"

fun handleSuccessAdResponse(
    adRequester: AdRequester,
    adGroupResult: AdGroupResult,
    onAdShown: () -> Unit
) {
    val scope = MainScope()
    scope.launch {
        when (val response = adRequester.adResponse) {
            is AdLoaderResponse -> {
                response.onAdLoaded(adGroupResult)
            }

            is AdShowResponse -> {
                val adShowingScope = response.getCoroutineScope()
                val showAd: () -> Unit = {
                    adShowingScope.launch {
                        response.beforeAdShow()
                        withContext(Dispatchers.Main) {
                            val showingActivity = response.getShowingActivity()
                            adGroupResult.adManager.show(showingActivity, response)
                        }
                        onAdShown()
                    }
                }

                val lifecycle = response.getLifeLifecycle()
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d(TAG, "handleSuccessAdResponse: active showing ad")
                    showAd()
                } else {
                    var observer: DefaultLifecycleObserver? = null
                    Log.d(TAG, "handleSuccessAdResponse: in background")
                    observer = object : DefaultLifecycleObserver {
                        override fun onResume(owner: LifecycleOwner) {
                            Log.d(TAG, "onResume: resumed ad")
                            observer?.let {
                                scope.launch { showAd() }
                                lifecycle.removeObserver(it)
                                observer = null
                            }
                        }
                    }.also { lifecycle.addObserver(it) }
                }
            }
        }
    }
}

fun handleFailedAdResponse(
    adRequester: AdRequester,
    exception: Exception
) {
    when (val response = adRequester.adResponse) {
        is AdLoaderResponse -> {
            response.onAdLoadFailed(exception)
        }

        is AdShowResponse -> {
            response.onAdLoadFailed(exception)
            response.adShowingFailed(exception)
        }
    }
}