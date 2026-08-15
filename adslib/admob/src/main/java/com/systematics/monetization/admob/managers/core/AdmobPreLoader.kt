package com.systematics.monetization.admob.managers.core

abstract class AdmobPreLoader<T>() {

    abstract fun getAd(): T?
    abstract fun pushAd()
    abstract fun isAdAvailable(): Boolean
}