package com.systematics.app.utils.providers

import androidx.compose.runtime.compositionLocalOf
import com.systematics.app.utils.core.AppLogEvents

val LocalAppLogEvents = compositionLocalOf<AppLogEvents> { error("No NavController found!") }