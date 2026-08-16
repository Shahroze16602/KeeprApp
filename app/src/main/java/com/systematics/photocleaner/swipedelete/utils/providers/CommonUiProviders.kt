package com.systematics.photocleaner.swipedelete.utils.providers

import androidx.compose.runtime.compositionLocalOf
import com.systematics.photocleaner.swipedelete.utils.core.AppLogEvents

val LocalAppLogEvents = compositionLocalOf<AppLogEvents> { error("No NavController found!") }