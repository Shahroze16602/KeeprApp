package com.systematics.photocleaner.swipedelete.presentation.swipedelete

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.SystemClock
import android.util.LruCache
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systematics.photocleaner.swipedelete.R
import com.systematics.photocleaner.swipedelete.data.swipedelete.*
import com.systematics.photocleaner.swipedelete.domain.usecase.IsAdsEnabledUseCase
import com.systematics.photocleaner.swipedelete.domain.swipedelete.SwipeDecisionEngine
import com.systematics.photocleaner.swipedelete.domain.swipedelete.SwipeOutcome
import com.systematics.photocleaner.swipedelete.domain.swipedelete.GamificationRules
import com.systematics.photocleaner.swipedelete.domain.usecase.MarkFirstSessionCompletedUseCase
import com.systematics.photocleaner.swipedelete.utils.monetization.config.frontend.breakpoints.AdBreakPoint
import com.systematics.photocleaner.swipedelete.utils.monetization.config.frontend.config.AdsPlacement
import com.systematics.photocleaner.swipedelete.utils.providers.LocalAppLogEvents
import com.systematics.monetization.ui.MonetizationInstall
import com.systematics.monetization.ui.compose.InlineAdShowComponent
import com.systematics.monetization.ui.compose.utils.monetizationInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

