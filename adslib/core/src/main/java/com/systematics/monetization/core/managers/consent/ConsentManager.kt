package com.systematics.monetization.core.managers.consent

import android.app.Activity

interface ConsentManager {

    fun show(activity: Activity, onDone: (ConsentStatus) -> Unit)
}