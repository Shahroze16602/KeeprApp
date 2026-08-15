package com.systematics.monetization.ui.compose.components

import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.systematics.monetization.core.MonetizationApp
import com.systematics.monetization.core.integration.natives.NativeLayoutRegistry
import com.systematics.monetization.core.models.natives.AppNativeAd
import com.systematics.monetization.core.models.natives.AppRefreshableNativeAd
import com.systematics.monetization.core.models.natives.NativeThemedUiDataModel
import com.systematics.monetization.core.models.natives.NativeUiDataModel
import com.systematics.monetization.core.utils.ALog
import com.systematics.monetization.core.utils.NativeAdOneTimeUiEvents
import com.systematics.monetization.core.utils.hide
import com.systematics.monetization.core.utils.toColor
import com.systematics.monetization.ui.ad.load.NativeLoadManager
import com.systematics.monetization.ui.ad.store.NativeAdStore
import com.systematics.monetization.ui.compose.utils.shimmerColor
import com.systematics.monetization.ui.placement.RefreshMode
import com.systematics.monetization.ui.placement.models.NativePlacementModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "Native AdShowComponent"

@Composable
fun NativeAdShowComponent(
    modifier: Modifier = Modifier,
    placementKey: String,
    nativePlacementModel: NativePlacementModel,
    nativeLoadManager: NativeLoadManager,
    nativeAdStore: NativeAdStore,
    nativeThemedUiModel: NativeThemedUiDataModel,
    nativeUiEvents: Flow<NativeAdOneTimeUiEvents>?,
    onNativeAdShow: () -> Unit = {},
    onAdFailed: (Exception) -> Unit = {}
) {
    val context = LocalContext.current
    var appNativeAd by remember {
        mutableStateOf<AppRefreshableNativeAd?>(null)
    }

    var nativeFailed by remember {
        mutableStateOf(false)
    }

    val isDark = isSystemInDarkTheme()
    val nativeUiDataModel by remember(isDark) {
        mutableStateOf(nativeThemedUiModel.themed(isDark))
    }

    var refreshCount by rememberSaveable {
        mutableIntStateOf(0)
    }

    var refreshFailed by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = refreshCount) {
        nativeLoadManager.getLoadedAd(
            coroutineScope = this,
            placementKey = placementKey,
            placement = nativePlacementModel,
            refreshCount = if (refreshFailed) refreshCount - 1 else refreshCount,
            nativeShown = appNativeAd != null,
            nativeAdStore = nativeAdStore,
            onNativeAdLoaded = {
                appNativeAd = it
                onNativeAdShow()
            },
            onAdFailed = {
                if (appNativeAd == null) {
                    nativeFailed = true
                    onAdFailed(it)
                } else {
                    refreshFailed = true
                }
            }
        )
    }
    appNativeAd?.let {
        key(appNativeAd) {
            NativeAdShowComponent(
                modifier = modifier,
                nativeAd = it.appNativeAd,
                adTag = nativePlacementModel.tag,
                layout = nativePlacementModel.layout,
                nativeUiDataModel = nativeUiDataModel
            )
        }
    } ?: run {
        if (!nativeFailed) {
            NativeAdShimmerComponent(
                modifier = modifier,
                layout = nativePlacementModel.layout,
                shimmerColor = remember { Color(nativeUiDataModel.shimmerColor.toColor()) }
            )
        }
    }

    var passedRefreshSeconds by rememberSaveable { mutableStateOf(0) }

    NativeAdRefreshComponent(
        refreshCount = refreshCount,
        nativeCount = appNativeAd?.refreshCount ?: -1,
        passedRefreshSeconds = passedRefreshSeconds,
        refreshMode = nativePlacementModel.refreshMode,
        nativeUiEvents = nativeUiEvents,
        onSecondPassed = { passedRefreshSeconds++ },
        onSecondsDone = { passedRefreshSeconds = 0 },
        onRefresh = { refreshCount++ },
    )
}

@Composable
private fun <T : AppNativeAd> NativeAdShowComponent(
    modifier: Modifier = Modifier,
    nativeAd: T,
    adTag: String,
    layout: String,
    nativeUiDataModel: NativeUiDataModel
) {
    val context = LocalContext.current

    val nativeView by remember {
        val nativeLayoutRegistry: NativeLayoutRegistry<T>? =
            MonetizationApp.instance.integrationManager.getNativeRegistry(nativeAd, layout)
        nativeLayoutRegistry?.let {
            val view = it.createView(context)
            nativeLayoutRegistry.populate(context = context, view, nativeAd, nativeUiDataModel)
            ALog.d(TAG, "$adTag native shown")
            mutableStateOf(view)
        } ?: run {
            ALog.d(TAG, "$adTag no registry for view layout $layout")
            mutableStateOf(null)
        }
    }

    nativeView?.let {
        NativeAdShowComponent(
            modifier = modifier,
            nativeView = it
        )
    }
}

