package com.systematics.monetization.ui.wrapper

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isEmpty
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import com.systematics.monetization.core.MonetizationApp
import com.systematics.monetization.core.integration.natives.NativeLayoutRegistry
import com.systematics.monetization.core.models.natives.AppNativeAd
import com.systematics.monetization.core.models.natives.AppRefreshableNativeAd
import com.systematics.monetization.core.models.natives.NativeThemedUiDataModel
import com.systematics.monetization.core.models.natives.NativeUiDataModel
import com.systematics.monetization.core.remote.AdsCoreRemoteConfigs
import com.systematics.monetization.core.utils.ALog
import com.systematics.monetization.core.utils.NativeAdOneTimeUiEvents
import com.systematics.monetization.core.utils.hide
import com.systematics.monetization.core.utils.toColor
import com.systematics.monetization.ui.ad.load.NativeLoadManager
import com.systematics.monetization.ui.ad.store.NativeAdStore
import com.systematics.monetization.ui.placement.RefreshMode
import com.systematics.monetization.ui.placement.models.NativePlacementModel
import com.systematics.monetization.ui.utils.onceWhileVisibleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NativeShowWrapper(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val nativeLoadManager: NativeLoadManager,
    private val adsCoreRemoteConfigs: AdsCoreRemoteConfigs
) {

    var refreshCount: Int = 0
    var appNativeAd: MutableMap<String, AppRefreshableNativeAd> = mutableMapOf()

    var passedSeconds = 0
    fun showNativeAd(
        adView: ViewGroup,
        placementKey: String,
        nativePlacementModel: NativePlacementModel,
        nativeAdStore: NativeAdStore,
        nativeThemedUiModel: NativeThemedUiDataModel,
        nativeUiEvents: Flow<NativeAdOneTimeUiEvents>?,
        onNativeAdShow: () -> Unit = {},
        onAdFailed: (Exception) -> Unit = {}
    ) {
        val nativeUiDataModel = nativeThemedUiModel.themed(context)
        Log.d(TAG, "showNativeAd: native show requested")
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (!adsCoreRemoteConfigs.monetizationEnabled()) {
                    Log.d(TAG, "showNativeAd: monetization disabled")
                    adView.removeAllViews()
                    return@repeatOnLifecycle
                }
                val screenVisibleScope = lifecycle.onceWhileVisibleScope()
                if (adView.isEmpty()) {
                    showNativeAdShimmer(
                        adView = adView,
                        layout = nativePlacementModel.layout,
                        shimmerColor = nativeUiDataModel.shimmerColor.toColor()
                    )
                }
                if (appNativeAd[placementKey] == null) {
                    nativeLoadManager.getLoadedAd(
                        coroutineScope = screenVisibleScope,
                        placementKey = placementKey,
                        placement = nativePlacementModel,
                        refreshCount = refreshCount,
                        nativeShown = false,
                        nativeAdStore = nativeAdStore,
                        onNativeAdLoaded = {
                            if (!adsCoreRemoteConfigs.monetizationEnabled()) {
                                Log.d(TAG, "showNativeAd: loaded show disabled")
                                adView.removeAllViews()
                            } else {
                                appNativeAd[placementKey] = it
                                showNativeAd(
                                    nativeAd = it.appNativeAd,
                                    adView = adView,
                                    adTag = nativePlacementModel.tag,
                                    layout = nativePlacementModel.layout,
                                    nativeUiDataModel = nativeUiDataModel
                                )
                                onNativeAdShow()
                            }
                        },
                        onAdFailed = {
                            if (refreshCount == 0) {
                                adView.removeAllViews()
                                onAdFailed(it)
                            }
                        }
                    )
                } else {
                    showNativeAd(
                        nativeAd = appNativeAd[placementKey]!!.appNativeAd,
                        adView = adView,
                        adTag = nativePlacementModel.tag,
                        layout = nativePlacementModel.layout,
                        nativeUiDataModel = nativeUiDataModel
                    )
                }

                val refreshNow = {
                    refreshCount += 1
                    nativeLoadManager.getLoadedAd(
                        coroutineScope = screenVisibleScope,
                        placementKey = placementKey,
                        placement = nativePlacementModel,
                        refreshCount = refreshCount,
                        nativeShown = appNativeAd[placementKey] != null,
                        nativeAdStore = nativeAdStore,
                        onNativeAdLoaded = {
                            if (!adsCoreRemoteConfigs.monetizationEnabled()) {
                                Log.d(TAG, "showNativeAd: loaded show disabled")
                                adView.removeAllViews()
                            } else {
                                appNativeAd[placementKey] = it
                                showNativeAd(
                                    nativeAd = it.appNativeAd,
                                    adView = adView,
                                    adTag = nativePlacementModel.tag,
                                    layout = nativePlacementModel.layout,
                                    nativeUiDataModel = nativeUiDataModel
                                )
                                onNativeAdShow()
                            }
                        }
                    )
                }

                when (val refreshMode = nativePlacementModel.refreshMode) {
                    is RefreshMode.RefreshOnAction -> {
                        val shouldObserveAction = (refreshCount == 0 || !refreshMode.oneTime)
                        if (shouldObserveAction) {
                            screenVisibleScope.launch {
                                nativeUiEvents?.collectLatest {
                                    when (it) {
                                        NativeAdOneTimeUiEvents.Refresh -> {
                                            val isRefreshAllowed =
                                                (refreshCount == 0 || !refreshMode.oneTime) && appNativeAd[placementKey]?.refreshCount == refreshCount
                                            if (isRefreshAllowed) {
                                                Log.d(TAG, "showNativeAd: refreshing on action")
                                                refreshNow()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is RefreshMode.RefreshOnInterval -> {
                        val shouldCount = (refreshCount == 0 || !refreshMode.oneTime)
                        if (shouldCount) {
                            screenVisibleScope.launch {
                                while ((refreshCount == 0 || !refreshMode.oneTime)) {
                                    while (passedSeconds < refreshMode.intervalSeconds) {
                                        while (
                                            appNativeAd[placementKey] == null ||
                                            (appNativeAd[placementKey]?.refreshCount
                                                ?: -1) < refreshCount
                                        ) {
                                            delay(1000)
                                        }
                                        delay(1000)
                                        passedSeconds += 1
                                        Log.d(TAG, "interval passed seconds $passedSeconds")
                                    }
                                    if (appNativeAd[placementKey]?.refreshCount == refreshCount) {
                                        passedSeconds = 0
                                        refreshNow()
                                    }
                                }
                            }
                        }
                    }

                    is RefreshMode.RefreshOnResume -> {
                        val isRefreshAllowed =
                            (refreshCount == 0 || !refreshMode.oneTime) && appNativeAd[placementKey]?.refreshCount == refreshCount
                        if (isRefreshAllowed) {
                            Log.d(TAG, "showNativeAd: refreshing on resume")
                            refreshNow()
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun <T : AppNativeAd> showNativeAd(
        adView: ViewGroup,
        nativeAd: T,
        adTag: String,
        layout: String,
        nativeUiDataModel: NativeUiDataModel
    ) {
        val nativeLayoutRegistry: NativeLayoutRegistry<T>? =
            MonetizationApp.instance.integrationManager.getNativeRegistry(nativeAd, layout)
        nativeLayoutRegistry?.let {
            val view = it.createView(context, adView)
            nativeLayoutRegistry.populate(context = context, view, nativeAd, nativeUiDataModel)
            ALog.d(TAG, "$adTag native shown")
            adView.layoutParams = adView.layoutParams.apply {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            adView.removeAllViews()
            adView.addView(view)
        } ?: run {
            ALog.d(TAG, "$adTag no registry for view layout $layout")
            null
        }
    }

    private fun showNativeAdShimmer(
        adView: ViewGroup,
        layout: String,
        shimmerColor: Int,
    ) {
        val nativeLayoutRegistry =
            MonetizationApp.instance.integrationManager.getNativeRegistryForView(layout)
        nativeLayoutRegistry?.let {
            val view = it.createView(context)
            view.hide()
            val linearContainer = LinearLayout(context).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                setBackgroundColor(shimmerColor)
            }
            linearContainer.addView(view)
            adView.removeAllViews()
            adView.addView(linearContainer)
        } ?: run {
            ALog.d(TAG, "showNativeAdShimmer: No shimmer registry for $layout")
        }
    }

    companion object {

        private const val TAG = "NativeShowWrapperTAG"
    }

}