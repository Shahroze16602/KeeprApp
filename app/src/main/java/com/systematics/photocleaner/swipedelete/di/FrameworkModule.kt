package com.systematics.photocleaner.swipedelete.di

import com.systematics.photocleaner.swipedelete.utils.core.AppLogEvents
import com.systematics.photocleaner.swipedelete.utils.core.InternetController
import com.systematics.photocleaner.swipedelete.data.swipedelete.SwipeDeleteController
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val frameworkModule = module {
    singleOf(::AppLogEvents)
    singleOf(::InternetController)
    singleOf(::SwipeDeleteController)
}
