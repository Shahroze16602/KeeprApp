package com.systematics.monetization.admob.consent

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.systematics.monetization.core.managers.consent.ConsentManager
import com.systematics.monetization.core.managers.consent.ConsentStatus
import com.systematics.monetization.core.utils.ALog
import java.util.concurrent.atomic.AtomicBoolean

class GoogleConsentManager : ConsentManager {

    private lateinit var consentInformation: ConsentInformation
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)

    override fun show(activity: Activity, onDone: (ConsentStatus) -> Unit) {
        ALog.d(TAG, "consent checking started")
        if (isMobileAdsInitializeCalled.get()){
            isMobileAdsInitializeCalled.set(false)
        }

        // Create a ConsentRequestParameters object.
        val params = ConsentRequestParameters
            .Builder()
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity, params,
            {
                ALog.d(TAG, "consent info updated")
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        ALog.d(TAG, "consent form not shown: ${loadAndShowError.message}")
                    }
                    // Consent has been gathered.
                    if (consentInformation.canRequestAds()) {
                        ALog.d(TAG, "consent granted")
                        initializeMobileAdsSdk(true, onDone = onDone)
                    }
                }
            },
            { requestConsentError ->
                // Consent gathering failed.
                ALog.d(TAG, "new consent failed: ${requestConsentError.message}")
                initializeMobileAdsSdk(consentInformation.canRequestAds(), onDone = onDone)
            }
        )

        // Check if you can initialize the Google Mobile Ads SDK in parallel
        // while checking for new consent information. Consent obtained in
        // the previous session can be used to request ads.
        if (consentInformation.canRequestAds()) {
            Log.d(TAG, "consent reused")
            initializeMobileAdsSdk(true, onDone = onDone)
        }
    }

    private fun initializeMobileAdsSdk(
        canRequestAds: Boolean,
        onDone: (ConsentStatus) -> Unit
    ) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        val consentStatus = if (canRequestAds) ConsentStatus.GRANTED else ConsentStatus.FAILED
        onDone(consentStatus)
    }

    companion object {

        private const val TAG = "Google Consent"
    }
}