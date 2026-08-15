package com.systematics.keepr.utils.providers

import androidx.compose.runtime.compositionLocalOf
import com.systematics.keepr.utils.core.AppLogEvents

val LocalAppLogEvents = compositionLocalOf<AppLogEvents> { error("No NavController found!") }