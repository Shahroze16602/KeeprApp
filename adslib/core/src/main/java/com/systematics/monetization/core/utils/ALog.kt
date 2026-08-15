package com.systematics.monetization.core.utils

import android.util.Log

private const val TAG = "MonetizationLib"

object ALog {

    fun d(
        tag: String,
        msg: String
    ) {
        Log.d("$TAG $tag", msg)
    }

    fun e(
        tag: String,
        msg: String,
        exception: Exception
    ) {
        Log.e("$TAG $tag", msg, exception)
    }

    fun d(
        vararg tags: String,
        msg: String
    ) {
        val separatedParams = tags.joinToString(" ")
        Log.d("$TAG $separatedParams", msg)
    }
}