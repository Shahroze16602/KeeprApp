package com.systematics.photocleaner.swipedelete.presentation.screens.privacy_policy

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systematics.photocleaner.swipedelete.R
import com.systematics.photocleaner.swipedelete.presentation.swipedelete.HardSurface
import com.systematics.photocleaner.swipedelete.presentation.swipedelete.Heading
import com.systematics.photocleaner.swipedelete.presentation.swipedelete.SwipeDeleteDimens
import com.systematics.photocleaner.swipedelete.presentation.swipedelete.SwipeDeleteHeader
import com.systematics.photocleaner.swipedelete.presentation.swipedelete.Kicker
import com.systematics.photocleaner.swipedelete.presentation.swipedelete.LocalSwipeDeletePalette
import org.koin.androidx.compose.koinViewModel

@Composable
fun PrivacyPolicyScreen(
    onBackPress: () -> Unit,
    viewModel: PrivacyPolicyViewModel = koinViewModel(),
) {
    BackHandler { onBackPress() }

    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val palette = LocalSwipeDeletePalette.current
    val targetUrl = state.privacyPolicyLink.safe()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.app)
            .systemBarsPadding(),
    ) {
        SwipeDeleteHeader("Your data", "Privacy policy", onBackPress)
        HardSurface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 18.dp, top = 4.dp, end = 18.dp, bottom = 18.dp),
            radius = SwipeDeleteDimens.radiusLg,
            background = palette.card,
            borderColor = palette.border,
            shadowX = 4.dp,
            shadowY = 5.dp,
            contentPadding = PaddingValues(0.dp),
        ) {
            AndroidView(
                factory = {
                    WebView(context).apply {
                        setBackgroundColor(palette.card.toArgb())
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                viewModel.isLoading(false)
                            }
                        }
                    }
                },
                update = { webView ->
                    webView.setBackgroundColor(palette.card.toArgb())
                    if (targetUrl.isNotBlank() && webView.tag != targetUrl) {
                        webView.tag = targetUrl
                        viewModel.isLoading(true)
                        webView.loadUrl(targetUrl)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )

            if (state.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(palette.card),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.ic_shield),
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        colorFilter = ColorFilter.tint(palette.keep),
                    )
                    Spacer(Modifier.height(14.dp))
                    Heading("Privacy policy", size = 22.sp)
                    Spacer(Modifier.height(7.dp))
                    Kicker("Loading secure page", color = palette.textMuted)
                }
            }
        }
    }
}

fun String?.safe() = this ?: ""
