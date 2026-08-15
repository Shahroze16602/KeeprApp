package com.systematics.keepr.di

import com.systematics.keepr.utils.core.AppLogEvents
import com.systematics.keepr.utils.core.InternetController
import com.systematics.keepr.data.keepr.KeeprController
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val frameworkModule = module {
    singleOf(::AppLogEvents)
    singleOf(::InternetController)
    singleOf(::KeeprController)
}
