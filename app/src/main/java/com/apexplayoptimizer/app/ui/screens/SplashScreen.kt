package com.apexplayoptimizer.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.apexplayoptimizer.app.R
import com.apexplayoptimizer.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val phases = listOf(
        stringResource(R.string.splash_phase_1),
        stringResource(R.string.splash_phase_2),
        stringResource(R.string.splash_phase_3),
        stringResource(R.string.splash_phase_4),
        stringResource(R.string.splash_phase_5),
    )
    var phase    by remember { mutableIntStateOf(0) }
    var progress by remember { mutableFloatStateOf(0f) }

    val animProgress by animateFloatAsState(
        targetValue    = progress,
        animationSpec  = tween(500),
        label          = "progress"
    )
    val logoAlpha by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = tween(700),
        label         = "alpha"
    )
    val logoScale by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 180f),
        label         = "scale"
    )

    val inf = rememberInfiniteTransition(label = "rings")
    val r1s by inf.animateFloat(1f, 1.55f, infiniteRepeatable(tween(1300), RepeatMode.Restart), label = "r1s")
    val r1a by inf.animateFloat(0.45f, 0f,  infiniteRepeatable(tween(1300), RepeatMode.Restart), label = "r1a")
    val r2s by inf.animateFloat(1f, 1.9f,  infiniteRepeatable(tween(1700, delayMillis = 350), RepeatMode.Restart), label = "r2s")
    val r2a by inf.animateFloat(0.25f, 0f,  infiniteRepeatable(tween(1700, delayMillis = 350), RepeatMode.Restart), label = "r2a")

    LaunchedEffect(Unit) {
        delay(300)
        phases.forEachIndexed { i, _ ->
            phase    = i
            progress = (i + 1f) / phases.size
            delay(600)
        }
        delay(400)
        onFinished()
    }

    Box(
        modifier          = Modifier.fillMaxSize().background(Background),
        contentAlignment  = Alignment.Center
    ) {
        Box(Modifier.size(260.dp).scale(r2s).alpha(r2a).clip(RoundedCornerShape(130.dp)).background(Primary.copy(0.04f)))
        Box(Modifier.size(180.dp).scale(r1s).alpha(r1a).clip(RoundedCornerShape(90.dp)).background(Primary.copy(0.07f)))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.alpha(logoAlpha).scale(logoScale)
        ) {
            Box(
                modifier         = Modifier.size(88.dp).clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(Primary, Primary.copy(0.55f)))),
                contentAlignment = Alignment.Center
            ) {
                Text("A", fontSize = 46.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.splash_app_title),    fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = 3.sp)
            Text(stringResource(R.string.splash_app_subtitle), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Primary,    letterSpacing = 5.sp)
            Spacer(Modifier.height(48.dp))
            Text(
                if (phase < phases.size) phases[phase] else "",
                fontSize = 12.sp, color = TextSecondary, letterSpacing = 0.3.sp
            )
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier.width(220.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Surface)
            ) {
                Box(
                    Modifier.fillMaxHeight().fillMaxWidth(animProgress)
                        .clip(RoundedCornerShape(2.dp)).background(Primary)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text("${(animProgress * 100).toInt()}%", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.SemiBold)
        }
    }
}
