package com.systematics.keepr.presentation.screens.privacy_policy

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systematics.keepr.R
import com.systematics.keepr.presentation.core.utils.LoadingState
import com.systematics.keepr.presentation.core.utils.ReusableTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun PrivacyPolicyScreen(
    onBackPress: () -> Unit,
    viewModel: PrivacyPolicyViewModel = koinViewModel(),
) {
    BackHandler { onBackPress() }

    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = {
        ReusableTopBar(
            titleRes = R.string.privacy_policy,
            onBackClick = onBackPress
        )
    }) { paddingValues ->
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondary)
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LaunchedEffect(Unit) {
                viewModel.isLoading(true)
            }
            AndroidView(factory = {
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            viewModel.isLoading(false)
                        }
                    }
                }
            }, update = { webView ->
                webView.loadUrl(state.privacyPolicyLink.safe())
            }, modifier = Modifier.fillMaxSize())

            if (state.isLoading) {
                LoadingState(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

fun String?.safe() = this ?: ""