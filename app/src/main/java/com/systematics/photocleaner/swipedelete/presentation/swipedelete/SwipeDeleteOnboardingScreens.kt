package com.systematics.photocleaner.swipedelete.presentation.swipedelete

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.systematics.photocleaner.swipedelete.R
import com.systematics.photocleaner.swipedelete.data.swipedelete.SwipeDeleteController
import com.systematics.photocleaner.swipedelete.domain.usecase.IsAdsEnabledUseCase
import com.systematics.photocleaner.swipedelete.domain.usecase.SaveSelectedLanguageUseCase
import com.systematics.photocleaner.swipedelete.utils.core.AppLocaleManager
import com.systematics.photocleaner.swipedelete.utils.monetization.config.frontend.config.AdsPlacement
import com.systematics.photocleaner.swipedelete.utils.providers.LocalAppLogEvents
import com.systematics.monetization.ui.compose.InlineAdShowComponent
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private data class LanguageChoice(val code: String, val label: String, val native: String)
private val SwipeDeleteLanguages = listOf(
    LanguageChoice("en","English","English"), LanguageChoice("es","Spanish","Español"),
    LanguageChoice("fr","French","Français"), LanguageChoice("de","German","Deutsch"),
    LanguageChoice("pt","Portuguese","Português"), LanguageChoice("it","Italian","Italiano"),
    LanguageChoice("ja","Japanese","日本語"), LanguageChoice("ko","Korean","한국어"),
    LanguageChoice("hi","Hindi","हिन्दी"), LanguageChoice("ar","Arabic","العربية"),
)

@Composable
fun AnalyticsLaunch(name: String) {
    val events = LocalAppLogEvents.current
    LaunchedEffect(name) { events.loadEvents(name) }
}

@Composable
fun SwipeDeleteLanguageScreen(
    isFromSplash: Boolean,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    saveLanguage: SaveSelectedLanguageUseCase = koinInject(),
    isAdsEnabled: IsAdsEnabledUseCase = koinInject(),
) {
    AnalyticsLaunch("lang_scr_launch")
    val p = LocalSwipeDeletePalette.current
    var selected by rememberSaveable { mutableStateOf("en") }
    val events = LocalAppLogEvents.current
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(18.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!isFromSplash) KIconButton(painterResource(R.drawable.ic_back), "Back", {
                events.loadEvents("lang_scr_back_clck"); onBack()
            })
            Column(Modifier.padding(start = if (isFromSplash) 0.dp else 12.dp)) {
                Kicker("Welcome to Photo Cleaner", color = p.keep)
                Heading("Choose your language")
            }
        }
        SwipeDeleteText(
            "Photo Cleaner follows your choice throughout the app. Unsupported device languages use English.",
            Modifier.padding(horizontal = 18.dp, vertical = 4.dp), color = p.textMuted, fontSize = 13.sp
        )
        LazyColumn(
            Modifier.weight(1f).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(SwipeDeleteLanguages, key = { it.code }) { item ->
                val active = selected == item.code
                HardSurface(
                    Modifier.fillMaxWidth().height(70.dp).clickable { selected = item.code },
                    radius = SwipeDeleteDimens.radiusMd,
                    background = if (active) p.cardRaised else p.card,
                    borderColor = if (active) p.keep else p.border,
                    shadowX = if (active) 4.dp else 0.dp, shadowY = if (active) 4.dp else 0.dp,
                    borderWidth = if (active) 3.dp else 2.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            SwipeDeleteText(item.native, color = p.textStrong, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Kicker(item.label)
                        }
                        if (active) Badge("✓", tone = p.keep)
                    }
                }
            }
        }
        Column(Modifier.fillMaxWidth().background(p.appRaised).padding(18.dp, 10.dp)) {
            KButton("Continue", {
                events.loadEvents("lang_scr_continue_clck")
                val choice = SwipeDeleteLanguages.first { it.code == selected }
                saveLanguage(choice.code, choice.label)
                AppLocaleManager.applyLocale(choice.code)
                onContinue()
            }, Modifier.fillMaxWidth())
        }
        if (isAdsEnabled()) InlineAdShowComponent(modifier = Modifier.fillMaxWidth(), placementKey = AdsPlacement.Inlines.LANGUAGE_BOTTOM)
    }
}

private data class IntroPage(val kicker: String, val title: String, val body: String, val icon: Int, val color: (SwipeDeletePalette) -> Color)
private val IntroPages = listOf(
    IntroPage("One month at a time", "Turn the backlog into a small win", "Pick any calendar month. Every accessible month stays available, however many photos it contains.", R.drawable.ic_photo) { it.reward },
    IntroPage("A tactile decision", "Right keeps · left deletes", "Move the card with your thumb, or use the visible Keep and Delete buttons. Undo is always available before deletion.", R.drawable.ic_heart) { it.keep },
    IntroPage("Safe by design", "Review before anything leaves", "Inspect every choice, then confirm with Android. Photos stay on this device and Photo Cleaner verifies each result.", R.drawable.ic_shield) { it.gone },
)

