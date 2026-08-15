package com.systematics.monetization.core.integration.natives

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.systematics.monetization.core.models.natives.AppNativeAd
import com.systematics.monetization.core.models.natives.NativeUiDataModel

abstract class NativeLayoutRegistry<T : AppNativeAd>() {

    abstract fun createView(context: Context, parent: ViewGroup? = null): View

    abstract fun populate(context: Context, view: View, ad: T, nativeUiDataModel: NativeUiDataModel)
}