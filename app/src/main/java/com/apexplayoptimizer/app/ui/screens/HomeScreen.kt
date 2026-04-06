package com.apexplayoptimizer.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.apexplayoptimizer.app.R
import com.apexplayoptimizer.app.data.rememberDeviceStats
import com.apexplayoptimizer.app.ui.components.CircularGauge
import com.apexplayoptimizer.app.ui.navigation.Screen
import com.apexplayoptimizer.app.ui.theme.*
import kotlinx.coroutines.delay

private data class Feature(val icon: String, val label: String, val desc: String, val color: Color, val route: String)

private data class GamingMode(val icon: String, val labelRes: Int, val color: Color)

@Composable
fun HomeScreen(nav: NavController) {
    val features = listOf(
        Feature("🤖", stringResource(R.string.feat_ai_boost),    stringResource(R.string.feat_ai_boost_desc),    Primary, Screen.Dashboard.route),
        Feature("🎯", stringResource(R.string.feat_sensitivity), stringResource(R.string.feat_sensitivity_desc), Orange,  Screen.Sensitivity.route),
        Feature("🖥", stringResource(R.string.feat_gfx_tool),   stringResource(R.string.feat_gfx_tool_desc),   Purple,  Screen.GFXTool.route),
        Feature("📊", stringResource(R.string.feat_hud_monitor),stringResource(R.string.feat_hud_monitor_desc), Teal,    Screen.HUDMonitor.route),
        Feature("⚡", stringResource(R.string.feat_zero_lag),   stringResource(R.string.feat_zero_lag_desc),   Yellow,  Screen.ZeroLag.route),
        Feature("📈", stringResource(R.string.feat_dashboard),  stringResource(R.string.feat_dashboard_desc),  Primary, Screen.Dashboard.route),
    )
    val gamingModes = listOf(
        GamingMode("🌿", R.string.mode_power_save, Teal),
        GamingMode("⚖", R.string.mode_balanced,   Primary),
        GamingMode("🔥", R.string.mode_gamer,      Orange),
    )

    val stats by rememberDeviceStats()
    var boosting     by remember { mutableStateOf(false) }
    var boostDone    by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableIntStateOf(1) }

    val pingDisplay = if (stats.ping < 0) "—" else "${stats.ping}"
    val pingColor   = if (stats.ping < 0) TextMuted else if (stats.ping > 80) Danger else if (stats.ping > 40) Orange else Primary
    val ramColor    = if (stats.ramUsed > 80) Danger else if (stats.ramUsed > 60) Orange else Primary
    val tempColor   = if (stats.temperature > 48) Danger else if (stats.temperature > 38) Orange else Primary
    val cpuColor    = if (stats.cpuLoad > 75) Danger else if (stats.cpuLoad > 50) Orange else Primary
    val netSpeed    = stats.networkDown + stats.networkUp

    val pingScore  = if (stats.ping < 0) 0 else (100 - (stats.ping * 0.5f).toInt()).coerceIn(0, 40)
    val perfScore  = (pingScore + (100 - stats.ramUsed * 0.35f) + (100 - stats.temperature * 1.2f))
        .toInt().coerceIn(0, 100)
    val perfColor  = if (perfScore > 75) Primary else if (perfScore > 50) Orange else Danger

    LaunchedEffect(boosting) {
        if (boosting) { delay(3_200); boosting = false; boostDone = true; delay(4_000); boostDone = false }
    }

    Column(Modifier.fillMaxSize().background(Background)) {

        // ── Header bar ──────────────────────────────────────────────────────
        Box(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Surface, Background)))
        ) {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(Primary, Primary.copy(0.6f)))),
                        Alignment.Center
                    ) {
                        Text("A", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            stringResource(R.string.home_app_title),
                            fontSize = 17.sp, fontWeight = FontWeight.Black,
                            color = TextPrimary, letterSpacing = 1.sp
                        )
                        Text(
                            stringResource(R.string.home_app_subtitle),
                            fontSize = 10.sp, fontWeight = FontWeight.Medium,
                            color = Primary, letterSpacing = 2.sp
                        )
                    }
                }
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                        .background(Card)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .clickable { nav.navigate(Screen.Settings.route) },
                    Alignment.Center
                ) { Text("⚙", fontSize = 18.sp) }
            }
        }

        // ── Scrollable body ──────────────────────────────────────────────────
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 88.dp)) {

            // ── TurboSpeed-style gauge dashboard ────────────────────────────
            Box(
                Modifier.fillMaxWidth().padding(12.dp, 10.dp, 12.dp, 6.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.verticalGradient(listOf(Surface, Card)))
                    .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            ) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ── Left: Gaming mode selector ───────────────────────
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(72.dp)
                        ) {
                            gamingModes.forEachIndexed { i, mode ->
                                val sel = selectedMode == i
                                val modeLabel = stringResource(mode.labelRes)
                                Box(
                                    Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (sel) mode.color.copy(0.18f)
                                            else Card
                                        )
                                        .border(
                                            if (sel) 1.5.dp else 1.dp,
                                            if (sel) mode.color.copy(0.7f) else CardBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedMode = i }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(mode.icon, fontSize = 18.sp)
                                        Text(
                                            modeLabel,
                                            fontSize = 8.sp,
                                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sel) mode.color else TextMuted,
                                            letterSpacing = 0.2.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.width(10.dp))

                        // ── Center: Large CPU gauge ─────────────────────────
                        Box(
                            Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularGauge(
                                value        = stats.cpuLoad.toFloat(),
                                label        = stringResource(R.string.label_cpu),
                                color        = cpuColor,
                                gaugeSize    = 148.dp,
                                strokeWidth  = 13.dp
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        // ── Right: RAM + Speed gauges ───────────────────────
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(90.dp)
                        ) {
                            CircularGauge(
                                value        = stats.ramUsed,
                                label        = stringResource(R.string.label_ram),
                                color        = ramColor,
                                gaugeSize    = 86.dp,
                                strokeWidth  = 9.dp
                            )
                            CircularGauge(
                                value        = (netSpeed / 20f * 100f).coerceIn(0f, 100f),
                                label        = stringResource(R.string.label_speed),
                                displayValue = "${"%.1f".format(netSpeed)}",
                                unit         = "MB/s",
                                color        = Blue,
                                gaugeSize    = 86.dp,
                                strokeWidth  = 9.dp
                            )
                        }
                    }

                    // ── Mini stats strip ─────────────────────────────────────
                    Row(
                        Modifier.fillMaxWidth()
                            .background(Card)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MiniStat("🌡", stringResource(R.string.label_temp), "${"%.0f".format(stats.temperature)}°C", tempColor)
                        Box(Modifier.width(1.dp).height(28.dp).background(DividerColor))
                        MiniStat("⚡", stringResource(R.string.label_cpu),  "${stats.cpuLoad}%",  cpuColor)
                        Box(Modifier.width(1.dp).height(28.dp).background(DividerColor))
                        MiniStat("🧠", stringResource(R.string.label_ram),  "${stats.ramUsed.toInt()}%", ramColor)
                        Box(Modifier.width(1.dp).height(28.dp).background(DividerColor))
                        MiniStat("📡", stringResource(R.string.label_ping), if (stats.ping < 0) "—" else "${stats.ping}ms", pingColor)
                    }
                }
            }

            // ── Boost button ─────────────────────────────────────────────────
            val bScale by animateFloatAsState(if (boosting) 1.04f else 1f, spring(0.4f, 280f), label = "bs")
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .scale(bScale)
                    .background(
                        if (boostDone) Brush.horizontalGradient(listOf(Primary.copy(0.7f), Primary))
                        else if (boosting) Brush.horizontalGradient(listOf(Orange.copy(0.7f), Orange))
                        else Brush.horizontalGradient(listOf(Primary.copy(0.85f), Blue))
                    )
                    .clickable { if (!boosting) { boosting = true; boostDone = false } }
                    .padding(vertical = 16.dp),
                Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        if (boosting) "⚡" else if (boostDone) "✅" else "🚀",
                        fontSize = 22.sp
                    )
                    Column {
                        Text(
                            if (boosting) stringResource(R.string.boost_optimizing)
                            else if (boostDone) stringResource(R.string.boost_game_ready)
                            else stringResource(R.string.boost_quick_boost),
                            fontSize = 15.sp, fontWeight = FontWeight.Black,
                            color = Color.White, letterSpacing = 1.sp
                        )
                        Text(
                            if (boosting) stringResource(R.string.boost_cleaning_tuning)
                            else if (boostDone) stringResource(R.string.boost_all_optimal)
                            else stringResource(R.string.boost_one_tap),
                            fontSize = 10.sp, color = Color.White.copy(0.75f)
                        )
                    }
                }
            }

            // ── Performance score pill ───────────────────────────────────────
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                        .background(perfColor.copy(0.08f))
                        .border(1.dp, perfColor.copy(0.25f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("$perfScore", fontSize = 26.sp, fontWeight = FontWeight.Black, color = perfColor)
                        Column {
                            Text(
                                stringResource(R.string.boost_perf_score),
                                fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                                color = TextMuted, letterSpacing = 1.sp
                            )
                            Text(
                                if (perfScore > 75) stringResource(R.string.status_all_optimal)
                                else if (perfScore > 50) stringResource(R.string.status_needs_optimization)
                                else stringResource(R.string.status_critical_boost),
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = perfColor
                            )
                        }
                    }
                }
                Box(
                    Modifier.clip(RoundedCornerShape(12.dp))
                        .background(Card).border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .clickable { nav.navigate(Screen.HUDMonitor.route) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊", fontSize = 22.sp)
                        Text(stringResource(R.string.feat_hud_monitor), fontSize = 9.sp, color = Teal, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Feature grid ────────────────────────────────────────────────
            SectionHeader(stringResource(R.string.section_quick_access))
            Column(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                features.chunked(2).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { f -> FeatureCard(f, Modifier.weight(1f)) { nav.navigate(f.route) } }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            // ── Pro tip ─────────────────────────────────────────────────────
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Yellow.copy(0.06f))
                    .border(1.dp, Yellow.copy(0.22f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(Yellow.copy(0.12f)),
                        Alignment.Center
                    ) { Text("💡", fontSize = 18.sp) }
                    Column {
                        Text(
                            stringResource(R.string.pro_tip_header),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Yellow
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.pro_tip_body),
                            fontSize = 12.sp, color = TextSecondary, lineHeight = 19.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun StatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(0.07f))
            .border(1.dp, color.copy(0.25f), RoundedCornerShape(10.dp))
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = color)
        Text(label, fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp)
    }
}

@Composable
fun MiniStat(icon: String, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(icon, fontSize = 11.sp)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = color)
        }
        Text(label, fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.5.sp)
        Box(Modifier.weight(1f).height(1.dp).background(CardBorder))
    }
}

@Composable
private fun FeatureCard(item: Feature, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                    .background(item.color.copy(0.12f))
                    .border(1.dp, item.color.copy(0.25f), RoundedCornerShape(14.dp)),
                Alignment.Center
            ) { Text(item.icon, fontSize = 22.sp) }
            Spacer(Modifier.height(12.dp))
            Text(item.label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(3.dp))
            Text(item.desc, fontSize = 11.sp, color = TextMuted, lineHeight = 15.sp)
        }
        Box(
            Modifier.align(Alignment.TopEnd).size(22.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(item.color.copy(0.1f)),
            Alignment.Center
        ) { Text("›", fontSize = 16.sp, color = item.color, fontWeight = FontWeight.Bold) }
    }
}
