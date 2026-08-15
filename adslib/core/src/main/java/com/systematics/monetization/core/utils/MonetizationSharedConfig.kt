package com.systematics.monetization.core.utils

import com.systematics.monetization.core.BuildConfig

object MonetizationSharedConfig {

    var buildConfig =
        if (BuildConfig.DEBUG) MonetizationBuildConfig.DEBUG else MonetizationBuildConfig.RELEASE

    val isDebug: Boolean
        get() = buildConfig == MonetizationBuildConfig.DEBUG
}