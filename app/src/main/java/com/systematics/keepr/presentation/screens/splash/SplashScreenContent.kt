package com.systematics.keepr.presentation.screens.splash

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systematics.keepr.R
import com.systematics.keepr.presentation.keepr.*

@Composable
fun SplashScreenContent(showProgress: Boolean = true, progress: Float = 0f) {
    val p=LocalKeeprPalette.current
    Column(Modifier.fillMaxSize().background(p.app).systemBarsPadding(),horizontalAlignment=Alignment.CenterHorizontally){
        Spacer(Modifier.weight(1f))
        HardSurface(Modifier.size(124.dp),radius=KeeprDimens.radius2Xl,background=p.card,shadowX=8.dp,shadowY=10.dp,borderWidth=0.dp){
            androidx.compose.foundation.Image(painterResource(R.drawable.ic_app_logo),null,Modifier.fillMaxSize().padding(7.dp))
        }
        Spacer(Modifier.height(24.dp));KeeprWord(size=42.sp);Spacer(Modifier.height(11.dp));Kicker("Keep what matters · clear the rest")
        Spacer(Modifier.weight(1f))
        if(showProgress){
            Box(Modifier.fillMaxWidth().padding(horizontal=70.dp).height(10.dp).clip(CircleShape).background(p.inset).border(2.dp,p.border,CircleShape)){
                Box(Modifier.fillMaxHeight().fillMaxWidth(progress.coerceIn(0f,1f)).background(Brush.horizontalGradient(listOf(p.keepLight,p.keep))))
            }
        } else Spacer(Modifier.height(10.dp))
        Spacer(Modifier.height(42.dp))
    }
}
