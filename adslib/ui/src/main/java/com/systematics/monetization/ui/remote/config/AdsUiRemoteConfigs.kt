package com.systematics.monetization.ui.remote.config

interface AdsUiRemoteConfigs {

    // Counter
    val appAdCounter: (String) -> Long
    val appAdCounterDefault: (String) -> Long

    // Timer
    val appAdTimer: (String) -> Long
    val appAdTimerDefault: (String) -> Long

    // Ads Remote
    val remoteJSON: (String) -> String
}