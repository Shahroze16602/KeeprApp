package com.systematics.monetization.core.analytics

import android.os.Bundle

data class AdsEvents(
    val name: String,
    val params: (Bundle.() -> Unit)? = null
)