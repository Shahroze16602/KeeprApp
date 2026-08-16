package com.systematics.photocleaner.swipedelete.utils.update

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

interface SdkUpdateListener {
    fun onUpdateFailed(reason: String)
    fun onUpdateStarted()
    fun onUpdateSuccess()
}

class AppUpdateHelper(context: Context) {
    private val appUpdateManager = AppUpdateManagerFactory.create(context.applicationContext)
    private var updateCheckInProgress = false

    fun checkAndStartImmediateUpdate(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        callback: SdkUpdateListener
    ) = checkForImmediateUpdate(
        activityResultLauncher = activityResultLauncher,
        callback = callback,
        startAvailableUpdate = true
    )

    fun resumeImmediateUpdateIfInProgress(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        callback: SdkUpdateListener
    ) = checkForImmediateUpdate(
        activityResultLauncher = activityResultLauncher,
        callback = callback,
        startAvailableUpdate = false
    )

    private fun checkForImmediateUpdate(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        callback: SdkUpdateListener,
        startAvailableUpdate: Boolean
    ) {
        if (updateCheckInProgress) return
        updateCheckInProgress = true

        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                updateCheckInProgress = false
                when {
                    startAvailableUpdate &&
                        info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                        startImmediateUpdate(
                            activityResultLauncher = activityResultLauncher,
                            appUpdateInfo = info,
                            failureMessage = "Google Play declined to start the update flow",
                            callback = callback
                        )
                    }

                    info.updateAvailability() ==
                        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        startImmediateUpdate(
                            activityResultLauncher = activityResultLauncher,
                            appUpdateInfo = info,
                            failureMessage = "Google Play declined to resume the update flow",
                            callback = callback
                        )
                    }

                    else -> callback.onUpdateSuccess()
                }
            }
            .addOnFailureListener { error ->
                updateCheckInProgress = false
                callback.onUpdateFailed(error.message ?: error.javaClass.simpleName)
            }
    }

    private fun startImmediateUpdate(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        appUpdateInfo: AppUpdateInfo,
        failureMessage: String,
        callback: SdkUpdateListener
    ) {
        runCatching {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                activityResultLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
        }.onSuccess { started ->
            if (started) callback.onUpdateStarted()
            else callback.onUpdateFailed(failureMessage)
        }.onFailure { error ->
            callback.onUpdateFailed(error.message ?: error.javaClass.simpleName)
        }
    }
}
