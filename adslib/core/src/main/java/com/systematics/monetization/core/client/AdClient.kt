package com.systematics.monetization.core.client

import android.content.Context
import android.util.Log
import com.systematics.monetization.core.analytics.AdsEvents
import com.systematics.monetization.core.analytics.AnalyticsEventsListener
import com.systematics.monetization.core.interfaces.AdGroupLoadWListener
import com.systematics.monetization.core.managers.wrappers.AdGroupLoadWrapper
import com.systematics.monetization.core.models.AdGroupResult
import com.systematics.monetization.core.models.AdRequester
import com.systematics.monetization.core.models.AdRequesterLoaded
import com.systematics.monetization.core.models.AdRequesterWaited
import com.systematics.monetization.core.models.ad.local.group.abs.AdInfoGroup
import com.systematics.monetization.core.utils.EventsConstants.AD_INFO_PAIR_FAILED
import com.systematics.monetization.core.utils.EventsConstants.AD_NOT_LOADED
import com.systematics.monetization.core.utils.EventsConstants.AD_NOT_REQUESTED
import com.systematics.monetization.core.utils.InternetController
import com.systematics.monetization.core.utils.handleFailedAdResponse
import com.systematics.monetization.core.utils.handleSuccessAdResponse
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class AdClient(
    context: Context,
    internetController: InternetController,
    analyticsLogger: (AdsEvents) -> Unit,
) {

    private val adGroupLoadWrapper = AdGroupLoadWrapper(context, internetController,analyticsLogger)
    private var analyticsEventsListener: AnalyticsEventsListener? = null

    private val adResponseWaiter: MutableMap<String, MutableList<AdRequester>> = mutableMapOf()

    init {

        adGroupLoadWrapper.adGroupLoadWListener = object : AdGroupLoadWListener {

            override fun onAdLoaded(adType: String, getAdGroupResult: () -> AdGroupResult?) {
                adResponseWaiter[adType]?.let { waitersForAd ->
                    MainScope().launch {
                        waitersForAd.removeFirstOrNull()?.let {
                            Log.d(TAG, "onAdLoaded: handing waited $it")
                            getAdGroupResult()?.let { adGroupResult ->
                                handleSuccessAdResponse(it, adGroupResult) {
                                    adGroupLoadWrapper.onAdShown(adGroupResult)
                                }
                            } ?: run {
                                analyticsEventsListener?.onEvent(AdsEvents(AD_INFO_PAIR_FAILED))
                                handleFailedAdResponse(it, Exception(AD_INFO_PAIR_FAILED))
                            }
                        }
                    }
                }
            }

            override fun onAdLoadFailed(adType: String, exception: Exception) {
                adResponseWaiter[adType]?.let { waitersForAd ->
                    adResponseWaiter[adType] = arrayListOf()
                    waitersForAd.forEach { handleFailedAdResponse(it, exception) }
                }
            }
        }
    }

    fun addAdRequester(addRequest: AdRequester) {
        if (adGroupLoadWrapper.isLoadRequested(addRequest.adGroupType)) {
            if (adGroupLoadWrapper.isAdLoaded(addRequest.adGroupType)) {
                Log.d(TAG, "addAdGroupRequester: ${addRequest.adGroupType} ad loaded success")
                adGroupLoadWrapper.getLoadedAdGroupResult(addRequest.adGroupType)
                    ?.let { adGroupResult ->
                        handleSuccessAdResponse(addRequest, adGroupResult) {
                            adGroupLoadWrapper.onAdShown(adGroupResult)
                        }
                    } ?: run {
                    analyticsEventsListener?.onEvent(AdsEvents(AD_INFO_PAIR_FAILED))
                    handleFailedAdResponse(addRequest, Exception(AD_INFO_PAIR_FAILED))
                }
            } else {
                when (addRequest) {
                    is AdRequesterWaited -> {
                        Log.d(
                            TAG,
                            "addAdGroupRequester: ${addRequest.adGroupType} added into waiting"
                        )
                        adResponseWaiter.getOrPut(addRequest.adGroupType) { arrayListOf() }
                            .add(addRequest)
                    }

                    is AdRequesterLoaded -> {
                        Log.d(
                            TAG,
                            "addAdGroupRequester: ${addRequest.adGroupType} ad not loaded yet"
                        )
                        handleFailedAdResponse(addRequest, Exception(AD_NOT_LOADED))
                    }
                }
            }
        } else {
            Log.d(TAG, "addAdGroupRequester: ad not requested for ${addRequest.adGroupType}")
            handleFailedAdResponse(addRequest, Exception(AD_NOT_REQUESTED))
        }
    }

    fun removeRequester(adRequester: AdRequester) {
        adResponseWaiter[adRequester.adGroupType]?.let {
            Log.d(
                TAG,
                "removeRequester: removing requester for ${adRequester.adGroupType} ${adRequester.adTag}"
            )
            it.remove(adRequester)
        }
    }

    fun clearAllRequesters() {
        adResponseWaiter.clear()
    }

    fun loadAdGroup(adInfoGroup: AdInfoGroup) {
        Log.d(TAG, "loadAdGroup: requested async load for ${adInfoGroup.adTAG}")
        adGroupLoadWrapper.loadAdNow(adInfoGroup)
    }

    fun destroyAdGroupType(adGroupType: String) {
        Log.d(TAG, "destroyAdGroupType: request destroy for $adGroupType")
        adGroupLoadWrapper.destroyGroupTypeNow(adGroupType)
    }

    fun isAdLoaded(adType: String, manualOnly: Boolean = false) =
        adGroupLoadWrapper.isAdLoaded(adType, manualOnly)

    fun isAdLoading(adType: String) = adGroupLoadWrapper.isAdLoading(adType)

    fun getLoadedAdsCount(adType: String): Int = adGroupLoadWrapper.getLoadedAdsCount(adType)

    fun onNativeAdShown(adGroupResult: AdGroupResult) {
        adGroupLoadWrapper.onAdShown(adGroupResult)
    }

    companion object {

        private const val TAG = "AdMobClientTAG"
    }
}