package com.systematics.app.di

import com.systematics.app.utils.core.AppLogEvents
import com.systematics.app.utils.core.InternetController
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val frameworkModule = module {
    singleOf(::AppLogEvents)
    singleOf(::InternetController)
}
