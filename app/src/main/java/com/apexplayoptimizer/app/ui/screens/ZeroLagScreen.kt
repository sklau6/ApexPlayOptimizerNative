package com.apexplayoptimizer.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.apexplayoptimizer.app.R
import com.apexplayoptimizer.app.data.BoostResult
import android.app.Activity
import com.apexplayoptimizer.app.data.DeviceOptimizer
import com.apexplayoptimizer.app.data.RewardedInterstitialAdManager
import com.apexplayoptimizer.app.data.OptimizeMode
import com.apexplayoptimizer.app.data.rememberDeviceStats
import androidx.compose.ui.platform.LocalContext
import com.apexplayoptimizer.app.ui.theme.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

private data class ZeroLagMode(
    val id: String, val name: String, val icon: String,
    val description: String, val features: List<String>,
    val color: Color, val recommended: Boolean = false
)

@Composable
fun ZeroLagScreen(nav: NavController) {
    val modes = listOf(
        ZeroLagMode("balanced", stringResource(R.string.mode_balanced), "⚖",
            stringResource(R.string.mode_balanced_desc),
            listOf(stringResource(R.string.mode_balanced_f1), stringResource(R.string.mode_balanced_f2), stringResource(R.string.mode_balanced_f3), stringResource(R.string.mode_balanced_f4)),
            Blue),
        ZeroLagMode("performance", stringResource(R.string.mode_performance), "🚀",
            stringResource(R.string.mode_performance_desc),
            listOf(stringResource(R.string.mode_performance_f1), stringResource(R.string.mode_performance_f2), stringResource(R.string.mode_performance_f3), stringResource(R.string.mode_performance_f4), stringResource(R.string.mode_performance_f5)),
            Primary, recommended = true),
        ZeroLagMode("extreme", stringResource(R.string.mode_extreme), "⚡",
            stringResource(R.string.mode_extreme_desc),
            listOf(stringResource(R.string.mode_extreme_f1), stringResource(R.string.mode_extreme_f2), stringResource(R.string.mode_extreme_f3), stringResource(R.string.mode_extreme_f4), stringResource(R.string.mode_extreme_f5)),
            Danger),
    )
    val ctx             = LocalContext.current
    val stats by rememberDeviceStats()
    var selectedMode     by remember { mutableStateOf("performance") }
    var isOptimizing     by remember { mutableStateOf(false) }
    var optimizationDone by remember { mutableStateOf(false) }
    var progress         by remember { mutableFloatStateOf(0f) }
    var boostResult      by remember { mutableStateOf<BoostResult?>(null) }

    val current = modes.first { it.id == selectedMode }

    val animProgress by animateFloatAsState(progress, tween(400), label = "prog")
    val rocketScale  by animateFloatAsState(if (optimizationDone) 1.2f else 1f, spring(0.5f, 200f), label = "rs")
    val glowAlpha    by animateFloatAsState(if (optimizationDone) 0.8f else if (isOptimizing) 0.5f else 0.2f, tween(400), label = "ga")

    val inf = rememberInfiniteTransition(label = "glow")
    val glowPulse by inf.animateFloat(0.3f, 0.8f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "gp")

    LaunchedEffect(isOptimizing) {
        if (isOptimizing) {
            progress = 0f
            val mode = when (selectedMode) {
                "balanced" -> OptimizeMode.BALANCED
                "extreme"  -> OptimizeMode.GAMER
                else       -> OptimizeMode.GAMER   // performance
            }
            coroutineScope {
                // Animate progress bar in parallel with real work
                val anim = async {
                    var p = 0
                    while (p < 90) {
                        delay(120)
                        p = (p + 4).coerceAtMost(90)
                        progress = p / 100f
                    }
                }
                boostResult = DeviceOptimizer.runBoost(ctx, mode)
                anim.cancel()
            }
            progress = 1f
            delay(150)
            isOptimizing     = false
            optimizationDone = true
            // Show rewarded interstitial after optimization
            (ctx as? Activity)?.let { RewardedInterstitialAdManager.show(it) }
        }
    }

    Column(Modifier.fillMaxSize().background(Background)) {
        // Header
        Row(
            Modifier.fillMaxWidth().background(Surface).statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(Card)
                .border(1.dp, CardBorder, RoundedCornerShape(18.dp)).clickable { nav.popBackStack() }, Alignment.Center
            ) { Text("←", fontSize = 18.sp, color = TextPrimary, fontWeight = FontWeight.Bold) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.zero_lag_title),    fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = 2.sp)
                Text(stringResource(R.string.zero_lag_subtitle), fontSize = 10.sp, color = Primary, letterSpacing = 1.sp)
            }
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(Card)
                .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                .clickable { nav.navigate("settings") }, Alignment.Center
            ) { Text("⚙", fontSize = 16.sp) }
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = 72.dp)) {

            // Rocket area
            Box(Modifier.fillMaxWidth().padding(vertical = 28.dp), Alignment.Center) {
                Box(
                    Modifier.size(130.dp).alpha(if (isOptimizing) glowPulse else glowAlpha)
                        .clip(RoundedCornerShape(65.dp)).background(current.color.copy(0.08f))
                        .border(1.dp, current.color.copy(0.15f), RoundedCornerShape(65.dp))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (optimizationDone) "🚀" else "🎮", fontSize = 60.sp, modifier = Modifier.scale(rocketScale))
                    if (optimizationDone) {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            Modifier.clip(RoundedCornerShape(20.dp)).background(current.color)
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                        ) { Text(stringResource(R.string.label_optimized), fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black, letterSpacing = 1.sp) }
                    }
                }
            }

            // Mode selector
            Text(stringResource(R.string.select_boost_mode), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = TextMuted, letterSpacing = 1.5.sp, modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).padding(bottom = 10.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                modes.forEach { mode ->
                    val sel = selectedMode == mode.id
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(if (sel) mode.color.copy(0.1f) else Card)
                            .border(1.dp, if (sel) mode.color.copy(0.6f) else CardBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedMode = mode.id }.padding(10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(mode.icon, fontSize = 20.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(mode.name, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = if (sel) mode.color else TextSecondary, letterSpacing = 0.3.sp)
                        }
                        if (mode.recommended) {
                            Box(
                                Modifier.align(Alignment.TopEnd).size(16.dp)
                                    .clip(RoundedCornerShape(8.dp)).background(Primary), Alignment.Center
                            ) { Text("✓", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Mode detail card
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(14.dp)).background(Surface)
                    .border(1.dp, current.color.copy(0.22f), RoundedCornerShape(14.dp)).padding(16.dp)
            ) {
                Column {
                    Row(Modifier.padding(bottom = 14.dp), verticalAlignment = Alignment.Top) {
                        Text(current.icon, fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(current.name, fontSize = 15.sp, fontWeight = FontWeight.Black, color = current.color, letterSpacing = 1.sp)
                            Text(current.description, fontSize = 11.sp, color = TextMuted, lineHeight = 16.sp)
                        }
                    }
                    current.features.forEach { feature ->
                        Row(Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(18.dp).clip(RoundedCornerShape(9.dp)).background(current.color), Alignment.Center) {
                                Text("✓", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(feature, fontSize = 13.sp, color = TextPrimary)
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            // Progress card
            if (isOptimizing || optimizationDone) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(14.dp)).background(Surface)
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp)).padding(16.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text(if (isOptimizing) stringResource(R.string.optimizing_progress) else stringResource(R.string.optimization_complete),
                            fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text("${(animProgress * 100).toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = current.color)
                    }
                    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(GaugeTrack)) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(animProgress).clip(RoundedCornerShape(3.dp)).background(current.color))
                    }
                    if (optimizationDone) {
                        Spacer(Modifier.height(14.dp))
                        val r = boostResult
                        listOf(
                            "🧹" to stringResource(R.string.result_ram_freed)   to if (r != null && r.ramFreedMb > 0) "${r.ramFreedMb} MB" else "Done",
                            "⚡" to stringResource(R.string.result_cpu_optimized) to if (r != null) "${r.killedProcesses} apps" else stringResource(R.string.result_done),
                            "🎯" to stringResource(R.string.result_fps_mode)      to if (r?.wakeLockActive == true) "Unlocked" else "Ready",
                            "🔕" to "Do Not Disturb"                              to if (r?.dndEnabled == true) "ON" else "Skipped",
                        ).forEach { (pair, value) ->
                            val (icon, label) = pair
                            Box(Modifier.fillMaxWidth().padding(vertical = 6.dp).border(1.dp, DividerColor, RoundedCornerShape(0.dp))) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(icon, fontSize = 14.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Text(label, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                                    Text(value, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = current.color)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // Current stats
            SectionHeader(stringResource(R.string.section_current_status))
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "🧠" to stringResource(R.string.status_ram)       to "${stats.ramUsed.toInt()}%"                                          to (stats.ramUsed > 75),
                    "⚡" to stringResource(R.string.status_cpu)       to "${stats.cpuLoad}%"                                                   to (stats.cpuLoad > 70),
                    "🌡" to stringResource(R.string.status_temp_short) to "${stats.temperature.toInt()}°"                                       to (stats.temperature > 45),
                    "📡" to stringResource(R.string.status_ping)      to (if (stats.ping < 0) "—" else "${stats.ping}ms")                       to (stats.ping > 80),
                ).forEach { (triple, urgent) ->
                    val (pair, value) = triple
                    val (icon, label) = pair
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(Card)
                            .border(1.dp, if (urgent) Warning.copy(0.3f) else CardBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(icon,  fontSize = 16.sp)
                        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (urgent) Warning else current.color)
                        Text(label, fontSize = 9.sp,  color = TextMuted, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Optimize button
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedMode == "extreme") Danger else current.color)
                    .clickable { if (!isOptimizing) { optimizationDone = false; isOptimizing = true } }
                    .padding(vertical = 16.dp),
                Alignment.Center
            ) {
                Text(
                    if (isOptimizing) stringResource(R.string.btn_optimizing) else if (optimizationDone) stringResource(R.string.btn_re_optimize) else stringResource(R.string.btn_activate_zero_lag),
                    fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black, letterSpacing = 1.5.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            if (!isOptimizing && !optimizationDone) {
                Text(
                    stringResource(R.string.zero_lag_hint, current.name),
                    fontSize = 11.sp, color = TextMuted, modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
            Spacer(Modifier.height(12.dp))

            // Guarantee badges
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("🛡" to stringResource(R.string.badge_safe), "🔥" to stringResource(R.string.badge_no_heating), "⚡" to stringResource(R.string.badge_max_fps), "✅" to stringResource(R.string.badge_guaranteed))
                    .forEach { (icon, text) ->
                        Column(
                            Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Card)
                                .border(1.dp, CardBorder, RoundedCornerShape(10.dp)).padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(icon, fontSize = 18.sp)
                            Text(text, fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp)
                        }
                    }
            }
        }
    }
}
