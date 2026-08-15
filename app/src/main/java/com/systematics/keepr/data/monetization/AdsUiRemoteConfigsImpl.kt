package com.systematics.keepr.data.monetization

import com.systematics.keepr.data.datasource.FirebaseConfigDataSource
import com.systematics.monetization.ui.remote.config.AdsUiRemoteConfigs

class AdsUiRemoteConfigsImpl(private val configDataSource: FirebaseConfigDataSource) : AdsUiRemoteConfigs {

    override val appAdCounter: (String) -> Long = { configDataSource.rawLong(it) }
    override val appAdCounterDefault: (String) -> Long = { configDataSource.rawLong(it) }

    override val appAdTimer: (String) -> Long = { configDataSource.rawLong(it) }
    override val appAdTimerDefault: (String) -> Long = { configDataSource.rawLong(it) }

    override val remoteJSON: (String) -> String = { configDataSource.string(it) }
}
