package com.systematics.keepr.presentation.keepr

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.SystemClock
import android.util.LruCache
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
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
import com.systematics.keepr.R
import com.systematics.keepr.data.keepr.*
import com.systematics.keepr.domain.usecase.IsAdsEnabledUseCase
import com.systematics.keepr.domain.keepr.SwipeDecisionEngine
import com.systematics.keepr.domain.keepr.SwipeOutcome
import com.systematics.keepr.domain.keepr.GamificationRules
import com.systematics.keepr.domain.usecase.MarkFirstSessionCompletedUseCase
import com.systematics.keepr.utils.monetization.config.frontend.breakpoints.AdBreakPoint
import com.systematics.keepr.utils.monetization.config.frontend.config.AdsPlacement
import com.systematics.keepr.utils.providers.LocalAppLogEvents
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

fun Long.bytesLabel(): String = when {
    this >= 1024L * 1024 * 1024 -> "%.2f GB".format(this / (1024.0 * 1024 * 1024))
    this >= 1024L * 1024 -> "%.1f MB".format(this / (1024.0 * 1024))
    else -> "$this B"
}

@Composable
fun MonthPickerScreen(
    onSettings: () -> Unit,
    onPremium: () -> Unit,
    showPremium: Boolean,
    onStart: (String, String, Boolean) -> Unit,
    onResume: (String, String) -> Unit,
    onPermission: () -> Unit,
    onEmpty: () -> Unit,
    onSelected: () -> Unit,
    controller: KeeprController = koinInject(),
    isAdsEnabled: IsAdsEnabledUseCase = koinInject(),
    markFirstSessionCompleted: MarkFirstSessionCompletedUseCase = koinInject(),
    monetizationInstall: MonetizationInstall = monetizationInject(),
) {
    AnalyticsLaunch("months_scr_launch")
    val state by controller.state.collectAsStateWithLifecycle()
    val p = LocalKeeprPalette.current
    val events = LocalAppLogEvents.current
    LaunchedEffect(Unit) {
        markFirstSessionCompleted()
        if (isAdsEnabled()) monetizationInstall.executeBreakPoint(AdBreakPoint.BP_HOME_APPEAR)
        controller.refreshCatalog()
    }
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(18.dp, 8.dp, 18.dp, 12.dp), verticalAlignment = Alignment.CenterVertically) {
            KeeprWord(Modifier.weight(1f), 29.sp)
            KIconButton(painterResource(R.drawable.ic_premium), "Remove ads", {
                events.loadEvents("months_scr_prem_clck"); onPremium()
            }, Modifier.padding(end = 10.dp), tint = p.reward)
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
                        radius = KeeprDimens.radiusMd, background = p.cardRaised, borderColor = p.gone,
                        shadowX = 0.dp, shadowY = 0.dp, borderWidth = 2.dp, contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.foundation.Image(painterResource(R.drawable.ic_photo), null, Modifier.size(26.dp), colorFilter = ColorFilter.tint(p.gone))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Kicker("Selected photos", color = p.gone)
                                KeeprText("${state.selectedMedia.size} accessible · separate cleanup", color = p.textBody, fontWeight = FontWeight.Bold)
                            }
                            KeeprText("›", color = p.textFaint, fontSize = 24.sp)
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
                    HardSurface(Modifier.fillMaxWidth().height(100.dp), radius = KeeprDimens.radiusLg, background = p.card) {
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
                    EmptyPanel("Nothing to sort", "Keepr couldn't find accessible photos. Refresh or change which photos Android allows.", "View options", onEmpty, R.drawable.ic_photo)
                }
                CatalogStatus.Error -> item {
                    EmptyPanel("Scan paused", state.error ?: "Keepr couldn't read the camera roll.", "Try again", controller::refreshCatalog, R.drawable.ic_warning)
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
        if (isAdsEnabled()) InlineAdShowComponent(modifier = Modifier.fillMaxWidth(), placementKey = AdsPlacement.Inlines.HOME_BOTTOM)
    }
}

