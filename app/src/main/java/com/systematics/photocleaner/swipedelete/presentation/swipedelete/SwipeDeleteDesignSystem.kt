package com.systematics.photocleaner.swipedelete.presentation.swipedelete

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.core.view.WindowCompat
import com.systematics.photocleaner.swipedelete.R

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

data class SwipeDeletePalette(
    val app: Color, val appRaised: Color, val card: Color, val cardRaised: Color, val inset: Color,
    val border: Color, val borderSoft: Color, val textStrong: Color, val textBody: Color,
    val textMuted: Color, val textFaint: Color, val keep: Color = Color(0xFFFF6B2C),
    val keepLight: Color = Color(0xFFFFB13C), val gone: Color = Color(0xFF00B8F0),
    val goneDark: Color = Color(0xFF2E6BFF), val reward: Color = Color(0xFFFFC63C),
    val rewardDark: Color = Color(0xFFFF7A00), val win: Color = Color(0xFF2ED77E),
)
val SwipeDeleteDarkPalette = SwipeDeletePalette(
    Color(0xFF0F0E0D), Color(0xFF171614), Color(0xFF201E1C), Color(0xFF2C2A27), Color(0xFF14110F),
    Color.Black, Color(0xFF35322E), Color.White, Color(0xFFE8E3D9), Color(0xFFA7A296), Color(0xFF8E897F),
)
val SwipeDeleteLightPalette = SwipeDeletePalette(
    Color(0xFFFBF6EC), Color(0xFFF5EFE1), Color.White, Color(0xFFFBF6EC), Color(0xFFEFE7D7),
    Color(0xFF14110F), Color(0xFFE3D9C6), Color(0xFF14110F), Color(0xFF2C2A27), Color(0xFF726E66), Color(0xFF726E66),
    reward = Color(0xFFF5A800), win = Color(0xFF16A85C),
)
val LocalSwipeDeletePalette = staticCompositionLocalOf { SwipeDeleteDarkPalette }
val LocalSwipeDeleteReducedMotion = staticCompositionLocalOf { false }
val LocalSwipeDeleteHaptics = staticCompositionLocalOf { true }

internal fun swipedeleteContentColorFor(background: Color): Color {
    val backgroundLuminance = background.luminance()
    val dark = Color(0xFF14110F)
    val light = Color.White
    val darkContrast = (backgroundLuminance + .05f) / (dark.luminance() + .05f)
    val lightContrast = (light.luminance() + .05f) / (backgroundLuminance + .05f)
    return if (darkContrast >= lightContrast) dark else light
}

object SwipeDeleteDimens {
    val space1 = 4.dp; val space2 = 8.dp; val space3 = 12.dp; val space4 = 16.dp; val space5 = 20.dp
    val space6 = 24.dp; val space8 = 32.dp; val radiusXs = 8.dp; val radiusSm = 12.dp
    val radiusMd = 18.dp; val radiusLg = 26.dp; val radiusXl = 34.dp; val radius2Xl = 44.dp
}
private val Archivo = FontFamily(Font(R.font.archivo_variable, weight = FontWeight.Normal))
object SwipeDeleteTypography {
    val display = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Black, letterSpacing = (-0.7).sp)
    val ui = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Medium)
    val numeral = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Black, letterSpacing = (-0.8).sp)
}
@Composable fun SwipeDeleteTheme(dark: Boolean, reducedMotion: Boolean, haptics: Boolean, content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            if (activity != null) {
                val window = activity.window
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !dark
                insetsController.isAppearanceLightNavigationBars = !dark
            }
        }
    }
    CompositionLocalProvider(
        LocalSwipeDeletePalette provides if (dark) SwipeDeleteDarkPalette else SwipeDeleteLightPalette,
        LocalSwipeDeleteReducedMotion provides reducedMotion, LocalSwipeDeleteHaptics provides haptics, content = content
    )
}
@Composable fun SwipeDeleteText(
    text: String, modifier: Modifier = Modifier, style: TextStyle = SwipeDeleteTypography.ui,
    color: Color = LocalSwipeDeletePalette.current.textBody, fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight? = null, textAlign: TextAlign? = null, maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) = BasicText(text = text, modifier = modifier, style = style.merge(TextStyle(color = color, fontSize = fontSize, fontWeight = fontWeight, textAlign = textAlign ?: TextAlign.Start)), maxLines = maxLines, overflow = overflow)
@Composable fun Kicker(text: String, modifier: Modifier = Modifier, color: Color = LocalSwipeDeletePalette.current.textMuted) =
    SwipeDeleteText(text.uppercase(), modifier, SwipeDeleteTypography.ui.copy(letterSpacing = 1.45.sp), color, 10.sp, FontWeight.ExtraBold, maxLines = 1)
