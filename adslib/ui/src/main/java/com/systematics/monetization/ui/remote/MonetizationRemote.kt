package com.systematics.monetization.ui.remote

import android.util.Log
import com.systematics.monetization.core.MonetizationApp
import com.systematics.monetization.core.models.AppAdsConfig
import com.systematics.monetization.core.models.ad.remote.group.FlatFallbackRemoteAdInfoGroup
import com.systematics.monetization.core.models.ad.remote.group.InstancedRemoteAdInfoGroup
import com.systematics.monetization.core.models.ad.remote.group.PriorityRemoteAdInfoGroup
import com.systematics.monetization.core.models.ad.remote.group.RotationFallbackRemoteAdInfoGroup
import com.systematics.monetization.core.models.ad.remote.group.RotationRemoteAdInfoGroup
import com.systematics.monetization.core.models.ad.remote.group.abs.RemoteAdInfoGroup
import com.systematics.monetization.core.models.natives.AppNativeUiConfig
import com.systematics.monetization.core.models.natives.NativeThemedUiDataModel
import com.systematics.monetization.core.remote.AdsCoreRemoteConfigs
import com.systematics.monetization.ui.ad.ui.AppAdsMappingConfig
import com.systematics.monetization.ui.ad.ui.AppAdsUiConfig
import com.systematics.monetization.ui.ad.ui.AppBreakPointConfig
import com.systematics.monetization.ui.breakpoints.BreakPointModel
import com.systematics.monetization.ui.models.ConditionalModel
import com.systematics.monetization.ui.placement.RefreshMode
import com.systematics.monetization.ui.placement.models.BannerPlacementModel
import com.systematics.monetization.ui.placement.models.FullScreenPlacementModel
import com.systematics.monetization.ui.placement.models.NativePlacementModel
import com.systematics.monetization.ui.placement.models.abs.InlinePlacementModel
import com.systematics.monetization.ui.placement.models.abs.PlacementModel
import com.systematics.monetization.ui.remote.config.AdsUiRemoteConfigs
import com.systematics.monetization.ui.utils.monetizationInject
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

