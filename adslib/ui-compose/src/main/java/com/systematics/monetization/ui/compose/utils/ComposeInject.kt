package com.systematics.monetization.ui.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.systematics.monetization.ui.di.MonetizationDIContainer
import com.systematics.monetization.ui.utils.get

@Composable
inline fun <reified T> monetizationInject(): T {
    return remember {
        MonetizationDIContainer.get<T>()
    }
}