package com.systematics.keepr.presentation.keepr

import android.content.*
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systematics.keepr.BuildConfig
import com.systematics.keepr.R
import com.systematics.keepr.data.keepr.KeeprController
import com.systematics.keepr.utils.providers.LocalAppLogEvents
import org.koin.compose.koinInject

@Composable
fun KeeprSettingsScreen(
    onBack:()->Unit,onLanguage:()->Unit,onAccess:()->Unit,onAnalytics:()->Unit,onPrivacy:()->Unit,
    onFeedback:()->Unit,onRate:()->Unit,onReplayIntro:()->Unit,onReset:()->Unit,
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
            SettingsToggle(R.drawable.ic_settings,"Dark mode",if(state.darkMode)"On" else "Off",state.darkMode,controller::setDarkMode)
            SettingsToggle(R.drawable.ic_review,"Motion & animation",if(state.fullMotion)"Springy card physics" else "Reduced",state.fullMotion,controller::setFullMotion)
            SettingsToggle(R.drawable.ic_heart,"Haptics",if(state.haptics)"Feedback on every swipe" else "Off",state.haptics,controller::setHaptics)
            SettingsSection("Privacy")
            SettingsRow(R.drawable.ic_chart,"Usage analytics",if(state.analytics)"Sharing anonymous usage" else "Off",{events.loadEvents("settings_scr_analytics_clck");onAnalytics()})
            SettingsRow(R.drawable.ic_shield,"Privacy policy","Your photos stay on device",{events.loadEvents("settings_scr_privacy_clck");onPrivacy()})
            SettingsSection("Support")
            SettingsRow(R.drawable.ic_feedback,"Send feedback","Privacy-safe diagnostics",{events.loadEvents("settings_scr_feedback_clck");onFeedback()})
            SettingsRow(R.drawable.ic_star,"Rate Keepr","Open Google Play",{events.loadEvents("settings_scr_rate_clck");onRate()})
            SettingsRow(R.drawable.ic_review,"Replay intro","See how Keepr works",{events.loadEvents("settings_scr_intro_clck");onReplayIntro()})
            SettingsRow(R.drawable.ic_shield,"Diagnostics","App ${BuildConfig.VERSION_NAME} · Android ${android.os.Build.VERSION.RELEASE}",onFeedback)
            SettingsSection("Danger zone")
            SettingsRow(R.drawable.ic_trash,"Reset Keepr","Clear local data · keeps your photos",{events.loadEvents("settings_scr_reset_clck");onReset()},true)
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

@Composable
fun AnalyticsConsentScreen(onBack:()->Unit,controller:KeeprController=koinInject()){
    AnalyticsLaunch("analytics_scr_launch")
    val state by controller.state.collectAsStateWithLifecycle();val p=LocalKeeprPalette.current;val events=LocalAppLogEvents.current
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding()){
        KeeprHeader("Privacy","Usage analytics",onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally){
            HardSurface(Modifier.size(100.dp),radius=KeeprDimens.radius2Xl){androidx.compose.foundation.Image(painterResource(R.drawable.ic_chart),null,Modifier.align(Alignment.Center).size(48.dp),colorFilter=ColorFilter.tint(p.keep))}
            Spacer(Modifier.height(22.dp));Heading("Help improve Keepr",Modifier.fillMaxWidth(),26.sp,TextAlign.Center)
            Spacer(Modifier.height(10.dp));KeeprText("Anonymous usage analytics are optional and off until you choose to share. Keepr records bounded screen and action events only.",color=p.textMuted,textAlign=TextAlign.Center,fontSize=15.sp)
            Spacer(Modifier.height(18.dp))
            HardSurface(Modifier.fillMaxWidth().heightIn(min=154.dp),radius=KeeprDimens.radiusLg,shadowX=3.dp,shadowY=3.dp,contentPadding=PaddingValues(16.dp)){
                Column{Kicker("Never included",color=p.gone);Spacer(Modifier.height(9.dp));KeeprText("Photo content · thumbnails · filenames · dates · content URIs · media IDs · analytics identifiers in feedback",color=p.textBody,fontSize=14.sp)
                    Spacer(Modifier.height(14.dp));Kicker("Your control",color=p.win);Spacer(Modifier.height(6.dp));KeeprText("Turning analytics off disables collection and resets Firebase Analytics data.",color=p.textBody,fontSize=14.sp)}
            }
        }
        Column(Modifier.fillMaxWidth().background(p.appRaised).padding(18.dp,10.dp)){
            KButton(if(state.analytics)"Sharing anonymous usage" else "Share anonymous usage",{
                events.loadEvents("analytics_scr_enable_clck");controller.setAnalytics(true);onBack()
            },Modifier.fillMaxWidth(),enabled=!state.analytics)
            Spacer(Modifier.height(8.dp));KButton(if(state.analytics)"Turn off and reset" else "No thanks",{
                events.loadEvents("analytics_scr_disable_clck");controller.setAnalytics(false);onBack()
            },Modifier.fillMaxWidth(),if(state.analytics)KButtonStyle.Gone else KButtonStyle.Ghost)
        }
    }
}

