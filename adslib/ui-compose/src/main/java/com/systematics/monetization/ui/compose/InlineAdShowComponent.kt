package com.systematics.monetization.ui.compose

//import androidx.compose.runtime.setValue
import android.app.Activity
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systematics.monetization.core.MonetizationApp
import com.systematics.monetization.core.remote.AdsCoreRemoteConfigs
import com.systematics.monetization.core.utils.ALog
import com.systematics.monetization.core.utils.NativeAdOneTimeUiEvents
import com.systematics.monetization.ui.ad.load.NativeLoadManager
import com.systematics.monetization.ui.ad.store.AdsStoreManager
import com.systematics.monetization.ui.compose.components.BannerAdShowComponent
import com.systematics.monetization.ui.compose.components.NativeAdShowComponent
import com.systematics.monetization.ui.compose.utils.MonetizationUIConfig
import com.systematics.monetization.ui.compose.utils.activityViewModel
import com.systematics.monetization.ui.compose.utils.monetizationInject
import com.systematics.monetization.ui.exceptions.AdPlacementNotAllowedException
import com.systematics.monetization.ui.exceptions.AdPlacementNotFoundException
import com.systematics.monetization.ui.models.PlacementUiModel
import com.systematics.monetization.ui.placement.models.BannerPlacementModel
import com.systematics.monetization.ui.placement.models.NativePlacementModel
import com.systematics.monetization.ui.remote.MonetizationRemote
import com.systematics.monetization.ui.viewmodel.AdsViewModel
import kotlinx.coroutines.flow.Flow

private const val TAG = "InlineAdShowComponent"

@Composable
fun InlineAdShowComponent(
    modifier: Modifier = Modifier,
    adsStoreManager: AdsStoreManager = MonetizationUIConfig.adsStoreManagerProvider?.let { it() }
        ?: activityViewModel<AdsViewModel>(),
    placementKey: String,
    backupPlacementKey: String = placementKey + "_Backup",
    monetizationRemote: MonetizationRemote = monetizationInject(),
    monetizationApp: MonetizationApp = monetizationInject(),
    adsCoreRemoteConfigs: AdsCoreRemoteConfigs = monetizationInject(),
    nativeLoadManager: NativeLoadManager = monetizationInject(),
    nativeUiEvents: Flow<NativeAdOneTimeUiEvents>? = null,
    onAdShown: (PlacementUiModel) -> Unit = {},
    onFailed: (Exception) -> Unit = {}
) {
    val isInitialized by monetizationApp.isInitialized.collectAsStateWithLifecycle()
    val monetizationEnabled by adsCoreRemoteConfigs.monetizationEnabledState.collectAsStateWithLifecycle()
    if (monetizationEnabled) {
        if (isInitialized) {
            val getPlacementUiModel: (String) -> PlacementUiModel? = { key ->
                if (adsCoreRemoteConfigs.isPlacementOffForcefully(key)) {
                    Log.d(TAG, "InlineAdShowComponent: forcefully off for placement $key")
                    onFailed(AdPlacementNotAllowedException("Placement forcefully off exception"))
                    null
                }else {
                    val placement = monetizationRemote.appAdsUiConfig.appInlines[key]
                    if (placement == null) {
                        ALog.d(TAG, "No such placement for $key")
                        onFailed(AdPlacementNotFoundException("No such placement for $key"))
                        null
                    } else {
                        if (!placement.isEnabled) {
                            ALog.d(TAG, "$key placement not allowed")
                            onFailed(AdPlacementNotAllowedException("${placement.tag} placement not allowed No placement allowed"))
                            null
                        } else {
                            Log.d(
                                TAG,
                                "InlineAdShowComponent: setting current placement $placement"
                            )
                            PlacementUiModel(
                                key = key,
                                placement = placement
                            )
                        }
                    }
                }
            }
            var placementModel by remember {
                if (adsStoreManager.isPreserved(backupPlacementKey)) {
                    mutableStateOf(getPlacementUiModel(backupPlacementKey))
                } else {
                    mutableStateOf(getPlacementUiModel(placementKey))
                }
            }
            val onFailureInternal: (Exception, PlacementUiModel) -> Unit =
                { ex, failedUiPlacement ->
                    Log.d(TAG, "placement ${failedUiPlacement.key} failed $ex")
                    val isMainPlacement = placementKey == failedUiPlacement.key
                    if (isMainPlacement) {
                        val backupPlacement =
                            monetizationRemote.getInlinePlacementModel(backupPlacementKey)
                        if (backupPlacement != null) {
                            ALog.d(
                                TAG,
                                "${failedUiPlacement.key} shifting to backup placement $backupPlacementKey"
                            )
                            placementModel = getPlacementUiModel(backupPlacementKey)
                        } else {
                            onFailed(ex)
                        }
                    } else {
                        onFailed(ex)
                    }
                }

            placementModel?.let { uiPlacement ->
                val nativeThemedUiModel = remember {
                    monetizationRemote.getNativeUiConfigForPlacement(placementKey)
                }
                val activity = LocalActivity.current

                val isTopAd = placementKey.contains("TOP", ignoreCase = true)
                val onCrossClick: () -> Unit = {
                    (activity)?.let { act ->
                        AdClosePurchaseBridge.onAdCloseClicked?.invoke(act)
                    }
                }

                Column(modifier = modifier) {
                    if (!isTopAd) {
                        AdRemoveCrossButton(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            onClick = onCrossClick
                        )
                    }

                    when (val placementModel = uiPlacement.placement) {
                        is BannerPlacementModel -> {
                            BannerAdShowComponent(
                                modifier = Modifier,
                                placementKey = uiPlacement.key,
                                bannerPlacementModel = placementModel,
                                bannerAdStore = adsStoreManager.bannerAdStore,
                                nativeThemedUiModel = nativeThemedUiModel,
                                onBannerAdShown = { onAdShown(uiPlacement) },
                                onAdFailed = { onFailureInternal(it, uiPlacement) }
                            )
                        }

                        is NativePlacementModel -> {
                            NativeAdShowComponent(
                                modifier = Modifier,
                                placementKey = uiPlacement.key,
                                nativePlacementModel = placementModel,
                                nativeLoadManager = nativeLoadManager,
                                nativeAdStore = adsStoreManager.nativeAdStore,
                                nativeThemedUiModel = nativeThemedUiModel,
                                nativeUiEvents = nativeUiEvents,
                                onNativeAdShow = { onAdShown(uiPlacement) },
                                onAdFailed = { onFailureInternal(it, uiPlacement) }
                            )
                        }
                    }

                    if (isTopAd) {
                        AdRemoveCrossButton(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            onClick = onCrossClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdRemoveCrossButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetBy by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 0.dp,
        label = "remove_ads_button_press_offset"
    )

    val shadowHeight = 3.dp
    val shape = RoundedCornerShape(8.dp)
    val mainFaceBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFF97316), Color(0xFFEA580C))
    )
    val shadowColor = Color(0xFF9A3412)

    Box(
        modifier = modifier
            .size(28.dp + shadowHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .offset(y = shadowHeight)
                .clip(shape)
                .background(shadowColor)
        )

        Box(
            modifier = Modifier
                .size(28.dp)
                .offset { IntOffset(x = 0, y = offsetBy.roundToPx()) }
                .clip(shape)
                .background(mainFaceBrush)
                .border(width = 1.dp, color = Color(0xFFFDBA74), shape = shape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(14.dp)) {
                val stroke = 2.dp.toPx()
                drawLine(
                    color = Color.White,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(size.width, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
