package com.systematics.monetization.core.integration.natives

import android.content.Context
import android.view.View
import com.systematics.monetization.core.models.natives.NativeUiDataModel

interface NativeLayoutPopulator<T> {

    fun populate(
        context: Context,
        view: View,
        ad: T,
        nativeUiDataModel: NativeUiDataModel
    )
}