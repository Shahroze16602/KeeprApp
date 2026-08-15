package com.systematics.monetization.ui.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

fun Lifecycle.onceWhileVisibleScope(): CoroutineScope {
    val job = SupervisorJob()
    val scope = CoroutineScope(job + Dispatchers.Main.immediate)

    addObserver(object : DefaultLifecycleObserver {

        override fun onStop(owner: LifecycleOwner) {
            job.cancel()
            owner.lifecycle.removeObserver(this)
        }
    })

    return scope
}