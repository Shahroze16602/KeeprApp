package com.systematics.keepr.utils.update

import android.content.Context
import androidx.activity.ComponentActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

interface SdkUpdateListener {
    fun onUpdateFailed(reason: String)
    fun onUpdateStarted()
    fun onUpdateSuccess()
}

class AppUpdateHelper(context: Context) {
    private val appUpdateManager = AppUpdateManagerFactory.create(context.applicationContext)

    fun checkAndStartImmediateUpdate(
        activity: ComponentActivity,
        callback: SdkUpdateListener
    ) {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                when {
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                        val started = appUpdateManager.startUpdateFlowForResult(
                            info,
                            AppUpdateType.IMMEDIATE,
                            activity,
                            UPDATE_REQUEST_CODE
                        )
                        if (started) callback.onUpdateStarted()
                        else callback.onUpdateFailed("Google Play declined to start the update flow")
                    }

                    info.updateAvailability() ==
                        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        val resumed = appUpdateManager.startUpdateFlowForResult(
                            info,
                            AppUpdateType.IMMEDIATE,
                            activity,
                            UPDATE_REQUEST_CODE
                        )
                        if (resumed) callback.onUpdateStarted()
                        else callback.onUpdateFailed("Google Play declined to resume the update flow")
                    }

                    else -> callback.onUpdateSuccess()
                }
            }
            .addOnFailureListener { error ->
                callback.onUpdateFailed(error.message ?: error.javaClass.simpleName)
            }
    }

    private companion object {
        const val UPDATE_REQUEST_CODE = 7102
    }
}