@Composable
private fun EmptyPanel(title: String, body: String, action: String, onClick: () -> Unit, icon: Int) {
    val p = LocalKeeprPalette.current
    HardSurface(Modifier.fillMaxWidth().heightIn(min = 210.dp), radius = KeeprDimens.radiusLg, contentPadding = PaddingValues(22.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.foundation.Image(painterResource(icon), null, Modifier.size(44.dp), colorFilter = ColorFilter.tint(p.keep))
            Spacer(Modifier.height(12.dp)); Heading(title, size = 22.sp)
            Spacer(Modifier.height(8.dp)); KeeprText(body, color = p.textMuted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp)); KButton(action, onClick, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun SelectedPhotosScreen(onBack: () -> Unit, onAccess: () -> Unit, onStart: () -> Unit, controller: KeeprController = koinInject()) {
    AnalyticsLaunch("selected_scr_launch")
    val state by controller.state.collectAsStateWithLifecycle()
    val p = LocalKeeprPalette.current
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding()) {
        KeeprHeader("Selected photos", "Partial access", onBack, p.gone)
        HardSurface(Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 18.dp), radius = KeeprDimens.radiusMd,
            background = p.cardRaised, borderColor = p.gone, shadowX = 0.dp, shadowY = 0.dp, borderWidth = 2.dp,
            contentPadding = PaddingValues(12.dp)) {
            KeeprText("These Android-selected photos are a separate cleanup from calendar months.", color = p.textBody, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        LazyVerticalGrid(
            GridCells.Fixed(3), Modifier.weight(1f).padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.selectedMedia, key = { it.uri }) { media ->
                AsyncMediaImage(media, Modifier.aspectRatio(1f).clip(RoundedCornerShape(KeeprDimens.radiusSm)).border(2.dp, p.border, RoundedCornerShape(KeeprDimens.radiusSm)))
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
    controller: KeeprController = koinInject(),
) {
    AnalyticsLaunch("clean_scr_launch")
    val state by controller.state.collectAsStateWithLifecycle()
    val session = state.session?.takeIf { expectedScopeKey == null || it.scopeKey == expectedScopeKey }
    val p = LocalKeeprPalette.current
    val events = LocalAppLogEvents.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val useHaptics = LocalKeeprHaptics.current
    val reducedMotion = LocalKeeprReducedMotion.current
    val offset = remember { Animatable(0f) }
    var cardWidth by remember { mutableFloatStateOf(1f) }
    var dragStart by remember { mutableLongStateOf(0L) }
    var loadError by remember { mutableStateOf(false) }
    var locallyCommittedUri by remember(session?.id) { mutableStateOf<String?>(null) }
    var commitInFlight by remember(session?.id) { mutableStateOf(false) }
    val pending = remember(session?.media, session?.decisions, locallyCommittedUri) {
        session?.let { activeSession ->
            val decidedUris = activeSession.decisions.mapTo(HashSet()) { it.media.uri }
            activeSession.media.filter { it.uri !in decidedUris && it.uri != locallyCommittedUri }
        }.orEmpty()
    }
    val current = pending.firstOrNull()

    fun commit(decision: MediaDecision) {
        val committedUri = current?.uri ?: return
        if (loadError || commitInFlight) return
        commitInFlight = true
        scope.launch {
            if (useHaptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val target = if (decision == MediaDecision.Keep) cardWidth * 1.25f else -cardWidth * 1.25f
            if (reducedMotion) offset.snapTo(target) else offset.animateTo(target, spring(dampingRatio = .72f, stiffness = 420f))
            events.loadEvents(if (decision == MediaDecision.Keep) "clean_scr_keep_clck" else "clean_scr_delete_clck")
            controller.decide(decision)
            // Remove only the departed item from the local stack. The promoted card
            // is rendered at rest while the durable decision is being persisted.
            locallyCommittedUri = committedUri
            offset.snapTo(0f)
            loadError = false

            // Restore interaction if persistence fails instead of leaving cleanup locked.
            delay(5_000)
            if (locallyCommittedUri == committedUri && controller.state.value.session?.current?.uri == committedUri) {
                locallyCommittedUri = null
                commitInFlight = false
            }
        }
    }

    LaunchedEffect(session?.id, session?.current?.uri, locallyCommittedUri) {
        val committedUri = locallyCommittedUri ?: return@LaunchedEffect
        if (session?.current?.uri != committedUri) {
            locallyCommittedUri = null
            commitInFlight = false
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
            KeeprText((session?.delete?.sumOf { it.media.sizeBytes ?: 0L } ?: 0L).bytesLabel(), color = p.reward, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(7.dp)); Kicker("queued to free")
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(20.dp, 2.dp, 20.dp, 0.dp)) {
            cardWidth = constraints.maxWidth.toFloat()
            if (current == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Kicker(if (session == null) "Loading your month…" else "Opening review…") }
            } else if (loadError) {
                HardSurface(Modifier.fillMaxSize(), radius = KeeprDimens.radiusXl, contentPadding = PaddingValues(28.dp)) {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        androidx.compose.foundation.Image(painterResource(R.drawable.ic_warning), null, Modifier.size(58.dp), colorFilter = ColorFilter.tint(p.gone))
                        Spacer(Modifier.height(14.dp)); Heading("Couldn't load this photo", size = 22.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp)); KeeprText("No keep or delete was recorded. Retry or skip it for now.", color=p.textMuted,textAlign=TextAlign.Center)
                        Spacer(Modifier.height(18.dp)); KButton("Retry", { loadError=false }, Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp)); KButton("More details", onRecovery, Modifier.fillMaxWidth(), KButtonStyle.Neutral)
                    }
                }
            } else {
                val next = pending.getOrNull(1)
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
                if (state.combo >= 2) ComboCounter(state.combo, Modifier.align(Alignment.TopEnd).zIndex(5f))
                PhotoCard(
                    Modifier.fillMaxSize().zIndex(1f).onSizeChanged { cardWidth = it.width.toFloat() }
                        .graphicsLayer { translationX = visualOffset; rotationZ = (visualOffset / cardWidth) * 12f }
                        .pointerInput(current.uri, commitInFlight) {
                            if (commitInFlight) return@pointerInput
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
                    AsyncMediaImage(current, Modifier.fillMaxSize(), onError = { loadError = true }, contentScale = ContentScale.Fit)
                    Box(Modifier.fillMaxSize().background(
                        when { visualOffset > 4 -> p.keep.copy(alpha=(abs(visualOffset)/cardWidth).coerceAtMost(.28f))
                            visualOffset < -4 -> p.gone.copy(alpha=(abs(visualOffset)/cardWidth).coerceAtMost(.28f))
                            else -> Color.Transparent }
                    ))
                    if (abs(visualOffset) > 16) Stamp(if (visualOffset > 0) "KEEP" else "DELETE", Modifier.align(if(visualOffset>0) Alignment.TopStart else Alignment.TopEnd).padding(22.dp), if(visualOffset>0)p.keep else p.gone)
                    HardSurface(Modifier.fillMaxWidth().height(60.dp).align(Alignment.BottomCenter).padding(8.dp), radius=KeeprDimens.radiusMd,
                        shadowX=0.dp,shadowY=0.dp,borderWidth=2.dp,background=p.card.copy(alpha=.92f),contentPadding=PaddingValues(10.dp)) {
                        Row(Modifier.fillMaxSize(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
                            Kicker(Instant.ofEpochMilli(current.takenAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM d, yyyy")))
                            KeeprText(current.sizeBytes?.bytesLabel() ?: "Size unknown",color=p.textStrong,fontWeight=FontWeight.Bold)
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
fun MediaRecoveryScreen(onBack: () -> Unit, onRetry: () -> Unit, onSkip: () -> Unit) {
    AnalyticsLaunch("recovery_scr_launch")
    val p=LocalKeeprPalette.current
    StateScreen(R.drawable.ic_warning,"Photo unavailable","No decision was recorded","This item may be missing, unreadable, or no longer allowed. Your existing decisions remain saved.", p.gone) {
        KButton("Retry",onRetry,Modifier.fillMaxWidth()); Spacer(Modifier.height(9.dp))
        KButton("Skip for now",onSkip,Modifier.fillMaxWidth(),KButtonStyle.Neutral); Spacer(Modifier.height(6.dp))
        KButton("Return",onBack,Modifier.fillMaxWidth(),KButtonStyle.Ghost)
    }
}

@Composable
fun AsyncMediaImage(
    media: KeeprMedia,
    modifier: Modifier = Modifier,
    onError: () -> Unit = {},
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context=LocalContext.current
    var bitmap by remember(media.uri) { mutableStateOf(mediaBitmapCache.get(media.uri)) }
    LaunchedEffect(media.uri) {
        if (bitmap != null) return@LaunchedEffect
        val decoded = withContext(Dispatchers.IO) {
            runCatching {
                val source=ImageDecoder.createSource(context.contentResolver,Uri.parse(media.uri))
                ImageDecoder.decodeBitmap(source) { decoder,info,_ ->
                    val max=1200
                    if(info.size.width>max||info.size.height>max) decoder.setTargetSampleSize((maxOf(info.size.width,info.size.height)/max).coerceAtLeast(1))
                    decoder.allocator=ImageDecoder.ALLOCATOR_SOFTWARE
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
    if(bitmap==null) Box(modifier.background(LocalKeeprPalette.current.inset))
    else androidx.compose.foundation.Image(bitmap!!, null, modifier, contentScale = contentScale)
}

@Composable
fun KeeprHeader(kicker: String,title: String,onBack:()->Unit,tone:Color=LocalKeeprPalette.current.keep){
    Row(Modifier.fillMaxWidth().padding(18.dp,10.dp),verticalAlignment=Alignment.CenterVertically){
        KIconButton(painterResource(R.drawable.ic_back),"Back",onBack)
        Column(Modifier.padding(start=12.dp)){Kicker(kicker,color=tone);Heading(title)}
    }
}

@Composable
fun StateScreen(icon:Int,kicker:String,title:String,body:String,tone:Color,actions:@Composable ColumnScope.()->Unit){
    val p=LocalKeeprPalette.current
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding().padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Spacer(Modifier.weight(1f));HardSurface(Modifier.size(112.dp),radius=KeeprDimens.radius2Xl){
            androidx.compose.foundation.Image(painterResource(icon),null,Modifier.align(Alignment.Center).size(56.dp),colorFilter=ColorFilter.tint(tone))
        }
        Spacer(Modifier.height(28.dp));Kicker(kicker,color=tone);Spacer(Modifier.height(8.dp));Heading(title,Modifier.fillMaxWidth(),29.sp,TextAlign.Center)
        Spacer(Modifier.height(12.dp));KeeprText(body,Modifier.fillMaxWidth(),color=p.textMuted,fontSize=15.sp,textAlign=TextAlign.Center)
        Spacer(Modifier.weight(1f));Column(Modifier.fillMaxWidth(),content=actions)
    }
}
