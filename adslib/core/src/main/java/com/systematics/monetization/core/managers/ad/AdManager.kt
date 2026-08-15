package com.systematics.monetization.core.managers.ad

import android.app.Activity
import android.content.Context
import com.systematics.monetization.core.interfaces.AdShowListener
import com.systematics.monetization.core.models.AdLoadState
import com.systematics.monetization.core.models.ad.local.AdInfo

abstract class AdManager<T>(
    protected val context: Context,
    val adInfo: AdInfo
) {

    protected var loadState: AdLoadState = AdLoadState.Initialized

    var adLoadListener: AdLoadListener? = null
        set(value) {
            field = value
            field?.let {
                with(loadState) {
                    when (this) {
                        is AdLoadState.Loaded -> it.onAdLoaded()
                        is AdLoadState.Failed -> it.onAdFailed(this.exception)
                        else -> Unit
                    }
                }
            }
        }

    open fun hasExtraAd(): Boolean = false

    open fun isLoaded() = (loadState is AdLoadState.Loaded)

    fun isShown() = (loadState is AdLoadState.Shown)

    fun isLoading() = (loadState is AdLoadState.Loading)

    open fun adLoadingPermitted(): Boolean = true

    abstract fun load(loadListenerInternal: AdLoadListener? = null)

    abstract fun show(activity: Activity, adShowListener: AdShowListener)

    abstract fun destroy()

    abstract fun getLoadedAd(): T?

    interface AdLoadListener {
        fun onAdLoaded()
        fun onAdFailed(ex: Exception)
    }

    companion object {

        private const val TAG = "AdManagerTAG"
    }
}