class MonetizationRemote(
    defaultAppAdsConfig: AppAdsConfig,
    defaultAppAdsUiConfig: AppAdsUiConfig,
    defaultAppBreakPointConfig: AppBreakPointConfig,
    defaultAppNativeUiConfig: AppNativeUiConfig,
    defaultAppAdsMappingConfig: AppAdsMappingConfig,
    private val adsCoreRemoteConfigs: AdsCoreRemoteConfigs,
    private val uiRemoteConfig: AdsUiRemoteConfigs,
) {
    private val monetizationApp: MonetizationApp by monetizationInject()

    private val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            classDiscriminator = "type"
            serializersModule = SerializersModule {
                polymorphic(RemoteAdInfoGroup::class) {
                    subclass(
                        FlatFallbackRemoteAdInfoGroup::class,
                        FlatFallbackRemoteAdInfoGroup.serializer()
                    )
                    subclass(
                        InstancedRemoteAdInfoGroup::class,
                        InstancedRemoteAdInfoGroup.serializer()
                    )
                    subclass(
                        PriorityRemoteAdInfoGroup::class,
                        PriorityRemoteAdInfoGroup.serializer()
                    )
                    subclass(
                        RotationFallbackRemoteAdInfoGroup::class,
                        RotationFallbackRemoteAdInfoGroup.serializer()
                    )
                    subclass(
                        RotationRemoteAdInfoGroup::class,
                        RotationRemoteAdInfoGroup.serializer()
                    )
                }
                polymorphic(PlacementModel::class) {
                    subclass(
                        NativePlacementModel::class,
                        NativePlacementModel.serializer()
                    )
                    subclass(
                        BannerPlacementModel::class,
                        BannerPlacementModel.serializer()
                    )
                }

                polymorphic(RefreshMode::class) {
                    subclass(
                        RefreshMode.None::class,
                        RefreshMode.None.serializer()
                    )
                    subclass(
                        RefreshMode.RefreshOnAction::class,
                        RefreshMode.RefreshOnAction.serializer()
                    )
                    subclass(
                        RefreshMode.RefreshOnResume::class,
                        RefreshMode.RefreshOnResume.serializer()
                    )
                    subclass(
                        RefreshMode.RefreshOnInterval::class,
                        RefreshMode.RefreshOnInterval.serializer()
                    )
                }

                MonetizationApp.instance.integrationManager.getBannerSerializable().forEach {
                    include(it)
                }
            }
        }
    }

    private var appAdsConfig = defaultAppAdsConfig

    var appBreakPoints = defaultAppBreakPointConfig
        private set

    var appAdsUiConfig = defaultAppAdsUiConfig
        private set

    var appAdsMappings = defaultAppAdsMappingConfig
        private set

    private var appNativeUiConfig = defaultAppNativeUiConfig

    fun isFullScreenPlacementAllowed(placement: String): Boolean {
        return adsCoreRemoteConfigs.monetizationEnabled() && appAdsUiConfig.appInlines[placement]?.isEnabled ?: false
    }

    fun isInlinePlacementAllowed(placement: String): Boolean {
        return adsCoreRemoteConfigs.monetizationEnabled() && appAdsUiConfig.appFullScreens[placement]?.isEnabled ?: false
    }

    fun updateFromRemote() {
        updateAppAdsConfig()
        updateAppAdsUiConfig()
        updateAppBreakPointConfig()
        updateAppAdsMappingConfig()
    }

    fun getNativeUiConfigForPlacement(placement: String): NativeThemedUiDataModel {
        val nativeThemePlacementKey = placement + "_Native_UI_Config"
        val appDefaultPlacementTheme =
            appNativeUiConfig.nativeThemedPlacements[nativeThemePlacementKey]

        return appDefaultPlacementTheme?.let {
            getRemoteNativeThemedUiDataModel(
                defaultModel = appDefaultPlacementTheme,
                remoteJson = uiRemoteConfig.remoteJSON(nativeThemePlacementKey)
            )
        } ?: appNativeUiConfig.defaultNativeTheme
    }

    fun getInlinePlacementModel(placement: String): InlinePlacementModel? {
        return appAdsUiConfig.appInlines[placement] ?: run {
            getRemoteInlinePlacementModel(
                defaultModel = null,
                remoteJson = uiRemoteConfig.remoteJSON(placement)
            )
        }
    }

    fun getRemoteAdConfig(adGroupType: String): RemoteAdInfoGroup? {
        return appAdsConfig.appFullScreens[adGroupType] ?: appAdsConfig.appNatives[adGroupType]
        ?: getRemoteAdInfoGroup(
            defaultModel = null,
            remoteJson = uiRemoteConfig.remoteJSON(adGroupType)
        )?.filterForDebug(monetizationApp.integrationManager)
            ?.filterForKnownAdTypes(monetizationApp.integrationManager)
    }

    fun getBreakPoint(breakPoint: String): BreakPointModel? {
        return appBreakPoints.appBreakPoints[breakPoint] ?: run {
            getRemoteBreakPointModel(
                defaultModel = null,
                remoteJson = uiRemoteConfig.remoteJSON(breakPoint)
            )
        }
    }

    fun getConditionalMappings(condition: String): List<ConditionalModel>? {
        return appAdsMappings.appAdsConditionalMappings[condition] ?: run {
            getRemoteConditionalModels(
                defaultModels = null,
                remoteJson = uiRemoteConfig.remoteJSON(condition)
            )
        }
    }

    fun validateAdConfig() {
        appAdsConfig = appAdsConfig.validated()
    }

    private fun updateAppAdsConfig() {
        val config = appAdsConfig

        val appFullScreensUpdated = config.appFullScreens.entries.associate {
            val adGroupType = it.key
            val interAdStrategy = getRemoteAdInfoGroup(
                defaultModel = it.value,
                remoteJson = uiRemoteConfig.remoteJSON(adGroupType)
            )
            adGroupType to (interAdStrategy ?: it.value)
        }
        val appNativesUpdated = config.appNatives.entries.associate {
            val adGroupType = it.key
            val nativeAdStrategy = getRemoteAdInfoGroup(
                defaultModel = it.value,
                remoteJson = uiRemoteConfig.remoteJSON(adGroupType)
            )
            adGroupType to (nativeAdStrategy ?: it.value)
        }

        val updateConfig = config.copy(
            appFullScreens = appFullScreensUpdated,
            appNatives = appNativesUpdated
        )
        appAdsConfig = updateConfig.validated()
    }

    private fun updateAppAdsUiConfig() {
        val config = appAdsUiConfig

        val appFullScreensUpdated = config.appFullScreens.entries.associate {
            val adPlacement = it.key
            val fullScreenPlacements = getRemoteFullScreenPlacementModel(
                defaultModel = it.value,
                remoteJson = uiRemoteConfig.remoteJSON(adPlacement)
            )
            adPlacement to (fullScreenPlacements ?: it.value)
        }
        val appInlinesUpdated = config.appInlines.entries.associate {
            val adPlacement = it.key
            val inlinePlacements = getRemoteInlinePlacementModel(
                defaultModel = it.value,
                remoteJson = uiRemoteConfig.remoteJSON(adPlacement)
            )
            adPlacement to (inlinePlacements ?: it.value)
        }

        appAdsUiConfig = config.copy(
            appFullScreens = appFullScreensUpdated,
            appInlines = appInlinesUpdated,
        )

        appNativeUiConfig = appNativeUiConfig.copy(
            defaultNativeTheme = getRemoteNativeThemedUiDataModel(
                defaultModel = appNativeUiConfig.defaultNativeTheme,
                remoteJson = uiRemoteConfig.remoteJSON("App_Native_UI_Config")
            )
        )
    }

    private fun updateAppBreakPointConfig() {
        val config = appBreakPoints

        val breakPointModels = config.appBreakPoints.entries.associate {
            val breakPointKey = it.key
            val breakPointModel = getRemoteBreakPointModel(
                defaultModel = it.value,
                remoteJson = uiRemoteConfig.remoteJSON(breakPointKey)
            )
            breakPointKey to (breakPointModel ?: it.value)
        }

        appBreakPoints = config.copy(
            appBreakPoints = breakPointModels
        )
    }

    private fun updateAppAdsMappingConfig() {
        val config = appAdsMappings

        val conditionalModels = config.appAdsConditionalMappings.entries.associate {
            val conditionKey = it.key
            val conditionalModel = getRemoteConditionalModels(
                defaultModels = it.value,
                remoteJson = uiRemoteConfig.remoteJSON(conditionKey)
            )
            conditionKey to (conditionalModel ?: it.value)
        }

        appAdsMappings = config.copy(
            appAdsConditionalMappings = conditionalModels
        )
    }

    private fun getRemoteAdInfoGroup(
        defaultModel: RemoteAdInfoGroup?,
        remoteJson: String,
    ): RemoteAdInfoGroup? {
        return try {
            if (remoteJson.isEmpty()) {
                defaultModel
            } else {
                json.decodeFromString(RemoteAdInfoGroup.serializer(), remoteJson).also {
                    Log.d(TAG, "getRemoteAdInfoGroup: changed ${it.adType} to $it")
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "getRemoteAdInfoGroup: ", ex)
            defaultModel
        }
    }

    private fun getRemoteNativeThemedUiDataModel(
        defaultModel: NativeThemedUiDataModel,
        remoteJson: String,
    ): NativeThemedUiDataModel {
        return try {
            if (remoteJson.isEmpty()) {
                defaultModel
            } else {
                json.decodeFromString(NativeThemedUiDataModel.serializer(), remoteJson).also {
                    Log.d(TAG, "getRemoteNativeThemedUiDataModel: found to $it")
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "getRemoteNativeThemedUiDataModel: ", ex)
            defaultModel
        }
    }

    private fun getRemoteFullScreenPlacementModel(
        defaultModel: FullScreenPlacementModel?,
        remoteJson: String,
    ): FullScreenPlacementModel? {
        val trimmed = remoteJson.trim()
        if (trimmed.isEmpty() || !trimmed.startsWith("{")) {
            return defaultModel
        }
        return try {
            json.decodeFromString(FullScreenPlacementModel.serializer(), trimmed).also {
                Log.d(TAG, "getRemoteFullScreenPlacementModel: full screen -> $it")
            }
        } catch (ex: Exception) {
            Log.e(TAG, "getRemoteFullScreenPlacementModel: ", ex)
            defaultModel
        }
    }

    private fun getRemoteInlinePlacementModel(
        defaultModel: InlinePlacementModel? = null,
        remoteJson: String,
    ): InlinePlacementModel? {
        val trimmed = remoteJson.trim()
        if (trimmed.isEmpty() || !trimmed.startsWith("{")) {
            return defaultModel
        }
        return try {
            (json.decodeFromString(
                PlacementModel.serializer(),
                trimmed
            ) as InlinePlacementModel)
                .also {
                    Log.d(TAG, "getRemoteInlinePlacementModel: inline -> $it")
                }
        } catch (ex: Exception) {
            Log.e(TAG, "getRemoteInlinePlacementModel: ", ex)
            defaultModel
        }
    }

    private fun getRemoteBreakPointModel(
        defaultModel: BreakPointModel?,
        remoteJson: String,
    ): BreakPointModel? {
        return try {
            if (remoteJson.isEmpty()) {
                defaultModel
            } else {
                json.decodeFromString(BreakPointModel.serializer(), remoteJson).also {
                    Log.d(TAG, "getRemoteBreakPointModel: $it")
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "getRemoteBreakPointModel: ", ex)
            defaultModel
        }
    }

    private fun getRemoteConditionalModels(
        defaultModels: List<ConditionalModel>?,
        remoteJson: String,
    ): List<ConditionalModel>? {
        return try {
            if (remoteJson.isEmpty()) {
                defaultModels
            } else {
                json.decodeFromString(ListSerializer(ConditionalModel.serializer()), remoteJson)
                    .also {
                        Log.d(TAG, "getRemoteConditionalModels: $it")
                    }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "getRemoteConditionalModels: ", ex)
            defaultModels
        }
    }


    private fun AppAdsConfig.validated(): AppAdsConfig {
        return filterForKnownAdTypes(monetizationApp.integrationManager)
            .filterForDebug(monetizationApp.integrationManager)
    }

    companion object {

        private const val TAG = "MonetizationRemoteTAG"

    }
}