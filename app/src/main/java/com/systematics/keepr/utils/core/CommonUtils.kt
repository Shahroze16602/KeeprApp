package com.systematics.keepr.utils.core

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import com.systematics.keepr.R

private const val TAG = "CommonUtilsTAG"

fun <T> Collection<T>.firstEntry(): T? {
    return if (this.isEmpty())
        null
    else this.first()
}

fun <T> Collection<T>.lastEntry(): T? {
    return if (this.isEmpty())
        null
    else this.last()
}

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

fun Context.getAppVersion(): String {
    return try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName ?: "Unknown"
    } catch (_: Exception) {
        "Unknown"
    }
}

fun Context.rateOnPlayStore() {
    val intent = Intent(
        Intent.ACTION_VIEW,
        "https://play.google.com/store/apps/details?id=${packageName}".toUri()
    )
    try {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    } catch (_: Exception) {

    }
}

fun Context.shareAppLink() {
    val appLink = "https://play.google.com/store/apps/details?id=$packageName"
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, appLink)
    }

    val chooserIntent = Intent.createChooser(shareIntent, getString(R.string.share_app)).apply {
        if (this@shareAppLink !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    runCatching {
        startActivity(chooserIntent)
    }.onFailure { throwable ->
        if (throwable !is ActivityNotFoundException) {
            throw throwable
        }
    }
}

fun Context.getAppVersionCode(): Long {
    return runCatching {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        PackageInfoCompat.getLongVersionCode(packageInfo)
    }.getOrDefault(0L)
}

fun Context.sendFeedbackEmail(
    email: String,
    appName: String,
    versionCode: Long,
    message: String
) {
    val subject = Uri.encode(
        "$appName - App Version $versionCode\nOS Version- ${Build.VERSION.SDK_INT}\nDevice Model-${Build.MANUFACTURER} ${Build.MODEL}"
    )
    val body = Uri.encode(message)
    val uri = "mailto:$email?subject=$subject&body=$body".toUri()
    val intent = Intent(Intent.ACTION_SENDTO).apply { data = uri }
    runCatching {
        startActivity(Intent.createChooser(intent, "Send Email"))
    }.onFailure { throwable ->
        if (throwable !is ActivityNotFoundException) {
            throw throwable
        }
    }
}
