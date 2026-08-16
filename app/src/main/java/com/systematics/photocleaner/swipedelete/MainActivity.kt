package com.systematics.photocleaner.swipedelete

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val appLogEvents: AppLogEvents by inject()
    private val isAdsEnabled: IsAdsEnabledUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
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

}
