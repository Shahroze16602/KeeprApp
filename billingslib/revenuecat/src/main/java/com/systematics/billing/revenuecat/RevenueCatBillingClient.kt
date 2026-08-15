package com.systematics.billing.revenuecat

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.EntitlementInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.purchaseWith
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RevenueCatBillingClient(context: Context) {

    private val logLevel: LogLevel = LogLevel.DEBUG
    private val isConfigured = REVENUECAT_API_KEY.isNotBlank()

    companion object {
        private const val OFFERINGS_ID = "default_offerings"
        private const val TAG = "RevenueCatBillingClientTAG"
        // Replace with the RevenueCat Android public SDK key.
        private const val REVENUECAT_API_KEY = ""
    }

    init {
        if (isConfigured) {
            Purchases.logLevel = logLevel
            val configuration = PurchasesConfiguration.Builder(
                context.applicationContext, REVENUECAT_API_KEY
            ).build()
            Purchases.configure(configuration)
        } else {
            Log.w(TAG, "RevenueCat disabled: no owned API key configured")
        }
    }

    fun queryPurchases(
        entitlementIds: List<String>,
        onPurchasesFound: (List<EntitlementInfo>) -> Unit,
        onNoPurchasesFound: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        if (!isConfigured) {
            onNoPurchasesFound()
            return
        }
        Purchases.sharedInstance.getCustomerInfoWith(onSuccess = { info ->
            val activeEntitlements = getActiveEntitlements(info, entitlementIds)
            val isActive = activeEntitlements.isNotEmpty()
            if (isActive) {
                Log.d(TAG, "queryPurchases: purchases found: $activeEntitlements")
                onPurchasesFound(activeEntitlements)
            } else {
                Log.d(TAG, "queryPurchases: no purchases found")
                onNoPurchasesFound()
            }
        }, onError = { error ->
            Log.d(TAG, "queryPurchases: failed error message: ${error.message}")
            onFailed(error.underlyingErrorMessage ?: error.message)
        })
    }

    private fun getActiveEntitlements(
        info: CustomerInfo, entitlementIds: List<String> = emptyList()
    ): List<EntitlementInfo> {
        val entitlements = if (entitlementIds.isEmpty()) {
            info.entitlements.all.values
        } else {
            entitlementIds.mapNotNull { info.entitlements[it] }
        }

        return entitlements.filter { it.isActive }
    }

    suspend fun queryPackageDetails(packageIds: List<String>) =
        suspendCancellableCoroutine { continuation ->
            if (!isConfigured) {
                continuation.resumeWithException(
                    IllegalStateException("RevenueCat is not configured")
                )
                return@suspendCancellableCoroutine
            }
            Log.d(TAG, "queryPackageDetails: Fetching packages for offering: $OFFERINGS_ID")
            Purchases.sharedInstance.getOfferingsWith(onSuccess = { offerings ->
                val packages = offerings[OFFERINGS_ID]?.availablePackages?.filter { pkg ->
                    pkg.identifier in packageIds
                }.orEmpty()

                Log.d(
                    TAG,
                    "queryPackageDetails: Packages fetched: ${packages.map { it.identifier }}"
                )
                continuation.resume(packages)
            }, onError = { error ->
                Log.d(TAG, "queryPackageDetails: getOfferings error: ${error.message}")
                continuation.resumeWithException(
                    Exception("queryPackageDetails: Failed to query products: msg= ${error.message}")
                )
            })
        }

    fun purchasePremiumOffer(
        activity: Activity,
        packageDetails: Package,
        onPurchasesFound: (List<EntitlementInfo>) -> Unit,
        onNoPurchasesFound: () -> Unit,
        onFailed: (Boolean, String) -> Unit
    ) {
        if (!isConfigured) {
            onFailed(false, "RevenueCat is not configured")
            return
        }
        Log.d(
            TAG,
            "purchasePremiumOffer: Starting purchase for package: ${packageDetails.identifier}"
        )

        if (activity.isFinishing || (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed)) {
            Log.w(TAG, "purchasePremiumOffer: activity not in a valid state to launch billing flow")
            onFailed(false, "activity not in a valid state to launch billing flow")
            return
        }

        val params = PurchaseParams.Builder(activity, packageDetails).build()
        Purchases.sharedInstance.purchaseWith(purchaseParams = params, onSuccess = { _, info ->
            val activeEntitlements = getActiveEntitlements(info)
            val isActive = activeEntitlements.isNotEmpty()
            Log.d(TAG, "purchasePremiumOffer: Premium active = $isActive")
            if (isActive) {
                onPurchasesFound(activeEntitlements)
            } else {
                onNoPurchasesFound()
            }
        }, onError = { error, userCancelled ->
            Log.d(
                TAG,
                "purchasePremiumOffer: Error message: ${error.message}, userCancelled=$userCancelled"
            )
            onFailed(userCancelled, error.underlyingErrorMessage ?: error.message)
        })
    }
}