@Composable
fun SwipeDeleteOnboardingScreen(
    onContinue: () -> Unit,
    onPrivacy: () -> Unit,
    isAdsEnabled: IsAdsEnabledUseCase = koinInject(),
) {
    AnalyticsLaunch("intro_scr_launch")
    val p = LocalSwipeDeletePalette.current
    val pager = rememberPagerState { IntroPages.size }
    val scope = rememberCoroutineScope()
    val events = LocalAppLogEvents.current
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(18.dp, 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            SwipeDeleteWord()
            Kicker("${pager.currentPage + 1} / ${IntroPages.size}", color = p.keep)
        }
        HorizontalPager(pager, Modifier.weight(1f)) { page ->
            val item = IntroPages[page]
            Column(Modifier.fillMaxSize().padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                HardSurface(Modifier.size(140.dp), radius = SwipeDeleteDimens.radius2Xl, background = p.card, shadowX = 8.dp, shadowY = 10.dp) {
                    androidx.compose.foundation.Image(painterResource(item.icon), null, Modifier.align(Alignment.Center).size(68.dp), colorFilter = ColorFilter.tint(item.color(p)))
                }
                Spacer(Modifier.height(36.dp))
                Kicker(item.kicker, color = item.color(p))
                Spacer(Modifier.height(8.dp))
                Heading(item.title, Modifier.fillMaxWidth(), 31.sp, TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                SwipeDeleteText(item.body, Modifier.fillMaxWidth(), color = p.textMuted, fontSize = 15.sp, textAlign = TextAlign.Center)
            }
        }
        Row(Modifier.align(Alignment.CenterHorizontally).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(IntroPages.size) { i ->
                Box(Modifier.size(if (i == pager.currentPage) 28.dp else 9.dp, 9.dp).clip(RoundedCornerShape(99.dp)).background(if (i == pager.currentPage) p.keep else p.borderSoft))
            }
        }
        Column(Modifier.fillMaxWidth().background(p.appRaised).padding(18.dp, 10.dp)) {
            KButton(if (pager.currentPage == IntroPages.lastIndex) "Allow photo access" else "Continue", {
                events.loadEvents("intro_scr_continue_clck")
                if (pager.currentPage == IntroPages.lastIndex) onContinue() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
            }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(5.dp))
            Box(Modifier.fillMaxWidth().clickable { events.loadEvents("intro_scr_privacy_clck"); onPrivacy() }.padding(8.dp), contentAlignment = Alignment.Center) {
                SwipeDeleteText("Privacy details", color = p.textMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (isAdsEnabled()) InlineAdShowComponent(modifier = Modifier.fillMaxWidth(), placementKey = AdsPlacement.Inlines.ONBOARDING_BOTTOM)
    }
}

@Composable
fun MediaAccessScreen(
    onGranted: () -> Unit,
    onSelected: () -> Unit,
    onDenied: (Boolean) -> Unit,
    onNotNow: () -> Unit,
    controller: SwipeDeleteController = koinInject(),
) {
    AnalyticsLaunch("access_scr_launch")
    val p = LocalSwipeDeletePalette.current
    val activity = LocalActivity.current
    var asked by rememberSaveable { mutableStateOf(false) }
    val events = LocalAppLogEvents.current
    val permissions = remember {
        when {
            Build.VERSION.SDK_INT >= 34 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            Build.VERSION.SDK_INT <= 28 -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        when {
            controller.hasFullAccess() -> onGranted()
            controller.hasPartialAccess() -> onSelected()
            else -> {
                val permanent = asked && activity != null && permissions.none { activity.shouldShowRequestPermissionRationale(it) }
                onDenied(permanent)
            }
        }
    }
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(.5f))
        HardSurface(Modifier.size(126.dp), radius = SwipeDeleteDimens.radius2Xl, shadowX = 8.dp, shadowY = 10.dp) {
            androidx.compose.foundation.Image(painterResource(R.drawable.ic_photo), null, Modifier.align(Alignment.Center).size(60.dp), colorFilter = ColorFilter.tint(p.keep))
        }
        Spacer(Modifier.height(30.dp))
        Kicker("Photo access", color = p.keep)
        Spacer(Modifier.height(7.dp))
        Heading(if (controller.hasPartialAccess()) "You choose what Photo Cleaner sees" else "Your camera roll stays yours", Modifier.fillMaxWidth(), 30.sp, TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        SwipeDeleteText(
            "Photo Cleaner reads only the photos Android allows so you can review them month by month. Photo content is never uploaded. Deletion always uses Android confirmation.",
            Modifier.fillMaxWidth(), color = p.textMuted, fontSize = 15.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        HardSurface(Modifier.fillMaxWidth().height(74.dp), radius = SwipeDeleteDimens.radiusMd, background = p.cardRaised, shadowX = 0.dp, shadowY = 0.dp, borderWidth = 2.dp, contentPadding = PaddingValues(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(painterResource(R.drawable.ic_shield), null, Modifier.size(28.dp), colorFilter = ColorFilter.tint(p.win))
                Spacer(Modifier.width(12.dp))
                SwipeDeleteText("On-device processing · no account · no photo upload", color = p.textBody, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.weight(1f))
        KButton(if (controller.hasPartialAccess()) "Choose more photos" else "Allow photo access", {
            events.loadEvents("access_scr_allow_clck"); asked = true; launcher.launch(permissions)
        }, Modifier.fillMaxWidth())
        if (Build.VERSION.SDK_INT >= 34) {
            Spacer(Modifier.height(9.dp))
            KButton("Choose selected photos", { events.loadEvents("access_scr_select_clck"); asked = true; launcher.launch(permissions) }, Modifier.fillMaxWidth(), KButtonStyle.Neutral)
        }
        Spacer(Modifier.height(6.dp))
        KButton("Not now", { events.loadEvents("access_scr_later_clck"); onNotNow() }, Modifier.fillMaxWidth(), KButtonStyle.Ghost)
    }
}