@Composable
fun KeeprPrivacyScreen(onBack:()->Unit){
    AnalyticsLaunch("privacy_scr_launch");val p=LocalKeeprPalette.current
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding()){
        KeeprHeader("Your data","Privacy policy",onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp,6.dp,18.dp,30.dp)){
            HardSurface(Modifier.fillMaxWidth().heightIn(min=380.dp),radius=KeeprDimens.radiusLg,shadowX=3.dp,shadowY=3.dp,contentPadding=PaddingValues(18.dp)){
                Column{Heading("Your photos stay on this device.",size=20.sp)
                    PolicyParagraph("Keepr reads only photos you allow so you can review and organize them. Photo content, filenames, dates, and media identifiers are never uploaded.")
                    PolicyParagraph("Deletion is requested through Android system controls. Nothing is deleted until you confirm, and Keepr checks each URI before reporting success.")
                    PolicyParagraph("Anonymous usage analytics are optional and off by default. You can change that choice at any time in Settings.")
                    PolicyParagraph("Cleanup progress and preferences are stored locally. Reset Keepr removes that app data without deleting your photos.")
                    PolicyParagraph("Ads and billing use the starter app's retained Google/RevenueCat integrations. Buying Premium removes ads; cleanup features are never locked.")}
            }
            Spacer(Modifier.height(16.dp));Kicker("Last updated · August 15, 2026")
        }
    }
}
@Composable private fun PolicyParagraph(text:String){Spacer(Modifier.height(14.dp));KeeprText(text,color=LocalKeeprPalette.current.textBody,fontSize=14.sp)}

@Composable
fun FeedbackScreen(onBack:()->Unit){
    AnalyticsLaunch("feedback_scr_launch")
    val p=LocalKeeprPalette.current;val context=LocalContext.current;val clipboard=LocalClipboardManager.current;val events=LocalAppLogEvents.current
    var category by rememberSaveable{mutableStateOf("Idea")};var message by rememberSaveable{mutableStateOf("")};var error by remember{mutableStateOf(false)}
    val diagnostics="Keepr ${BuildConfig.VERSION_NAME}\nAndroid ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})\nNo photos, names, dates, URIs, media IDs, or analytics identifiers included."
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding()){
        KeeprHeader("Support","Send feedback",onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp)){
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Idea","Problem","Other").forEach{value->
                Box(Modifier.weight(1f).height(46.dp).background(if(category==value)p.keep else p.card,RoundedCornerShape(99.dp)).border(2.dp,p.border,RoundedCornerShape(99.dp)).clickable{category=value},contentAlignment=Alignment.Center){
                    KeeprText(value,color=if(category==value)Color(0xFF14110F) else p.textMuted,fontWeight=FontWeight.Black)
                }
            }}
            Spacer(Modifier.height(14.dp))
            BasicTextField(message,{message=it.take(4000);error=false},Modifier.fillMaxWidth().heightIn(min=140.dp).background(p.card,RoundedCornerShape(KeeprDimens.radiusMd)).border(3.dp,if(error)p.gone else p.border,RoundedCornerShape(KeeprDimens.radiusMd)).padding(14.dp),
                textStyle=TextStyle(color=p.textStrong,fontFamily=KeeprTypography.ui.fontFamily,fontSize=15.sp),decorationBox={inner->if(message.isBlank())KeeprText("Tell us what you think…",color=p.textFaint);inner()})
            if(error)KeeprText("Write a message before sharing.",Modifier.padding(top=7.dp),color=p.gone,fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            HardSurface(Modifier.fillMaxWidth().heightIn(min=130.dp),radius=KeeprDimens.radiusMd,shadowX=0.dp,shadowY=0.dp,borderWidth=2.dp,background=p.inset,contentPadding=PaddingValues(14.dp)){
                Column{Kicker("Attached diagnostics (safe)",color=p.win);Spacer(Modifier.height(8.dp));KeeprText(diagnostics,color=p.textMuted,fontSize=12.sp)
                    Spacer(Modifier.height(9.dp));KButton("Copy diagnostics",{clipboard.setText(androidx.compose.ui.text.AnnotatedString(diagnostics))},style=KButtonStyle.Ghost)}
            }
        }
        Column(Modifier.fillMaxWidth().background(p.appRaised).padding(18.dp,10.dp)){
            KButton("Share feedback",{
                if(message.isBlank()){error=true;return@KButton}
                events.loadEvents("feedback_scr_share_clck")
                val payload="Keepr feedback — $category\n\n${message.trim()}\n\n$diagnostics"
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_SUBJECT,"Keepr feedback");putExtra(Intent.EXTRA_TEXT,payload)},"Share Keepr feedback"))
            },Modifier.fillMaxWidth(),icon=painterResource(R.drawable.ic_feedback))
            Spacer(Modifier.height(6.dp));KButton("Cancel",onBack,Modifier.fillMaxWidth(),KButtonStyle.Ghost)
        }
    }
}

