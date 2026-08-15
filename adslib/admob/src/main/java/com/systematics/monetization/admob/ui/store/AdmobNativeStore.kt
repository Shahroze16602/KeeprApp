package com.systematics.monetization.admob.ui.store

import com.systematics.monetization.admob.R
import com.systematics.monetization.admob.models.natives.AdmobAppNativeAd
import com.systematics.monetization.admob.models.natives.AdmobNativeLayoutRegistry
import com.systematics.monetization.core.integration.natives.NativeRegistryModel

val defaultAdmobNativePopulator by lazy { DefaultAdmobNativePopulator() }

val defaultAdmobRegistryMap = mutableMapOf(
    "full_screen_native" to AdmobNativeLayoutRegistry(
        R.layout.adview_full_screen_native,
        defaultAdmobNativePopulator
    ),
    "large_native_large_cta" to AdmobNativeLayoutRegistry(
        R.layout.adview_large_native_large_cta,
        defaultAdmobNativePopulator
    ),
    "large_native_large_cta_bottom_detail" to AdmobNativeLayoutRegistry(
        R.layout.adview_large_native_large_cta_bottom_detail,
        defaultAdmobNativePopulator
    ),
    "large_native_large_cta_inverted" to AdmobNativeLayoutRegistry(
        R.layout.adview_large_native_large_cta_inverted,
        defaultAdmobNativePopulator
    ),
    "large_native_large_cta_top" to AdmobNativeLayoutRegistry(
        R.layout.adview_large_native_large_cta_top,
        defaultAdmobNativePopulator
    ),
    "small_native_large_cta" to AdmobNativeLayoutRegistry(
        R.layout.adview_small_native_large_cta,
        defaultAdmobNativePopulator
    ),
    "small_native_large_cta_top" to AdmobNativeLayoutRegistry(
        R.layout.adview_small_native_large_cta_top,
        defaultAdmobNativePopulator
    ),
    "small_native_small_cta" to AdmobNativeLayoutRegistry(
        R.layout.adview_small_native_small_cta,
        defaultAdmobNativePopulator
    ),
    "split_native_large_cta" to AdmobNativeLayoutRegistry(
        R.layout.adview_split_native_large_cta,
        defaultAdmobNativePopulator
    ),
    "split_native_large_cta_top" to AdmobNativeLayoutRegistry(
        R.layout.adview_split_native_large_cta_top,
        defaultAdmobNativePopulator
    ),
    "split_native_small_cta" to AdmobNativeLayoutRegistry(
        R.layout.adview_split_native_small_cta,
        defaultAdmobNativePopulator
    ),
    "split_native_small_cta_top" to AdmobNativeLayoutRegistry(
        R.layout.adview_split_native_small_cta_top,
        defaultAdmobNativePopulator
    )
)

val defaultAdmobNativeRegistryModel = NativeRegistryModel(
    registryClass = AdmobAppNativeAd::class,
    registryMap = defaultAdmobRegistryMap
)