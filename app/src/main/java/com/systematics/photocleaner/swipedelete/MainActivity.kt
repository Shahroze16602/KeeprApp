package com.systematics.photocleaner.swipedelete

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.systematics.monetization.ui.state.DialogState
import com.systematics.monetization.ui.utils.MonetizationSharedState
import com.systematics.photocleaner.swipedelete.utils.core.AppLogEvents
import com.systematics.photocleaner.swipedelete.domain.usecase.IsAdsEnabledUseCase
import com.systematics.photocleaner.swipedelete.dialogs.AdDialog
import com.systematics.photocleaner.swipedelete.utils.providers.LocalAppLogEvents
import com.systematics.photocleaner.swipedelete.navigation.SwipeDeleteApp
import com.systematics.photocleaner.swipedelete.ui.theme.AppTheme
import com.systematics.photocleaner.swipedelete.utils.update.AppUpdateHelper
import com.systematics.photocleaner.swipedelete.utils.update.SdkUpdateListener
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity(), SdkUpdateListener {
    private val appLogEvents: AppLogEvents by inject()
    private val isAdsEnabled: IsAdsEnabledUseCase by inject()
    private val appUpdateHelper: AppUpdateHelper by inject()
    private val immediateUpdateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_OK -> onUpdateSuccess()
            RESULT_CANCELED -> onUpdateFailed("The immediate update was cancelled")
            else -> onUpdateFailed("The immediate update failed with result code ${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
        appUpdateHelper.checkAndStartImmediateUpdate(immediateUpdateLauncher, this)
        setContent {
            val adsDialogState by MonetizationSharedState.dialogState.collectAsStateWithLifecycle()
            val navController = rememberNavController()
            AppTheme {
                CompositionLocalProvider(LocalAppLogEvents provides appLogEvents) {
                    SwipeDeleteApp(navController = navController)

                    if (isAdsEnabled() && adsDialogState is DialogState.Show) {
                        if ((adsDialogState as DialogState.Show).type == "appOpenBg") {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(androidx.compose.ui.graphics.Color.Black)
                            )
                        } else {
                            AdDialog(onDismiss = {})
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateHelper.resumeImmediateUpdateIfInProgress(immediateUpdateLauncher, this)
    }

    override fun onUpdateFailed(reason: String) {
        Log.w(TAG, "Immediate in-app update check failed: $reason")
    }

    override fun onUpdateStarted() {
        Log.d(TAG, "Immediate in-app update flow started")
    }

    override fun onUpdateSuccess() {
        Log.d(TAG, "Immediate in-app update check or flow completed")
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}
