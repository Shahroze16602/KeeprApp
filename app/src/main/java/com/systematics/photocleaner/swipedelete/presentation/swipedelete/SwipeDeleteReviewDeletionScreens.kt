package com.systematics.photocleaner.swipedelete.presentation.swipedelete

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systematics.photocleaner.swipedelete.R
import com.systematics.photocleaner.swipedelete.data.swipedelete.*
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ReviewScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    onStartOver: () -> Unit,
    onConfirm: () -> Unit,
    onFinish: () -> Unit,
    controller: SwipeDeleteController = koinInject(),
) {
    AnalyticsLaunch("review_scr_launch")
    val state by controller.state.collectAsStateWithLifecycle()
    val session = state.session
    val allReviewed = session != null && session.decisions.size >= session.media.size
    val handleBack = { if (allReviewed) onSave() else onBack() }
    BackHandler(onBack = handleBack)
    val p = LocalSwipeDeletePalette.current
    val events = com.systematics.photocleaner.swipedelete.utils.providers.LocalAppLogEvents.current
    var tab by rememberSaveable { mutableStateOf(MediaDecision.Delete) }
    var pendingMoveUri by rememberSaveable { mutableStateOf<String?>(null) }
    val shown = when (tab) { MediaDecision.Keep -> session?.keep; MediaDecision.Delete -> session?.delete; else -> session?.unavailable }.orEmpty()
    val deleteBytes = session?.delete?.sumOf { it.media.sizeBytes ?: 0L } ?: 0

    fun movePhoto(uri: String) {
        session?.decisions?.firstOrNull { it.media.uri == uri } ?: return
        controller.moveDecision(uri)
    }

    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding()) {
        SwipeDeleteHeader("Almost done", "Review ${session?.title ?: ""}", {
            events.loadEvents("review_scr_back_clck"); handleBack()
        })
        Row(Modifier.fillMaxWidth().padding(18.dp, 6.dp, 18.dp, 10.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            ReviewTab("Keep", session?.keep?.size ?: 0, tab == MediaDecision.Keep, p.keep) { tab = MediaDecision.Keep }
            ReviewTab("Delete", session?.delete?.size ?: 0, tab == MediaDecision.Delete, p.gone) { tab = MediaDecision.Delete }
            if (session?.unavailable?.isNotEmpty() == true) ReviewTab("Unresolved", session.unavailable.size, tab == MediaDecision.Unavailable, p.reward) { tab = MediaDecision.Unavailable }
        }
        if (tab == MediaDecision.Delete) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                SwipeDeleteText("Est. ${deleteBytes.bytesLabel()} freed · tap a photo to keep it instead", color = p.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        LazyVerticalGrid(
            GridCells.Fixed(3), Modifier.weight(1f).padding(18.dp, 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(shown, key = { it.media.uri }) { row ->
                Box(
                    Modifier.aspectRatio(.75f).clip(RoundedCornerShape(SwipeDeleteDimens.radiusSm))
                        .border(2.dp, when (row.decision) { MediaDecision.Keep -> p.keep; MediaDecision.Delete -> p.gone; else -> p.reward }, RoundedCornerShape(SwipeDeleteDimens.radiusSm))
                        .clickable(enabled = row.decision != MediaDecision.Unavailable) {
                            events.loadEvents("review_scr_move_clck")
                            if (controller.hasSeenReviewMoveHint()) movePhoto(row.media.uri)
                            else pendingMoveUri = row.media.uri
                        }
                ) {
                    AsyncMediaImage(row.media, Modifier.fillMaxSize())
                    Box(Modifier.fillMaxSize().background(if (row.decision == MediaDecision.Delete) p.gone.copy(alpha=.18f) else Color.Transparent))
                    Badge(if (row.decision == MediaDecision.Keep) "♥" else if (row.decision == MediaDecision.Delete) "×" else "!", Modifier.padding(5.dp), when(row.decision){MediaDecision.Keep->p.keep;MediaDecision.Delete->p.gone;else->p.reward})
                }
            }
            if (shown.isEmpty()) item(span = { GridItemSpan(3) }) {
                Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { Kicker("Nothing in this group") }
            }
        }
        Column(Modifier.fillMaxWidth().background(p.appRaised).padding(18.dp, 10.dp)) {
            when {
                !allReviewed -> KButton("Continue sorting", {
                    events.loadEvents("review_scr_continue_clck"); onBack()
                }, Modifier.fillMaxWidth())
                session.delete.isNotEmpty() -> KButton("Delete ${session.delete.size} permanently", {
                    events.loadEvents("review_scr_delete_clck"); onConfirm()
                }, Modifier.fillMaxWidth(), KButtonStyle.Gone, icon = painterResource(R.drawable.ic_trash))
                else -> KButton("Finish · 0 to delete", {
                    events.loadEvents("review_scr_finish_clck"); onFinish()
                }, Modifier.fillMaxWidth(), icon = painterResource(R.drawable.ic_shield))
            }
            Spacer(Modifier.height(7.dp))
            KButton("Start over", {
                events.loadEvents("review_scr_restart_clck"); onStartOver()
            }, Modifier.fillMaxWidth(), KButtonStyle.Neutral)
            Spacer(Modifier.height(7.dp))
            KButton("Save for later", { events.loadEvents("review_scr_save_clck"); onSave() }, Modifier.fillMaxWidth(), KButtonStyle.Ghost)
        }
    }
    if (pendingMoveUri != null) {
        ReviewMoveHintDialog(
            onDismiss = { pendingMoveUri = null },
            onMove = {
                val uri = pendingMoveUri
                pendingMoveUri = null
                controller.markReviewMoveHintSeen()
                if (uri != null) movePhoto(uri)
            },
        )
    }
}

@Composable
private fun ReviewMoveHintDialog(onDismiss: () -> Unit, onMove: () -> Unit) {
    val p = LocalSwipeDeletePalette.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(SwipeDeleteDimens.radiusXl))
                    .background(p.appRaised)
                    .border(4.dp, p.border, RoundedCornerShape(SwipeDeleteDimens.radiusXl))
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HardSurface(
                    modifier = Modifier.size(70.dp),
                    radius = SwipeDeleteDimens.radiusLg,
                    background = p.reward.copy(alpha = .22f),
                    borderColor = p.reward,
                    shadowX = 4.dp,
                    shadowY = 5.dp,
                ) {
                    androidx.compose.foundation.Image(
                        painterResource(R.drawable.ic_review),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(34.dp),
                        colorFilter = ColorFilter.tint(p.reward),
                    )
                }
                Spacer(Modifier.height(18.dp))
                Heading("Move photos between lists", size = 24.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(9.dp))
                SwipeDeleteText(
                    "Tapping a photo switches it between Keep and Delete. You can tap it again to move it back.",
                    modifier = Modifier.fillMaxWidth(),
                    color = p.textMuted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(20.dp))
                KButton("Move photo", onMove, Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                KButton("Cancel", onDismiss, Modifier.fillMaxWidth(), KButtonStyle.Ghost)
            }
        }
    }
}

