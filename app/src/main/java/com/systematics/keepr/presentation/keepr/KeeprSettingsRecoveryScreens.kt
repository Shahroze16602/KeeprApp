package com.systematics.keepr.presentation.keepr

import android.content.*
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systematics.keepr.BuildConfig
import com.systematics.keepr.R
import com.systematics.keepr.data.keepr.KeeprController
import com.systematics.keepr.data.keepr.ThemeMode
import com.systematics.keepr.utils.core.getAppVersionCode
import com.systematics.keepr.utils.core.sendFeedbackEmail
import com.systematics.keepr.utils.providers.LocalAppLogEvents
import org.koin.compose.koinInject

@Composable
fun KeeprSettingsScreen(
    onBack:()->Unit,onLanguage:()->Unit,onAccess:()->Unit,onPrivacy:()->Unit,
    onFeedback:()->Unit,onRate:()->Unit,
    controller:KeeprController=koinInject()
){
    AnalyticsLaunch("settings_scr_launch")
    val state by controller.state.collectAsStateWithLifecycle();val p=LocalKeeprPalette.current
    val events=LocalAppLogEvents.current
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding()){
        KeeprHeader("Keepr","Settings",{events.loadEvents("settings_scr_back_clck");onBack()})
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp,2.dp,18.dp,28.dp)){
            SettingsSection("General")
            SettingsRow(R.drawable.ic_feedback,"Language","English",{events.loadEvents("settings_scr_lang_clck");onLanguage()})
            SettingsRow(R.drawable.ic_photo,"Photo access",when{controller.hasFullAccess()->"Full access";controller.hasPartialAccess()->"Selected photos";else->"Not allowed"},{events.loadEvents("settings_scr_access_clck");onAccess()})
            SettingsSection("Feel")
            ThemeSegmentedControl(state.themeMode, controller::setThemeMode)
            SettingsToggle(R.drawable.ic_review,"Motion & animation",if(state.fullMotion)"Springy card physics" else "Reduced",state.fullMotion,controller::setFullMotion)
            SettingsToggle(R.drawable.ic_heart,"Haptics",if(state.haptics)"Swipe & combo feedback" else "Off",state.haptics,controller::setHaptics)
            SettingsSection("Privacy")
            SettingsRow(R.drawable.ic_shield,"Privacy policy","Your photos stay on device",{events.loadEvents("settings_scr_privacy_clck");onPrivacy()})
            SettingsSection("Support")
            SettingsRow(R.drawable.ic_feedback,"Send feedback","Share an idea or report a problem",{events.loadEvents("settings_scr_feedback_clck");onFeedback()})
            SettingsRow(R.drawable.ic_star,"Rate Keepr","Open Google Play",{events.loadEvents("settings_scr_rate_clck");onRate()})
            Column(Modifier.fillMaxWidth().padding(top=24.dp),horizontalAlignment=Alignment.CenterHorizontally){
                KeeprWord(size=20.sp);Kicker("Version ${BuildConfig.VERSION_NAME} · Made for your camera roll",Modifier.padding(top=5.dp))
            }
        }
    }
}
@Composable private fun SettingsSection(text:String)=Kicker(text,Modifier.padding(4.dp,18.dp,4.dp,8.dp))
@Composable private fun SettingsRow(icon:Int,label:String,sub:String,onClick:()->Unit,danger:Boolean=false){
    val p=LocalKeeprPalette.current
    HardSurface(Modifier.fillMaxWidth().height(72.dp).padding(bottom=8.dp).clickable(onClick=onClick),radius=KeeprDimens.radiusMd,
        shadowX=0.dp,shadowY=0.dp,borderWidth=2.dp,contentPadding=PaddingValues(12.dp)){
        Row(Modifier.fillMaxSize(),verticalAlignment=Alignment.CenterVertically){
            Box(Modifier.size(40.dp).background(if(danger)p.gone.copy(alpha=.18f) else p.inset,RoundedCornerShape(KeeprDimens.radiusSm)),contentAlignment=Alignment.Center){
                androidx.compose.foundation.Image(painterResource(icon),null,Modifier.size(21.dp),colorFilter=ColorFilter.tint(if(danger)p.gone else p.keep))
            }
            Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){KeeprText(label,color=if(danger)p.gone else p.textStrong,fontWeight=FontWeight.ExtraBold,fontSize=14.sp);KeeprText(sub,color=p.textMuted,fontSize=12.sp)}
            KeeprText("›",color=p.textFaint,fontSize=22.sp)
        }
    }
}
@Composable private fun SettingsToggle(icon:Int,label:String,sub:String,checked:Boolean,onChange:(Boolean)->Unit){
    val p=LocalKeeprPalette.current
    HardSurface(Modifier.fillMaxWidth().height(72.dp).padding(bottom=8.dp),radius=KeeprDimens.radiusMd,shadowX=0.dp,shadowY=0.dp,borderWidth=2.dp,contentPadding=PaddingValues(12.dp)){
        Row(Modifier.fillMaxSize(),verticalAlignment=Alignment.CenterVertically){
            Box(Modifier.size(40.dp).background(p.inset,RoundedCornerShape(KeeprDimens.radiusSm)),contentAlignment=Alignment.Center){
                androidx.compose.foundation.Image(painterResource(icon),null,Modifier.size(21.dp),colorFilter=ColorFilter.tint(p.keep))
            }
            Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){KeeprText(label,color=p.textStrong,fontWeight=FontWeight.ExtraBold,fontSize=14.sp);KeeprText(sub,color=p.textMuted,fontSize=12.sp)}
            KSwitch(checked,label,onChange)
        }
    }
}
@Composable private fun ThemeSegmentedControl(
    selectedMode: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
){
    val p = LocalKeeprPalette.current
    val events = LocalAppLogEvents.current
    val options = listOf(
        ThemeMode.SYSTEM to "System",
        ThemeMode.LIGHT to "Light",
        ThemeMode.DARK to "Dark"
    )
    HardSurface(
        modifier = modifier.fillMaxWidth().padding(bottom = 8.dp),
        radius = KeeprDimens.radiusMd,
        shadowX = 0.dp,
        shadowY = 0.dp,
        borderWidth = 2.dp,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(40.dp).background(p.inset, RoundedCornerShape(KeeprDimens.radiusSm)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painterResource(R.drawable.ic_settings),
                        null,
                        Modifier.size(21.dp),
                        colorFilter = ColorFilter.tint(p.keep)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    KeeprText(
                        "Theme",
                        color = p.textStrong,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                    KeeprText(
                        when (selectedMode) {
                            ThemeMode.SYSTEM -> "Follow system settings"
                            ThemeMode.LIGHT -> "Light theme"
                            ThemeMode.DARK -> "Dark theme"
                        },
                        color = p.textMuted,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(KeeprDimens.radiusSm))
                    .background(p.inset)
                    .border(2.dp, p.borderSoft, RoundedCornerShape(KeeprDimens.radiusSm))
                    .padding(3.dp)
            ) {
                Row(
                    Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    options.forEach { (mode, label) ->
                        val selected = selectedMode == mode
                        val bg = if (selected) p.cardRaised else Color.Transparent
                        val border = if (selected) p.border else Color.Transparent
                        val textColor = if (selected) p.textStrong else p.textMuted
                        val fontWeight = if (selected) FontWeight.Black else FontWeight.Medium

                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(KeeprDimens.radiusXs))
                                .background(bg)
                                .border(if (selected) 2.dp else 0.dp, border, RoundedCornerShape(KeeprDimens.radiusXs))
                                .clickable {
                                    events.loadEvents("settings_scr_theme_${label.lowercase()}_clck")
                                    onSelect(mode)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            KeeprText(
                                label,
                                color = textColor,
                                fontSize = 13.sp,
                                fontWeight = fontWeight,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackScreen(onBack:()->Unit){
    AnalyticsLaunch("feedback_scr_launch")
    val p=LocalKeeprPalette.current;val context=LocalContext.current;val events=LocalAppLogEvents.current
    var category by rememberSaveable{mutableStateOf("Idea")};var message by rememberSaveable{mutableStateOf("")};var error by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding()){
        KeeprHeader("Support","Send feedback",onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp)){
            Kicker("What is this about?")
            Spacer(Modifier.height(9.dp))
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                listOf("Idea","Problem","Other").forEach{value->
                    val selected=category==value
                    Box(
                        Modifier.weight(1f).height(46.dp)
                            .background(if(selected)p.keep else p.card,RoundedCornerShape(99.dp))
                            .border(2.dp,p.border,RoundedCornerShape(99.dp))
                            .selectable(selected=selected,role=Role.RadioButton,onClick={category=value}),
                        contentAlignment=Alignment.Center
                    ){
                        KeeprText(value,color=if(selected)keeprContentColorFor(p.keep) else p.textMuted,fontWeight=FontWeight.Black)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            BasicTextField(message,{message=it.take(4000);error=false},Modifier.fillMaxWidth().heightIn(min=140.dp).background(p.card,RoundedCornerShape(KeeprDimens.radiusMd)).border(3.dp,if(error)p.gone else p.border,RoundedCornerShape(KeeprDimens.radiusMd)).padding(14.dp),
                textStyle=TextStyle(color=p.textStrong,fontFamily=KeeprTypography.ui.fontFamily,fontSize=15.sp),decorationBox={inner->if(message.isBlank())KeeprText("Tell us what you think…",color=p.textFaint);inner()})
            if(error)KeeprText("Write a message before sharing.",Modifier.padding(top=7.dp),color=p.gone,fontWeight=FontWeight.Bold)
        }
        Column(Modifier.fillMaxWidth().background(p.appRaised).padding(18.dp,10.dp)){
            KButton("Share feedback",{
                if(message.isBlank()){error=true;return@KButton}
                events.loadEvents("feedback_scr_share_clck")
                context.sendFeedbackEmail(
                    email = "",
                    appName = context.getString(R.string.app_name),
                    versionCode = context.getAppVersionCode(),
                    message = "Category: $category\n\n${message.trim()}",
                )
            },Modifier.fillMaxWidth(),icon=painterResource(R.drawable.ic_feedback))
            Spacer(Modifier.height(6.dp));KButton("Cancel",onBack,Modifier.fillMaxWidth(),KButtonStyle.Ghost)
        }
    }
}

@Composable
fun RateUsScreen(onBack:()->Unit,onFeedback:()->Unit){
    AnalyticsLaunch("rate_scr_launch");val p=LocalKeeprPalette.current;val context=LocalContext.current;var stars by rememberSaveable{mutableIntStateOf(5)};val events=LocalAppLogEvents.current
    StateScreen(R.drawable.ic_app_logo,"A small favor","Enjoying Keepr?","A quick rating helps other people find a cleaner that respects them.",p.reward,iconTint=null){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){(1..5).forEach{s->Box(Modifier.size(52.dp).clickable{stars=s},contentAlignment=Alignment.Center){KeeprText("★",color=if(s<=stars)p.reward else p.cardRaised,fontSize=38.sp)}}}
        Spacer(Modifier.height(16.dp));KButton("Rate on Google Play",{
            events.loadEvents("rate_scr_play_clck")
            val id=context.packageName
            val market=Intent(Intent.ACTION_VIEW,"market://details?id=$id".toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching{context.startActivity(market)}.onFailure{context.startActivity(Intent(Intent.ACTION_VIEW,"https://play.google.com/store/apps/details?id=$id".toUri()))}
        },Modifier.fillMaxWidth(),KButtonStyle.Reward,icon=painterResource(R.drawable.ic_star))
        Spacer(Modifier.height(8.dp));KButton("Send private feedback",onFeedback,Modifier.fillMaxWidth(),KButtonStyle.Ghost)
        KButton("Maybe later",onBack,Modifier.fillMaxWidth(),KButtonStyle.Ghost)
    }
}

@Composable
fun PermissionDeniedScreen(permanent:Boolean,onRetry:()->Unit,onSelected:()->Unit,onPrivacy:()->Unit,onNotNow:()->Unit){
    AnalyticsLaunch("denied_scr_launch");val p=LocalKeeprPalette.current;val context=LocalContext.current
    StateScreen(R.drawable.ic_lock,"Photo access needed",if(permanent)"Allow access in Settings" else "Keepr can't see your photos yet",
        "Your saved sessions stay intact. Allow full access, choose selected photos, or continue without scanning.",p.gone){
        KButton(if(permanent)"Open Android Settings" else "Try again",{
            if(permanent)context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.fromParts("package",context.packageName,null))) else onRetry()
        },Modifier.fillMaxWidth(),KButtonStyle.Gone)
        Spacer(Modifier.height(8.dp));KButton("Choose selected photos",onSelected,Modifier.fillMaxWidth(),KButtonStyle.Neutral)
        Spacer(Modifier.height(6.dp));KButton("Privacy details",onPrivacy,Modifier.fillMaxWidth(),KButtonStyle.Ghost)
        KButton("Not now",onNotNow,Modifier.fillMaxWidth(),KButtonStyle.Ghost)
    }
}
@Composable
fun ResumeSessionScreen(onResume:()->Unit,onRestart:()->Unit,onMonths:()->Unit,onReview:()->Unit,controller:KeeprController=koinInject()){
    AnalyticsLaunch("resume_scr_launch");val state by controller.state.collectAsStateWithLifecycle();val s=state.session;val p=LocalKeeprPalette.current
    StateScreen(R.drawable.ic_undo,"Progress restored","Continue where you left off",
        "Keepr saved ${s?.decisions?.size?:0} decisions from ${s?.title?:"this cleanup"} on this device. No deletion has started.",p.reward){
        KButton("Resume cleanup",onResume,Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp));KButton("Review saved choices",onReview,Modifier.fillMaxWidth(),KButtonStyle.Neutral,enabled=s?.decisions?.isNotEmpty()==true)
        Spacer(Modifier.height(8.dp));KButton("Start over",onRestart,Modifier.fillMaxWidth(),KButtonStyle.Gone)
        Spacer(Modifier.height(6.dp));KButton("Month picker",onMonths,Modifier.fillMaxWidth(),KButtonStyle.Ghost)
    }
}
