package com.systematics.app.presentation.screens.main

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.systematics.monetization.ui.MonetizationInstall
import com.systematics.monetization.ui.compose.utils.monetizationInject
import com.systematics.monetization.ui.utils.MonetizationSharedState
import com.systematics.app.R
import com.systematics.app.domain.usecase.MarkFirstSessionCompletedUseCase
import com.systematics.app.domain.usecase.IsAdsEnabledUseCase
import com.systematics.app.utils.monetization.config.frontend.breakpoints.AdBreakPoint
import com.systematics.app.utils.update.AppUpdateHelper
import com.systematics.app.utils.update.SdkUpdateListener
import com.systematics.app.presentation.core.utils.ReusableAlertDialog
import com.systematics.app.presentation.screens.home.HomeScreen
import com.systematics.app.presentation.core.utils.ReusableBottomBar
import com.systematics.app.presentation.core.utils.MainTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "MainScreenTAG"

@Composable
fun MainScreen(
    onFeatureOneClick: () -> Unit,
    onFeatureTwoClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPremiumClick: () -> Unit,
    isBillingEnabled: Boolean,
    updateHelper: AppUpdateHelper = koinInject(),
    monetizationInstall: MonetizationInstall = monetizationInject(),
    markFirstSessionCompleted: MarkFirstSessionCompletedUseCase = koinInject(),
    isAdsEnabled: IsAdsEnabledUseCase = koinInject()
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var showExitDialog by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(pageCount = { MainTab.entries.size })
    val coroutineScope = rememberCoroutineScope()

    var isAdWorkDone by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        delay(100.milliseconds)
        if (isAdsEnabled() && !isAdWorkDone) {
            markFirstSessionCompleted()
            MonetizationSharedState.unblockAppOpenManually()
            monetizationInstall.executeBreakPoint(AdBreakPoint.BP_HOME_APPEAR)
            isAdWorkDone = true
        }
    }

    var inAppUpdatedChecked by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        if (!inAppUpdatedChecked) {
            updateHelper.checkAndStartImmediateUpdate(
                activity = context as ComponentActivity, callback = object : SdkUpdateListener {
                    override fun onUpdateFailed(reason: String) {
                        Log.d(TAG, "onUpdateFailed: $reason")
                    }

                    override fun onUpdateStarted() {
                        Log.d(TAG, "onUpdateStarted: ")
                    }

                    override fun onUpdateSuccess() {
                        Log.d(TAG, "onUpdateSuccess: ")
                    }
                }
            )
        }
    }


    BackHandler {
        if (pagerState.currentPage == 0) {
            showExitDialog = true
        } else {
            coroutineScope.launch {
                pagerState.scrollToPage(0)
            }
        }
    }

    Scaffold(
        bottomBar = {
            ReusableBottomBar(
                selected = MainTab.entries[pagerState.currentPage], onSelect = { tab ->
                    coroutineScope.launch {
                        pagerState.scrollToPage(tab.ordinal)
                    }
                }
            )
        }) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            userScrollEnabled = false
        ) { page ->
            when (MainTab.entries[page]) {
                MainTab.Home -> {
                    HomeScreen(
                        onFeatureOneClick = onFeatureOneClick,
                        onFeatureTwoClick = onFeatureTwoClick,
                        onSettingsClick = onSettingsClick,
                        onPremiumClick = onPremiumClick,
                        isBillingEnabled = isBillingEnabled
                    )
                }
            }
        }
    }

    if (showExitDialog) {
        ReusableAlertDialog(
            title = stringResource(R.string.exit_app_title),
            body = stringResource(R.string.exit_app_body),
            confirmText = stringResource(R.string.exit),
            onConfirm = {
                showExitDialog = false
                activity?.finish()
            },
            onDismiss = { showExitDialog = false },
            dismissText = stringResource(R.string.cancel),
        )
    }
}