@Composable
private fun RowScope.ReviewTab(label: String, count: Int, selected: Boolean, tone: Color, onClick: () -> Unit) {
    val p = LocalSwipeDeletePalette.current
    Box(
        Modifier.weight(1f).height(52.dp).clip(CircleShape)
            .background(if (selected) tone else p.card).border(3.dp, p.border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        SwipeDeleteText("$label $count", color = if (selected) swipedeleteContentColorFor(tone) else p.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun DeletionConfirmationScreen(onBack: () -> Unit, onConfirm: () -> Unit, controller: SwipeDeleteController = koinInject()) {
    AnalyticsLaunch("confirm_scr_launch")
    val state by controller.state.collectAsStateWithLifecycle()
    val count = state.session?.delete?.size ?: 0
    val bytes = state.session?.delete?.sumOf { it.media.sizeBytes ?: 0L } ?: 0
    val batches = (count + SwipeDeleteController.PLATFORM_DELETE_LIMIT - 1) / SwipeDeleteController.PLATFORM_DELETE_LIMIT
    val p = LocalSwipeDeletePalette.current
    val events = com.systematics.photocleaner.swipedelete.utils.providers.LocalAppLogEvents.current
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.72f)).systemBarsPadding(), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(34.dp,34.dp,0.dp,0.dp)).background(p.appRaised)
                .border(4.dp,p.border,RoundedCornerShape(34.dp,34.dp,0.dp,0.dp)).padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(46.dp,6.dp).clip(CircleShape).background(p.borderSoft))
            Spacer(Modifier.height(18.dp))
            HardSurface(Modifier.size(78.dp),radius=SwipeDeleteDimens.radiusLg,background=p.gone,shadowX=5.dp,shadowY=6.dp) {
                androidx.compose.foundation.Image(painterResource(R.drawable.ic_trash),null,Modifier.align(Alignment.Center).size(38.dp),colorFilter=ColorFilter.tint(Color(0xFF04121A)))
            }
            Spacer(Modifier.height(17.dp))
            Heading("Delete $count photos\npermanently?",Modifier.fillMaxWidth(),27.sp,TextAlign.Center)
            Spacer(Modifier.height(11.dp))
            SwipeDeleteText(
                "Android will ask you to confirm${if(batches>1) " in $batches batches" else ""}. This frees an estimated ${bytes.bytesLabel()}. Kept photos are never touched.",
                Modifier.fillMaxWidth(),color=p.textMuted,fontSize=14.sp,textAlign=TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            KButton("Delete permanently",{events.loadEvents("confirm_scr_delete_clck");onConfirm()},Modifier.fillMaxWidth(),KButtonStyle.Gone,icon=painterResource(R.drawable.ic_trash))
            Spacer(Modifier.height(8.dp));KButton("Cancel",{events.loadEvents("confirm_scr_back_clck");onBack()},Modifier.fillMaxWidth(),KButtonStyle.Ghost)
        }
    }
}

