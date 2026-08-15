package com.systematics.monetization.core.interfaces

interface AdShowListener {
    fun onAdShown()
    fun adShowingFailed(ex: Exception)
}