private val mediaBitmapCache = object : LruCache<String, ImageBitmap>(32 * 1024) {
    override fun sizeOf(key: String, value: ImageBitmap): Int =
        ((value.width.toLong() * value.height.toLong() * 4L) / 1024L)
            .coerceIn(1L, Int.MAX_VALUE.toLong())
            .toInt()
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Long.bytesLabel(): String = when {
    this >= 1024L * 1024 * 1024 -> "%.2f GB".format(this / (1024.0 * 1024 * 1024))
    this >= 1024L * 1024 -> "%.1f MB".format(this / (1024.0 * 1024))
    else -> "$this B"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthPickerScreen(
    onSettings: () -> Unit,
    onPremium: () -> Unit,
    showPremium: Boolean,
    onStart: (String, String, Boolean) -> Unit,
    onResume: (String, String) -> Unit,
    onPermission: () -> Unit,
    onSelected: () -> Unit,
    controller: SwipeDeleteController = koinInject(),
    isAdsEnabled: IsAdsEnabledUseCase = koinInject(),
    markFirstSessionCompleted: MarkFirstSessionCompletedUseCase = koinInject(),
) {
    AnalyticsLaunch("months_scr_launch")
    val state by controller.state.collectAsStateWithLifecycle()
    val p = LocalSwipeDeletePalette.current
    val events = LocalAppLogEvents.current
    val activity = LocalContext.current.findActivity()
    var showExitSheet by rememberSaveable { mutableStateOf(false) }
    val exitSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val adsEnabled = isAdsEnabled()
    val hasPhotoAccess = controller.hasFullAccess() || controller.hasPartialAccess()
    val monetizationInstall: MonetizationInstall? =
        if (adsEnabled) monetizationInject() else null
    LaunchedEffect(Unit) {
        markFirstSessionCompleted()
        if (adsEnabled) monetizationInstall?.executeBreakPoint(AdBreakPoint.BP_HOME_APPEAR)
        controller.refreshCatalog()
    }
    BackHandler(enabled = !showExitSheet) { showExitSheet = true }
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(18.dp, 8.dp, 18.dp, 12.dp), verticalAlignment = Alignment.CenterVertically) {
            SwipeDeleteWord(Modifier.weight(1f), 29.sp)
            if (showPremium) {
                KIconButton(painterResource(R.drawable.ic_premium), "Remove ads", {
                    events.loadEvents("months_scr_prem_clck"); onPremium()
                }, Modifier.padding(end = 10.dp), tint = p.reward)
            }
            KIconButton(painterResource(R.drawable.ic_settings), "Settings", {
                events.loadEvents("months_scr_settings_clck"); onSettings()
            })
        }
        LazyColumn(
            Modifier.weight(1f).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 18.dp)
        ) {
            item {
                LevelCard(
                    level = GamificationRules.level(state.xp),
                    progress = (state.xp % 1000) / 1000f,
                    reclaimed = state.reclaimedBytes.bytesLabel(),
                    streak = state.streak,
                    months = state.monthsCleared,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (controller.hasPartialAccess() && !controller.hasFullAccess()) {
                item {
                    HardSurface(
                        Modifier.fillMaxWidth().height(72.dp).clickable { onSelected() },
                        radius = SwipeDeleteDimens.radiusMd, background = p.cardRaised, borderColor = p.gone,
                        shadowX = 0.dp, shadowY = 0.dp, borderWidth = 2.dp, contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.foundation.Image(painterResource(R.drawable.ic_photo), null, Modifier.size(26.dp), colorFilter = ColorFilter.tint(p.gone))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Kicker("Selected photos", color = p.gone)
                                SwipeDeleteText("${state.selectedMedia.size} accessible · separate cleanup", color = p.textBody, fontWeight = FontWeight.Bold)
                            }
                            SwipeDeleteText("›", color = p.textFaint, fontSize = 24.sp)
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Kicker("Pick a month to clean")
                    Badge("${state.months.sumOf { it.total - it.decided }} to sort", tone = p.cardRaised)
                }
            }
            when (state.catalogStatus) {
                CatalogStatus.Loading, CatalogStatus.Idle -> items(4) {
                    HardSurface(Modifier.fillMaxWidth().height(100.dp), radius = SwipeDeleteDimens.radiusLg, background = p.card) {
                        Box(Modifier.fillMaxSize().padding(16.dp)) {
                            Box(Modifier.size(66.dp).clip(RoundedCornerShape(99.dp)).background(p.inset))
                            Column(Modifier.align(Alignment.CenterStart).padding(start = 82.dp)) {
                                Box(Modifier.size(140.dp, 20.dp).clip(CircleShape).background(p.inset))
                                Spacer(Modifier.height(10.dp))
                                Box(Modifier.size(190.dp, 12.dp).clip(CircleShape).background(p.inset))
                            }
                        }
                    }
                }
                CatalogStatus.PermissionRequired -> item {
                    EmptyPanel("Photo access needed", "Your saved progress is safe. Allow Android photo access to build the month catalog.", "Review access", onPermission, R.drawable.ic_lock)
                }
                CatalogStatus.Empty -> item {
                    EmptyPanel("Nothing to sort", "Photo Cleaner couldn't find accessible photos. Refresh or change which photos Android allows.", "Review access", onPermission, R.drawable.ic_photo)
                }
                CatalogStatus.Error -> item {
                    EmptyPanel("Scan paused", state.error ?: "Photo Cleaner couldn't read the camera roll.", "Try again", controller::refreshCatalog, R.drawable.ic_warning)
                }
                CatalogStatus.Ready -> items(state.months, key = { it.key }) { month ->
                    val title = "${month.label} ${month.year}"
                    PileTile(
                        title = month.label,
                        subtitle = when {
                            month.complete -> "Cleared"
                            month.decided > 0 -> "${month.decided} / ${month.total} · tap to resume"
                            else -> "${month.total} photos · not started"
                        },
                        progress = if (month.complete) 1f else if (month.total == 0) 0f else month.decided.toFloat() / month.total,
                        onClick = {
                            events.loadEvents("months_scr_month_clck")
                            if (month.decided > 0 && !month.complete) onResume(month.key, title) else onStart(month.key, title, false)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        if (adsEnabled) InlineAdShowComponent(modifier = Modifier.fillMaxWidth(), placementKey = AdsPlacement.Inlines.HOME_BOTTOM)
    }
    if (showExitSheet) {
        ModalBottomSheet(
            onDismissRequest = { showExitSheet = false },
            sheetState = exitSheetState,
            containerColor = p.card,
            contentColor = p.textBody,
            scrimColor = Color.Black.copy(alpha = .7f),
            shape = RoundedCornerShape(topStart = SwipeDeleteDimens.radiusXl, topEnd = SwipeDeleteDimens.radiusXl),
        ) {
            Column(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Heading("Exit Photo Cleaner?", size = 22.sp)
                Spacer(Modifier.height(8.dp))
                SwipeDeleteText("Are you sure you want to exit?", color = p.textMuted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KButton("Cancel", { showExitSheet = false }, Modifier.weight(1f), KButtonStyle.Neutral)
                    KButton("Exit", { showExitSheet = false; activity?.finish() }, Modifier.weight(1f), KButtonStyle.Gone)
                }
            }
        }
    }
}

@Composable
private fun EmptyPanel(title: String, body: String, action: String, onClick: () -> Unit, icon: Int) {
    val p = LocalSwipeDeletePalette.current
    HardSurface(Modifier.fillMaxWidth().heightIn(min = 210.dp), radius = SwipeDeleteDimens.radiusLg, contentPadding = PaddingValues(22.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.foundation.Image(painterResource(icon), null, Modifier.size(44.dp), colorFilter = ColorFilter.tint(p.keep))
            Spacer(Modifier.height(12.dp)); Heading(title, size = 22.sp)
            Spacer(Modifier.height(8.dp)); SwipeDeleteText(body, color = p.textMuted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp)); KButton(action, onClick, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun SelectedPhotosScreen(onBack: () -> Unit, onAccess: () -> Unit, onStart: () -> Unit, controller: SwipeDeleteController = koinInject()) {
    AnalyticsLaunch("selected_scr_launch")
    val state by controller.state.collectAsStateWithLifecycle()
    val p = LocalSwipeDeletePalette.current
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding()) {
        SwipeDeleteHeader("Selected photos", "Partial access", onBack, p.gone)
        HardSurface(Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 18.dp), radius = SwipeDeleteDimens.radiusMd,
            background = p.cardRaised, borderColor = p.gone, shadowX = 0.dp, shadowY = 0.dp, borderWidth = 2.dp,
            contentPadding = PaddingValues(12.dp)) {
            SwipeDeleteText("These Android-selected photos are a separate cleanup from calendar months.", color = p.textBody, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        LazyVerticalGrid(
            GridCells.Fixed(3), Modifier.weight(1f).padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.selectedMedia, key = { it.uri }) { media ->
                AsyncMediaImage(media, Modifier.aspectRatio(1f).clip(RoundedCornerShape(SwipeDeleteDimens.radiusSm)).border(2.dp, p.border, RoundedCornerShape(SwipeDeleteDimens.radiusSm)))
            }
        }
        Kicker("${state.selectedMedia.size} photos selected", Modifier.align(Alignment.CenterHorizontally).padding(8.dp))
        Column(Modifier.fillMaxWidth().background(p.appRaised).padding(18.dp, 10.dp)) {
            KButton("Start selected cleanup", onStart, Modifier.fillMaxWidth(), enabled = state.selectedMedia.isNotEmpty())
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KButton("Reselect", onAccess, Modifier.weight(1f), KButtonStyle.Neutral)
                KButton("Allow all", onAccess, Modifier.weight(1f), KButtonStyle.Ghost)
            }
        }
    }
}

@Composable
fun CleanupSessionScreen(
    onExit: () -> Unit,
    onReview: () -> Unit,
    onRecovery: () -> Unit,
    expectedScopeKey: String? = null,
    controller: SwipeDeleteController = koinInject(),
) {
    AnalyticsLaunch("clean_scr_launch")
    val state by controller.state.collectAsStateWithLifecycle()
    val session = state.session?.takeIf { expectedScopeKey == null || it.scopeKey == expectedScopeKey }
    val p = LocalSwipeDeletePalette.current
    val events = LocalAppLogEvents.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val useHaptics = LocalSwipeDeleteHaptics.current
    val offset = remember { Animatable(0f) }
    var cardWidth by remember { mutableFloatStateOf(1f) }
    var dragStart by remember { mutableLongStateOf(0L) }
    var locallyCommittedUri by remember(session?.id) { mutableStateOf<String?>(null) }
    var commitInFlight by remember(session?.id) { mutableStateOf(false) }
    val pending = remember(session?.media, session?.decisions, locallyCommittedUri) {
        session?.let { activeSession ->
            val decidedUris = activeSession.decisions.mapTo(HashSet()) { it.media.uri }
            activeSession.media.filter { it.uri !in decidedUris && it.uri != locallyCommittedUri }
        }.orEmpty()
    }
    val current = pending.firstOrNull()
    var loadError by remember(current?.uri) { mutableStateOf(false) }
    var loadAttempt by remember(current?.uri) { mutableIntStateOf(0) }
    val nextMedia = pending.getOrNull(1)

    fun commit(decision: MediaDecision) {
        val committedUri = current?.uri ?: return
        if (loadError || commitInFlight) return
        commitInFlight = true
        scope.launch {
            try {
                if (useHaptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                // Promote and unlock the next card immediately. decide() updates the
                // in-memory session synchronously and queues the durable DB write on IO.
                locallyCommittedUri = committedUri
                offset.snapTo(0f)
                commitInFlight = false

                controller.decide(decision)
                events.loadEvents(if (decision == MediaDecision.Keep) "clean_scr_keep_clck" else "clean_scr_delete_clck")
            } finally {
                offset.snapTo(0f)
                commitInFlight = false
            }
        }
    }

    LaunchedEffect(session?.id, session?.current?.uri, locallyCommittedUri) {
        val committedUri = locallyCommittedUri ?: return@LaunchedEffect
        if (session?.current?.uri != committedUri) {
            locallyCommittedUri = null
        }
    }

    LaunchedEffect(session?.current?.uri, session?.media?.size, session?.decisions?.size) {
        if (session != null && session.current == null && session.media.isNotEmpty()) {
            lifecycleOwner.lifecycle.currentStateFlow.first { it == Lifecycle.State.RESUMED }
            onReview()
        }
    }
    BackHandler {
        events.loadEvents("clean_scr_back_clck"); controller.clearCombo(); onExit()
    }
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(16.dp, 10.dp, 16.dp, 5.dp), verticalAlignment = Alignment.CenterVertically) {
            KIconButton(painterResource(R.drawable.ic_back), "Save and exit", { events.loadEvents("clean_scr_back_clck"); controller.clearCombo(); onExit() }, size = 44.dp)
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Heading(session?.title ?: "Loading…", size = 18.sp)
                Kicker(if (session == null) "Preparing photos" else "Photo ${(session.decisions.size + 1).coerceAtMost(session.media.size)} of ${session.media.size}")
            }
            KIconButton(painterResource(R.drawable.ic_review), "Review decisions", { events.loadEvents("clean_scr_review_clck"); onReview() }, size = 44.dp, enabled = session != null)
        }
        val progress = session?.progress ?: 0f
        Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 5.dp).height(10.dp).clip(CircleShape).background(p.inset).border(2.dp,p.border,CircleShape)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(progress).background(Brush.horizontalGradient(listOf(p.keepLight,p.keep))))
        }
        Row(Modifier.align(Alignment.CenterHorizontally).padding(7.dp), verticalAlignment = Alignment.CenterVertically) {
            SwipeDeleteText((session?.delete?.sumOf { it.media.sizeBytes ?: 0L } ?: 0L).bytesLabel(), color = p.reward, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(7.dp)); Kicker("queued to free")
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(20.dp, 2.dp, 20.dp, 0.dp)) {
            cardWidth = constraints.maxWidth.toFloat()
            if (current == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Kicker(if (session == null) "Loading your month…" else "Opening review…") }
            } else if (loadError) {
                HardSurface(Modifier.fillMaxSize(), radius = SwipeDeleteDimens.radiusXl, contentPadding = PaddingValues(28.dp)) {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        androidx.compose.foundation.Image(painterResource(R.drawable.ic_warning), null, Modifier.size(58.dp), colorFilter = ColorFilter.tint(p.gone))
                        Spacer(Modifier.height(14.dp)); Heading("Couldn't load this photo", size = 22.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp)); SwipeDeleteText("No keep or delete was recorded. Retry or skip it for now.", color=p.textMuted,textAlign=TextAlign.Center)
                        Spacer(Modifier.height(18.dp)); KButton("Retry", {
                            loadError = false
                            loadAttempt++
                        }, Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp)); KButton("More details", onRecovery, Modifier.fillMaxWidth(), KButtonStyle.Neutral)
                    }
                }
            } else {
                val next = nextMedia
                val stackedOffset = with(density) { 12.dp.toPx() }
                val visualOffset = if (locallyCommittedUri == null) offset.value else 0f
                if (next != null) {
                    PhotoCard(
                        Modifier.fillMaxSize().zIndex(0f).graphicsLayer {
                            val reveal = (abs(visualOffset) / cardWidth.coerceAtLeast(1f)).coerceIn(0f, 1f)
                            val scale = .94f + (.06f * reveal)
                            scaleX = scale
                            scaleY = scale
                            translationY = stackedOffset * (1f - reveal)
                            alpha = .82f + (.18f * reveal)
                        }
                    ) {
                        Box(Modifier.fillMaxSize().background(Color.Black))
                        AsyncMediaImage(next, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    }
                }
                AnimatedComboCounter(
                    combo = state.combo,
                    modifier = Modifier.align(Alignment.TopEnd).zIndex(5f).padding(top = 24.dp, end = 24.dp),
                )
                PhotoCard(
                    Modifier.fillMaxSize().zIndex(1f).onSizeChanged { cardWidth = it.width.toFloat() }
                        .graphicsLayer { translationX = visualOffset; rotationZ = (visualOffset / cardWidth) * 12f }
                        .pointerInput(current.uri, commitInFlight, loadError) {
                            if (commitInFlight || loadError) return@pointerInput
                            detectDragGestures(
                                onDragStart = { dragStart = SystemClock.elapsedRealtime(); scope.launch { offset.stop() } },
                                onDragCancel = { scope.launch { offset.animateTo(0f, spring()) } },
                                onDragEnd = {
                                    val elapsed = (SystemClock.elapsedRealtime() - dragStart).coerceAtLeast(1)
                                    val velocity = abs(offset.value) / elapsed * 1000
                                    if (abs(offset.value) >= cardWidth * .28f || velocity >= 1250f) commit(if (offset.value > 0) MediaDecision.Keep else MediaDecision.Delete)
                                    else scope.launch { offset.animateTo(0f, spring(dampingRatio=.7f, stiffness=500f)) }
                                }
                            ) { change, amount -> change.consume(); scope.launch { offset.snapTo(offset.value + amount.x) } }
                        }
                ) {
                    Box(Modifier.fillMaxSize().background(Color.Black))
                    AsyncMediaImage(
                        media = current,
                        modifier = Modifier.fillMaxSize(),
                        onError = { loadError = true },
                        contentScale = ContentScale.Fit,
                        reloadKey = loadAttempt,
                    )
                    Box(Modifier.fillMaxSize().background(
                        when { visualOffset > 4 -> p.keep.copy(alpha=(abs(visualOffset)/cardWidth).coerceAtMost(.28f))
                            visualOffset < -4 -> p.gone.copy(alpha=(abs(visualOffset)/cardWidth).coerceAtMost(.28f))
                            else -> Color.Transparent }
                    ))
                    if (abs(visualOffset) > 16) Stamp(if (visualOffset > 0) "KEEP" else "DELETE", Modifier.align(if(visualOffset>0) Alignment.TopStart else Alignment.TopEnd).padding(22.dp), if(visualOffset>0)p.keep else p.gone)
                    HardSurface(Modifier.fillMaxWidth().height(60.dp).align(Alignment.BottomCenter).padding(8.dp), radius=SwipeDeleteDimens.radiusMd,
                        shadowX=0.dp,shadowY=0.dp,borderWidth=2.dp,background=p.card.copy(alpha=.92f),contentPadding=PaddingValues(10.dp)) {
                        Row(Modifier.fillMaxSize(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
                            Kicker(Instant.ofEpochMilli(current.takenAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM d, yyyy")))
                            SwipeDeleteText(current.sizeBytes?.bytesLabel() ?: "Size unknown",color=p.textStrong,fontWeight=FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(20.dp, 14.dp, 20.dp, 20.dp), horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            KIconButton(painterResource(R.drawable.ic_undo), "Undo last decision", { events.loadEvents("clean_scr_undo_clck"); controller.undo() }, size=54.dp, enabled=session?.decisions?.isNotEmpty()==true&&!commitInFlight)
            KIconButton(painterResource(R.drawable.ic_trash), "Delete photo", { commit(MediaDecision.Delete) }, size=72.dp, tint=Color(0xFF04121A), background=p.gone, enabled=current!=null&&!loadError&&!commitInFlight)
            KIconButton(painterResource(R.drawable.ic_heart), "Keep photo", { commit(MediaDecision.Keep) }, size=72.dp, tint=Color(0xFF14110F), background=p.keep, enabled=current!=null&&!loadError&&!commitInFlight)
        }
    }
}

@Composable
private fun AnimatedComboCounter(combo: Int, modifier: Modifier = Modifier) {
    val p = LocalSwipeDeletePalette.current
    val haptic = LocalHapticFeedback.current
    val useHaptics = LocalSwipeDeleteHaptics.current
    val reducedMotion = LocalSwipeDeleteReducedMotion.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val pulse = remember { Animatable(1f) }
    val vibration = remember { Animatable(0f) }
    var previousCombo by remember { mutableIntStateOf(combo) }
    val growth = (combo - 2).coerceIn(0, 8)
    val baseScale = 1.12f + growth * .0375f
    val heat = growth / 8f
    val background by animateColorAsState(
        targetValue = lerp(p.reward, Color(0xFFFF3B30), heat),
        animationSpec = tween(240),
        label = "comboHeat",
    )

    LaunchedEffect(combo) {
        val increased = combo > previousCombo
        previousCombo = combo
        if (!increased || combo < 2) return@LaunchedEffect

        val vibrationDistance = with(density) { (1.5f + growth * .45f).dp.toPx() }
        val vibrationCount = 3 + growth / 4
        val vibrationDuration = (38 - growth).coerceAtLeast(28)
        val popScale = 1.24f + growth * .018f
        val hapticPulses = (1 + growth / 3).coerceAtMost(3)
        coroutineScope {
            if (useHaptics) launch {
                repeat(hapticPulses) { index ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (index < hapticPulses - 1) delay(48)
                }
            }
            if (!reducedMotion) {
                launch {
                    pulse.snapTo(.9f)
                    pulse.animateTo(popScale, spring(dampingRatio = .38f, stiffness = 680f))
                    pulse.animateTo(1f, spring(dampingRatio = .5f, stiffness = 520f))
                }
                launch {
                    vibration.snapTo(0f)
                    repeat(vibrationCount) { index ->
                        vibration.animateTo(
                            if (index % 2 == 0) vibrationDistance else -vibrationDistance,
                            tween(vibrationDuration),
                        )
                    }
                    vibration.animateTo(0f, tween(55))
                }
            }
        }
    }

    if (combo >= 2) {
        Box(
            modifier.graphicsLayer {
                scaleX = baseScale * pulse.value
                scaleY = baseScale * pulse.value
                translationX = vibration.value
                rotationZ = if (layoutDirection == LayoutDirection.Ltr) 12f else -12f
            },
        ) {
            ComboCounter(combo, tone = background)
        }
    }
}

@Composable
fun MediaRecoveryScreen(onBack: () -> Unit, onRetry: () -> Unit, onSkip: () -> Unit) {
    AnalyticsLaunch("recovery_scr_launch")
    val p=LocalSwipeDeletePalette.current
    StateScreen(R.drawable.ic_warning,"Photo unavailable","No decision was recorded","This item may be missing, unreadable, or no longer allowed. Your existing decisions remain saved.", p.gone) {
        KButton("Retry",onRetry,Modifier.fillMaxWidth()); Spacer(Modifier.height(9.dp))
        KButton("Skip for now",onSkip,Modifier.fillMaxWidth(),KButtonStyle.Neutral); Spacer(Modifier.height(6.dp))
        KButton("Return",onBack,Modifier.fillMaxWidth(),KButtonStyle.Ghost)
    }
}

@Composable
fun AsyncMediaImage(
    media: SwipeDeleteMedia,
    modifier: Modifier = Modifier,
    onError: () -> Unit = {},
    contentScale: ContentScale = ContentScale.Crop,
    reloadKey: Int = 0,
) {
    val context=LocalContext.current
    var bitmap by remember(media.uri, reloadKey) { mutableStateOf(mediaBitmapCache.get(media.uri)) }
    LaunchedEffect(media.uri, reloadKey) {
        if (bitmap != null) return@LaunchedEffect
        val decoded = withContext(Dispatchers.IO) {
            runCatching {
                val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(media.uri))
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    val max = 1200
                    val maxDimension = maxOf(info.size.width, info.size.height)
                    if (maxDimension > max) {
                        decoder.setTargetSampleSize(
                            ((maxDimension + max - 1) / max).coerceAtLeast(1)
                        )
                    }
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }.asImageBitmap()
            }
        }
        decoded.onSuccess { loaded ->
            mediaBitmapCache.put(media.uri, loaded)
            bitmap = loaded
        }.onFailure {
            onError()
        }
    }
    if(bitmap==null) Box(modifier.background(LocalSwipeDeletePalette.current.inset))
    else androidx.compose.foundation.Image(bitmap!!, null, modifier, contentScale = contentScale)
}

@Composable
fun SwipeDeleteHeader(kicker: String,title: String,onBack:()->Unit,tone:Color=LocalSwipeDeletePalette.current.keep){
    Row(Modifier.fillMaxWidth().padding(18.dp,10.dp),verticalAlignment=Alignment.CenterVertically){
        KIconButton(painterResource(R.drawable.ic_back),"Back",onBack)
        Column(Modifier.padding(start=12.dp)){Kicker(kicker,color=tone);Heading(title)}
    }
}

@Composable
fun StateScreen(icon:Int,kicker:String,title:String,body:String,tone:Color,iconTint:Color?=tone,actions:@Composable ColumnScope.()->Unit){
    val p=LocalSwipeDeletePalette.current
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding().padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Spacer(Modifier.weight(1f));HardSurface(Modifier.size(112.dp),radius=SwipeDeleteDimens.radius2Xl){
            androidx.compose.foundation.Image(painterResource(icon),null,Modifier.align(Alignment.Center).size(56.dp),colorFilter=iconTint?.let { ColorFilter.tint(it) })
        }
        Spacer(Modifier.height(28.dp));Kicker(kicker,color=tone);Spacer(Modifier.height(8.dp));Heading(title,Modifier.fillMaxWidth(),29.sp,TextAlign.Center)
        Spacer(Modifier.height(12.dp));SwipeDeleteText(body,Modifier.fillMaxWidth(),color=p.textMuted,fontSize=15.sp,textAlign=TextAlign.Center)
        Spacer(Modifier.weight(1f));Column(Modifier.fillMaxWidth(),content=actions)
    }
}
