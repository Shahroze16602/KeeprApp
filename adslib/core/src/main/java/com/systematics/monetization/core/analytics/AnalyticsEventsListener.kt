package com.systematics.monetization.core.analytics

interface AnalyticsEventsListener {

    fun onEvent(adsEvents: AdsEvents)
}