@Composable fun Heading(text: String, modifier: Modifier = Modifier, size: TextUnit = 26.sp, textAlign: TextAlign? = null) =
    SwipeDeleteText(text, modifier, SwipeDeleteTypography.display, LocalSwipeDeletePalette.current.textStrong, size, FontWeight.Black, textAlign)
@Composable fun SwipeDeleteWord(modifier: Modifier = Modifier, size: TextUnit = 27.sp) {
    Row(modifier) {
        SwipeDeleteText("Photo", style = SwipeDeleteTypography.display, color = LocalSwipeDeletePalette.current.keep, fontSize = size, fontWeight = FontWeight.Black)
        SwipeDeleteText(" Cleaner", style = SwipeDeleteTypography.display, color = LocalSwipeDeletePalette.current.textStrong, fontSize = size, fontWeight = FontWeight.Black)
    }
}
@Composable fun HardSurface(
    modifier: Modifier = Modifier, radius: Dp = SwipeDeleteDimens.radiusLg,
    background: Color = LocalSwipeDeletePalette.current.card, borderColor: Color = LocalSwipeDeletePalette.current.border,
    shadowX: Dp = 5.dp, shadowY: Dp = 6.dp, borderWidth: Dp = 3.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp), content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.padding(end = shadowX, bottom = shadowY)) {
        Box(Modifier.matchParentSize().offset(shadowX, shadowY).clip(RoundedCornerShape(radius)).background(Color.Black))
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(radius)).background(background)
            .border(borderWidth, borderColor, RoundedCornerShape(radius)).padding(contentPadding), content = content)
    }
}
enum class KButtonStyle { Keep, Gone, Reward, Neutral, Ghost }
@Composable fun KButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier, style: KButtonStyle = KButtonStyle.Keep,
    enabled: Boolean = true, icon: Painter? = null, contentDescription: String = text,
) {
    val p = LocalSwipeDeletePalette.current; val shape = RoundedCornerShape(999.dp)
    val background = when (style) {
        KButtonStyle.Keep -> Brush.linearGradient(listOf(p.keepLight, p.keep))
        KButtonStyle.Gone -> Brush.linearGradient(listOf(Color(0xFF3ED8FF), p.goneDark))
        KButtonStyle.Reward -> Brush.linearGradient(listOf(Color(0xFFFFE24D), p.rewardDark))
        KButtonStyle.Neutral -> SolidColor(p.cardRaised); KButtonStyle.Ghost -> SolidColor(Color.Transparent)
    }
    val ink = when (style) {
        KButtonStyle.Neutral -> swipedeleteContentColorFor(p.cardRaised)
        KButtonStyle.Ghost -> p.textBody
        else -> Color(0xFF0F0E0D)
    }
    Box(modifier.heightIn(min = 54.dp).clip(shape).background(background, shape)
        .border(if (style == KButtonStyle.Ghost) 2.dp else 3.dp, if (enabled) p.border else p.borderSoft, shape)
        .clickable(remember { MutableInteractionSource() }, null, enabled, "Activate", Role.Button, onClick)
        .semantics { this.contentDescription = contentDescription }.padding(horizontal = 20.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                androidx.compose.foundation.Image(icon, null, Modifier.size(22.dp), colorFilter = ColorFilter.tint(if (enabled) ink else p.textFaint))
                Spacer(Modifier.width(8.dp))
            }
            SwipeDeleteText(text, color = if (enabled) ink else p.textFaint, fontSize = 14.sp, fontWeight = FontWeight.Black,
                style = SwipeDeleteTypography.ui.copy(letterSpacing = .6.sp))
        }
    }
}
@Composable fun KIconButton(
    painter: Painter, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 48.dp,
    tint: Color = LocalSwipeDeletePalette.current.textStrong, background: Color = LocalSwipeDeletePalette.current.card, enabled: Boolean = true,
) {
    val p = LocalSwipeDeletePalette.current
    Box(modifier.size(size).clip(CircleShape).background(background).border(2.dp, p.border, CircleShape)
        .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick).semantics { contentDescription = label },
        contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Image(painter, null, Modifier.size(size * .46f), colorFilter = ColorFilter.tint(if (enabled) tint else p.textFaint))
    }
}
@Composable fun KSwitch(checked: Boolean, label: String, onCheckedChange: (Boolean) -> Unit) {
    val p = LocalSwipeDeletePalette.current
    val x by animateFloatAsState(if (checked) 29f else 4f, spring(stiffness = 520f), label = "switch")
    Box(Modifier.size(62.dp, 42.dp).clip(CircleShape).background(if (checked) p.win else p.inset)
        .border(2.dp, p.border, CircleShape).clickable(role = Role.Switch) { onCheckedChange(!checked) }
        .semantics { contentDescription = label }) {
        Box(Modifier.offset(x.dp, 6.dp).size(26.dp).clip(CircleShape).background(Color.White).border(2.dp, p.border, CircleShape))
    }
}
@Composable fun ProgressRing(
    value: Float, modifier: Modifier = Modifier, size: Dp = 72.dp, color: Color = LocalSwipeDeletePalette.current.keep,
    stroke: Dp = 9.dp, center: @Composable BoxScope.() -> Unit = {},
) {
    val p = LocalSwipeDeletePalette.current
    Box(modifier.size(size).semantics { progressBarRangeInfo = ProgressBarRangeInfo(value.coerceIn(0f,1f), 0f..1f) }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = stroke.toPx(); drawArc(p.inset, -90f, 360f, false, style = Stroke(sw))
            drawArc(color, -90f, value.coerceIn(0f,1f) * 360f, false, style = Stroke(sw, cap = StrokeCap.Round))
        }
        center()
    }
}
@Composable fun StatNumber(value: String, label: String? = null, modifier: Modifier = Modifier, size: TextUnit = 42.sp, gradient: Boolean = false) {
    val p = LocalSwipeDeletePalette.current
    Column(modifier) {
        val numberStyle = if (gradient) SwipeDeleteTypography.numeral.copy(brush = Brush.linearGradient(listOf(p.reward, p.keep)), fontSize = size)
            else SwipeDeleteTypography.numeral.copy(color = p.textStrong, fontSize = size)
        BasicText(value, style = numberStyle)
        if (label != null) Kicker(label)
    }
}
@Composable fun Badge(text: String, modifier: Modifier = Modifier, tone: Color = LocalSwipeDeletePalette.current.reward) {
    val p = LocalSwipeDeletePalette.current
    Box(modifier.clip(CircleShape).background(tone).border(2.dp, p.border, CircleShape).padding(horizontal = 10.dp, vertical = 5.dp)) {
        SwipeDeleteText(text, color = swipedeleteContentColorFor(tone), fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}
@Composable fun StreakBadge(days: Int, modifier: Modifier = Modifier) = Badge("🔥 $days day streak", modifier, LocalSwipeDeletePalette.current.win)
@Composable fun ComboCounter(
    combo: Int,
    modifier: Modifier = Modifier,
    tone: Color = LocalSwipeDeletePalette.current.reward,
) = Badge("×$combo COMBO", modifier, tone)
@Composable fun Stamp(text: String, modifier: Modifier = Modifier, tone: Color = LocalSwipeDeletePalette.current.keep) {
    Box(modifier.rotate(-8f).border(3.dp, tone, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
        SwipeDeleteText(text.uppercase(), color = tone, fontSize = 16.sp, fontWeight = FontWeight.Black)
    }
}
@Composable fun LevelCard(level: Int, progress: Float, reclaimed: String, streak: Int, months: Int, modifier: Modifier = Modifier) {
    val p = LocalSwipeDeletePalette.current
    HardSurface(modifier.height(132.dp), radius = SwipeDeleteDimens.radius2Xl, contentPadding = PaddingValues(18.dp)) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(progress, size = 82.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SwipeDeleteText(level.toString(), style = SwipeDeleteTypography.numeral, color = p.textStrong, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Kicker("level")
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Kicker("Reclaimed so far"); StatNumber(reclaimed, size = 39.sp, gradient = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StreakBadge(streak); Spacer(Modifier.width(8.dp))
                    SwipeDeleteText("$months months cleared", color = p.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
@Composable fun PhotoCard(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) =
    HardSurface(modifier, radius = SwipeDeleteDimens.radiusXl, shadowX = 7.dp, shadowY = 9.dp, borderWidth = 4.dp, content = content)
@Composable fun PileTile(title: String, subtitle: String, progress: Float, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val p = LocalSwipeDeletePalette.current
    HardSurface(modifier.height(100.dp).clickable(role = Role.Button, onClick = onClick), radius = SwipeDeleteDimens.radiusLg,
        contentPadding = PaddingValues(15.dp)) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(progress, size = 66.dp) { SwipeDeleteText("${(progress * 100).toInt()}", style = SwipeDeleteTypography.numeral, color = p.textStrong, fontSize = 16.sp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) { Heading(title, size = 22.sp); Spacer(Modifier.height(5.dp)); Kicker(subtitle) }
            SwipeDeleteText("›", color = p.textFaint, fontSize = 28.sp, fontWeight = FontWeight.Black)
        }
    }
}
