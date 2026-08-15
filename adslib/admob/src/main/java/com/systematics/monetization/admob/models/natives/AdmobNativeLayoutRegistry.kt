package com.systematics.monetization.admob.models.natives

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.systematics.monetization.admob.ui.store.defaultAdmobNativePopulator
import com.systematics.monetization.core.integration.natives.NativeLayoutPopulator
import com.systematics.monetization.core.integration.natives.NativeLayoutRegistry
import com.systematics.monetization.core.models.natives.NativeUiDataModel

data class AdmobNativeLayoutRegistry(
    val viewId: Int,
    val populator: NativeLayoutPopulator<AdmobAppNativeAd> = defaultAdmobNativePopulator
) : NativeLayoutRegistry<AdmobAppNativeAd>() {

    override fun createView(context: Context, parent: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(viewId, parent ?: FrameLayout(context), false)
    }

    override fun populate(
        context: Context,
        view: View,
        ad: AdmobAppNativeAd,
        nativeUiDataModel: NativeUiDataModel
    ) {
        populator.populate(context, view, ad, nativeUiDataModel)
    }
}