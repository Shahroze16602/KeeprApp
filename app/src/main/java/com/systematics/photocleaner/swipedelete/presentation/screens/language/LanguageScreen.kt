package com.systematics.photocleaner.swipedelete.presentation.screens.language

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systematics.monetization.ui.MonetizationInstall
import com.systematics.monetization.ui.compose.InlineAdShowComponent
import com.systematics.monetization.ui.compose.utils.monetizationInject
import com.systematics.photocleaner.swipedelete.R
import com.systematics.photocleaner.swipedelete.utils.core.AppLocaleManager
import com.systematics.photocleaner.swipedelete.utils.core.MyColors
import com.systematics.photocleaner.swipedelete.domain.repository.SessionGate
import com.systematics.photocleaner.swipedelete.domain.usecase.IsSessionGateAllowedUseCase
import com.systematics.photocleaner.swipedelete.domain.usecase.IsAdsEnabledUseCase
import com.systematics.photocleaner.swipedelete.utils.monetization.config.frontend.breakpoints.AdBreakPoint
import com.systematics.photocleaner.swipedelete.utils.monetization.config.frontend.config.AdsPlacement
import com.systematics.photocleaner.swipedelete.presentation.core.utils.ReusableText
import com.systematics.photocleaner.swipedelete.presentation.core.utils.ReusableTopBar
import com.systematics.photocleaner.swipedelete.presentation.core.utils.noRippleClickable
import com.systematics.photocleaner.swipedelete.presentation.screens.language.components.LanguageListItem
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun LanguageScreen(
    isFromSplash: Boolean,
    onMoveNext: () -> Unit,
    viewModel: LanguageViewModel = koinViewModel(),
    isSessionGateAllowed: IsSessionGateAllowedUseCase = koinInject(),
    isAdsEnabled: IsAdsEnabledUseCase = koinInject(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val languages = state.filteredLanguages()
    val adsEnabled = isAdsEnabled()
    val monetizationInstall: MonetizationInstall? =
        if (adsEnabled) monetizationInject() else null

    val applyAndMoveNext: () -> Unit = {
        val selectedLanguageCode = viewModel.persistSelection()
        AppLocaleManager.applyLocale(selectedLanguageCode)
        onMoveNext()
    }

    var preloaded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100.milliseconds)
        if (adsEnabled && !preloaded) {
            if (isSessionGateAllowed(SessionGate.ONBOARDING)) {
                monetizationInstall?.executeBreakPoint(AdBreakPoint.BP_ONBOARDING_PENDING)
            } else {
                monetizationInstall?.executeBreakPoint(AdBreakPoint.BP_HOME_PENDING)
            }
            preloaded = true
        }
    }

    BackHandler(onBack = onMoveNext)

    Scaffold(
        containerColor = MyColors.BackgroundColor,
        topBar = {
            ReusableTopBar(
                titleRes = R.string.select_language,
                isBackPressEnable = !isFromSplash,
                onBackClick = onMoveNext,
                content = {
                    ReusableText(
                        modifier = Modifier.noRippleClickable { applyAndMoveNext() },
                        text = stringResource(R.string.done),
                        fontSize = 16,
                        fontWeight = FontWeight.SemiBold,
                        color = MyColors.AccentColor
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MyColors.BackgroundColor)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = state.searchText,
                onValueChange = viewModel::onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                placeholder = {
                    Text(stringResource(R.string.search_any_language))
                },
                singleLine = true,
                keyboardActions = KeyboardActions(onSearch = {}),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )

            if (languages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ReusableText(
                            text = stringResource(R.string.no_language_found),
                            fontSize = 20,
                            fontWeight = FontWeight.Bold,
                            color = MyColors.BlackColor
                        )
                        ReusableText(
                            modifier = Modifier.padding(top = 8.dp),
                            text = stringResource(R.string.try_different_keyword),
                            fontSize = 14,
                            color = MyColors.TextSecondaryColor
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = languages,
                        key = { it.languageAbbr }
                    ) { language ->
                        LanguageListItem(
                            model = language,
                            isSelected = language.languageAbbr == state.selectedLanguage,
                            onClick = {
                                viewModel.onLanguageSelected(language)
                            }
                        )
                    }
                }
            }

            if (adsEnabled) {
                InlineAdShowComponent(
                    modifier = Modifier.fillMaxWidth(),
                    placementKey = AdsPlacement.Inlines.LANGUAGE_BOTTOM
                )
            }
        }
    }
}