@Composable
fun DeletionProgressScreen(
    onComplete: () -> Unit,
    onPartial: () -> Unit,
    onLater: () -> Unit,
    controller: SwipeDeleteController = koinInject(),
) {
    AnalyticsLaunch("delete_scr_launch")
    val state by controller.state.collectAsStateWithLifecycle()
    val p=LocalSwipeDeletePalette.current
    var generation by rememberSaveable { mutableIntStateOf(0) }
    var promptCount by rememberSaveable { mutableIntStateOf(0) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val scope=rememberCoroutineScope()
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        scope.launch { controller.onDeletionResult(); generation++ }
    }
    LaunchedEffect(generation) {
        when(val step=controller.nextDeletionStep()){
            is DeleteStep.Launch -> { promptCount=step.count; launcher.launch(IntentSenderRequest.Builder(step.intentSender).build()) }
            DeleteStep.Finished -> if(controller.state.value.deletion.unresolved>0) onPartial() else onComplete()
            is DeleteStep.Failed -> error=step.message
        }
    }
    if(error!=null) {
        StateScreen(R.drawable.ic_warning,"Deletion paused","Photo Cleaner couldn't continue",error!!,p.gone){
            KButton("Try again",{error=null;generation++},Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp));KButton("Return later",onLater,Modifier.fillMaxWidth(),KButtonStyle.Ghost)
        }
        return
    }
    val result=state.deletion
    val pct=if(result.requested==0) 1f else (result.confirmed+result.unresolved).toFloat()/result.requested
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding().padding(30.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
        ProgressRing(pct,size=150.dp,color=p.gone,stroke=14.dp){
            SwipeDeleteText((result.confirmed+result.unresolved).toString(),style=SwipeDeleteTypography.numeral,color=p.textStrong,fontSize=42.sp,fontWeight=FontWeight.Black)
        }
        Spacer(Modifier.height(24.dp))
        Heading(if(promptCount>0) "Confirm with Android" else "Reconciling…",size=24.sp)
        Spacer(Modifier.height(8.dp))
        SwipeDeleteText(
            if(promptCount>0) "Android is asking permission to delete $promptCount photos."
            else "Re-checking every requested photo — no result is assumed.",
            color=p.textMuted,fontSize=14.sp,textAlign=TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        Kicker("${result.confirmed} confirmed · ${result.unresolved} unresolved")
    }
}