@Composable
fun RateUsScreen(onBack:()->Unit,onFeedback:()->Unit){
    AnalyticsLaunch("rate_scr_launch");val p=LocalKeeprPalette.current;val context=LocalContext.current;var stars by rememberSaveable{mutableIntStateOf(5)};val events=LocalAppLogEvents.current
    StateScreen(R.drawable.ic_app_logo,"A small favor","Enjoying Keepr?","A quick rating helps other people find a cleaner that respects them.",p.reward){
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
fun ResetKeeprScreen(onCancel:()->Unit,onReset:()->Unit){
    AnalyticsLaunch("reset_scr_launch");val p=LocalKeeprPalette.current
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.72f)).systemBarsPadding(),contentAlignment=Alignment.BottomCenter){
        Column(Modifier.fillMaxWidth().background(p.appRaised,RoundedCornerShape(34.dp,34.dp,0.dp,0.dp)).border(4.dp,p.border,RoundedCornerShape(34.dp,34.dp,0.dp,0.dp)).padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally){
            HardSurface(Modifier.size(78.dp),radius=KeeprDimens.radiusLg,background=p.gone.copy(alpha=.2f),borderColor=p.gone){androidx.compose.foundation.Image(painterResource(R.drawable.ic_warning),null,Modifier.align(Alignment.Center).size(38.dp),colorFilter=ColorFilter.tint(p.gone))}
            Spacer(Modifier.height(16.dp));Heading("Reset Keepr?",size=25.sp);Spacer(Modifier.height(10.dp))
            KeeprText("This clears sessions, decisions, preferences, consent, and the Analytics identifier on this device. Your MediaStore photos are never deleted.",color=p.textMuted,textAlign=TextAlign.Center)
            Spacer(Modifier.height(18.dp));KButton("Reset Keepr",onReset,Modifier.fillMaxWidth(),KButtonStyle.Gone);Spacer(Modifier.height(8.dp));KButton("Cancel",onCancel,Modifier.fillMaxWidth(),KButtonStyle.Ghost)
        }
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
fun EmptyLibraryScreen(monthTitle:String?,onMonths:()->Unit,onScan:()->Unit,onAccess:()->Unit,onSettings:()->Unit){
    AnalyticsLaunch("empty_scr_launch");val p=LocalKeeprPalette.current
    StateScreen(R.drawable.ic_photo,if(monthTitle==null)"Nothing to sort" else "Month is empty",if(monthTitle==null)"Your library is all clear" else "$monthTitle has no photos",
        if(monthTitle==null)"Keepr couldn't find accessible photos. Refresh or change which photos Android allows." else "Pick another month or refresh after changing photo access.",p.keep){
        KButton(if(monthTitle==null)"Scan again" else "Back to months",if(monthTitle==null)onScan else onMonths,Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp));KButton("Photo access",onAccess,Modifier.fillMaxWidth(),KButtonStyle.Neutral)
        Spacer(Modifier.height(6.dp));KButton("Settings",onSettings,Modifier.fillMaxWidth(),KButtonStyle.Ghost)
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