@Composable
private fun NativeAdShimmerComponent(
    modifier: Modifier = Modifier,
    shimmerColor: Color,
    layout: String,
) {
    val context = LocalContext.current

    var nativeView by remember {
        mutableStateOf<View?>(null)
    }

    LaunchedEffect(key1 = Unit) {
        val nativeLayoutRegistry =
            MonetizationApp.instance.integrationManager.getNativeRegistryForView(layout)
        nativeLayoutRegistry?.let {
            val view = it.createView(context)
            view.hide()
            nativeView = view
        } ?: run {
            ALog.d(TAG, "NativeAdShowComponent: No shimmer registry for $layout")
        }
    }

    nativeView?.let {
        NativeAdShowComponent(
            modifier = modifier.background(shimmerColor(shimmerColor = shimmerColor)),
            nativeView = it
        )
    }
}

@Composable
private fun NativeAdShowComponent(
    modifier: Modifier = Modifier,
    nativeView: View
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = {
            LinearLayout(it).apply { gravity = Gravity.CENTER_HORIZONTAL }
                .apply { addView(nativeView) }
        }
    )
}

@Composable
fun NativeAdRefreshComponent(
    refreshCount: Int,
    nativeCount: Int,
    passedRefreshSeconds: Int,
    refreshMode: RefreshMode,
    nativeUiEvents: Flow<NativeAdOneTimeUiEvents>?,
    onSecondPassed: () -> Unit,
    onSecondsDone: () -> Unit,
    onRefresh: () -> Unit
) {
    var refreshOnResume by remember {
        mutableStateOf(false)
    }
    val passedSecondsLatest by rememberUpdatedState(passedRefreshSeconds)
    LaunchedEffect(key1 = refreshCount, key2 = nativeCount) {
        if (refreshMode == RefreshMode.None || nativeCount == -1) return@LaunchedEffect
        val isOneTimeRefresh = when (refreshMode) {
            is RefreshMode.RefreshOnAction -> refreshMode.oneTime
            is RefreshMode.RefreshOnInterval -> refreshMode.oneTime
            is RefreshMode.RefreshOnResume -> refreshMode.oneTime
            else -> true
        }
        if (refreshCount == 0 || !isOneTimeRefresh) {
            when (refreshMode) {
                is RefreshMode.RefreshOnAction -> {
                    nativeUiEvents?.collectLatest {
                        when (it) {
                            NativeAdOneTimeUiEvents.Refresh -> {
                                if (nativeCount == refreshCount) {
                                    Log.d(TAG, "NativeAdRefreshComponent: refreshing on action")
                                    onRefresh()
                                }
                            }
                        }
                    }
                }

                is RefreshMode.RefreshOnInterval -> {
                    while (passedSecondsLatest < refreshMode.intervalSeconds) {
                        while (nativeCount < refreshCount) {
                            delay(1000)
                        }
                        Log.d(TAG, "NativeAdRefreshComponent: interval $passedSecondsLatest")
                        delay(1000)
                        onSecondPassed()
                    }
                    if (nativeCount == refreshCount) {
                        Log.d(TAG, "NativeAdRefreshComponent: refreshing on interval")
                        onRefresh()
                        onSecondsDone()
                    }
                }

                is RefreshMode.RefreshOnResume -> {
                    refreshOnResume = true
                }

                else -> Unit
            }
        } else {
            Log.d(TAG, "NativeAdRefreshComponent: no more refreshed")
        }
    }
    var isObservingResumes by rememberSaveable {
        mutableStateOf(false)
    }
    if (refreshMode is RefreshMode.RefreshOnResume) {
        val lifecycle = LocalLifecycleOwner.current
        DisposableEffect(key1 = refreshOnResume) {
            var listener: DefaultLifecycleObserver? = null
            if (refreshOnResume) {
                listener = object : DefaultLifecycleObserver {
                    override fun onResume(owner: LifecycleOwner) {
                        if (isObservingResumes) {
                            if (nativeCount == refreshCount) {
                                Log.d(TAG, "NativeAdRefreshComponent: refreshing on resume")
                                onRefresh()
                                onSecondsDone()
                            }
                            if (refreshMode.oneTime) {
                                refreshOnResume = false
                            }
                        } else {
                            Log.d(TAG, "NativeAdRefreshComponent: observe started")
                            isObservingResumes = true
                        }
                    }
                }
                lifecycle.lifecycle.addObserver(listener)
            }
            onDispose {
                listener?.let { lifecycle.lifecycle.removeObserver(it) }
            }
        }
    }
}