@Composable
fun CompletionScreen(onMonths:()->Unit,onRate:()->Unit,onPartial:()->Unit,controller:SwipeDeleteController=koinInject()){
    AnalyticsLaunch("complete_scr_launch")
    val state by controller.state.collectAsStateWithLifecycle();val p=LocalSwipeDeletePalette.current
    val session=state.session;val result=state.deletion
    val events=com.systematics.photocleaner.swipedelete.utils.providers.LocalAppLogEvents.current
    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(p.keep.copy(alpha=.22f),p.app),radius=1100f))){
        CompletionConfetti(p)
        Column(Modifier.fillMaxSize().systemBarsPadding().padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Spacer(Modifier.height(22.dp));Badge("🏆 MONTH CLEARED",tone=p.reward);Spacer(Modifier.height(14.dp))
        Heading("${session?.title ?: "Cleanup"} cleared!",Modifier.fillMaxWidth(),31.sp,TextAlign.Center)
        Spacer(Modifier.height(8.dp));StatNumber("+${result.confirmedBytes.bytesLabel()}",size=66.sp,gradient=true)
        Kicker("Estimated space freed")
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){
            CompletionStat("♥",session?.keep?.size?:0,"Kept",p.keep,Modifier.weight(1f))
            CompletionStat("×",result.confirmed,"Deleted",p.gone,Modifier.weight(1f))
            CompletionStat("!",result.unresolved,"Unresolved",p.reward,Modifier.weight(1f))
        }
        Spacer(Modifier.height(13.dp));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){StreakBadge(state.streak);Badge("+200 XP",tone=p.cardRaised)}
        if(result.unresolved>0){Spacer(Modifier.height(18.dp));KButton("Resolve remaining",onPartial,Modifier.fillMaxWidth(),KButtonStyle.Gone)}
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
            KButton("Back to months",{events.loadEvents("complete_scr_months_clck");onMonths()},Modifier.weight(1f),KButtonStyle.Neutral)
            KButton("Rate Photo Cleaner",{events.loadEvents("complete_scr_rate_clck");onRate()},Modifier.weight(1f),KButtonStyle.Ghost,icon=painterResource(R.drawable.ic_star))
        }
    }
    }
}

@Composable
private fun CompletionConfetti(p: SwipeDeletePalette) {
    if (LocalSwipeDeleteReducedMotion.current) return
    val transition = rememberInfiniteTransition(label = "completion confetti")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "confetti fall",
    )
    val colors = remember(p) { listOf(p.keep, p.gone, p.reward, p.win, Color(0xFFFF6FB5)) }
    Canvas(Modifier.fillMaxSize()) {
        val border = 2.dp.toPx()
        repeat(46) { index ->
            val particleWidth = (8 + (index * 7 % 10)).dp.toPx()
            val particleHeight = particleWidth * 1.4f
            val x = ((index * 37 + 11) % 100) / 100f * size.width
            val delay = ((index * 23) % 100) / 100f
            val speed = 0.72f + ((index * 17) % 48) / 100f
            val fraction = (phase * speed + delay) % 1f
            val y = -particleHeight + fraction * (size.height + particleHeight * 2f)
            val pivot = Offset(x + particleWidth / 2f, y + particleHeight / 2f)
            rotate(phase * 560f + index * 31f, pivot) {
                drawRect(
                    color = p.border,
                    topLeft = Offset(x - border, y - border),
                    size = Size(particleWidth + border * 2f, particleHeight + border * 2f),
                )
                drawRect(
                    color = colors[index % colors.size],
                    topLeft = Offset(x, y),
                    size = Size(particleWidth, particleHeight),
                )
            }
        }
    }
}

@Composable private fun CompletionStat(icon:String,value:Int,label:String,tone:Color,modifier:Modifier=Modifier){
    val p=LocalSwipeDeletePalette.current
    HardSurface(modifier.height(112.dp),radius=SwipeDeleteDimens.radiusLg,shadowX=3.dp,shadowY=3.dp,contentPadding=PaddingValues(10.dp)){
        Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
            SwipeDeleteText(icon,color=tone,fontSize=20.sp,fontWeight=FontWeight.Black)
            SwipeDeleteText(value.toString(),style=SwipeDeleteTypography.numeral,color=p.textStrong,fontSize=27.sp,fontWeight=FontWeight.Black);Kicker(label)
        }
    }
}

@Composable
fun PartialDeletionScreen(onReview:()->Unit,onRetry:()->Unit,onMonths:()->Unit,controller:SwipeDeleteController=koinInject()){
    AnalyticsLaunch("partial_scr_launch")
    val state by controller.state.collectAsStateWithLifecycle();val r=state.deletion;val p=LocalSwipeDeletePalette.current
    StateScreen(R.drawable.ic_warning,"Action needed","${r.confirmed} deleted · ${r.unresolved} unresolved",
        "Android confirmed some deletions, but other photos are still present or unavailable. Photo Cleaner never counts those as deleted.",p.gone){
        KButton("Review unresolved",onReview,Modifier.fillMaxWidth(),KButtonStyle.Gone)
        Spacer(Modifier.height(8.dp));KButton("Retry eligible",onRetry,Modifier.fillMaxWidth(),KButtonStyle.Neutral)
        Spacer(Modifier.height(6.dp));KButton("Back to months",onMonths,Modifier.fillMaxWidth(),KButtonStyle.Ghost)
    }
}
