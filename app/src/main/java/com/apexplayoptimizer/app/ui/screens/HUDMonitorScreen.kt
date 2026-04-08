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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.apexplayoptimizer.app.R
import com.apexplayoptimizer.app.data.rememberDeviceStats
import com.apexplayoptimizer.app.ui.components.CircularGauge
import com.apexplayoptimizer.app.ui.theme.*

@Composable
fun HUDMonitorScreen(nav: NavController) {
    val stats by rememberDeviceStats()
    var isLive by remember { mutableStateOf(true) }

    val tempColor   = if (stats.temperature > 48) Danger else if (stats.temperature > 38) Orange else Primary
    val pingColor   = if (stats.ping < 0) TextMuted else if (stats.ping > 80) Danger else if (stats.ping > 40) Orange else Primary
    val ramColor    = if (stats.ramUsed > 80) Danger else if (stats.ramUsed > 60) Orange else Primary
    val cpuColor    = if (stats.cpuLoad > 75) Danger else if (stats.cpuLoad > 50) Orange else Primary
    val pingDisplay = if (stats.ping < 0) "-" else "${stats.ping}"

    val inf = rememberInfiniteTransition(label = "pulse")
    val pulseDot by inf.animateFloat(1f, 1.5f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "dot")

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
                Text(stringResource(R.string.hud_title), fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = 3.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.size(7.dp).scale(if (isLive) pulseDot else 1f).clip(RoundedCornerShape(4.dp)).background(if (isLive) Primary else TextMuted))
                    Text(if (isLive) stringResource(R.string.hud_live) else stringResource(R.string.hud_paused), fontSize = 10.sp, color = if (isLive) Primary else TextMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(Card)
                .border(1.dp, if (isLive) Primary.copy(0.4f) else CardBorder, RoundedCornerShape(18.dp))
                .clickable { isLive = !isLive }, Alignment.Center
            ) { Text(if (isLive) "⏸" else "▶", fontSize = 14.sp) }
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = 72.dp)) {

            // ── Circular gauge panel (TurboSpeed style) ─────────────────────
            Box(
                Modifier.fillMaxWidth().padding(12.dp, 12.dp, 12.dp, 6.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.verticalGradient(listOf(Surface, Card)))
                    .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            ) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Large RAM gauge
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            CircularGauge(
                                value        = stats.ramUsed,
                                label        = stringResource(R.string.hud_memory),
                                color        = ramColor,
                                gaugeSize    = 130.dp,
                                strokeWidth  = 12.dp
                            )
                        }
                        // Large CPU gauge
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            CircularGauge(
                                value        = stats.cpuLoad.toFloat(),
                                label        = stringResource(R.string.hud_cpu_load),
                                color        = cpuColor,
                                gaugeSize    = 130.dp,
                                strokeWidth  = 12.dp
                            )
                        }
                        // Right column: Temp + Ping gauges
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(80.dp)
                        ) {
                            CircularGauge(
                                value        = (stats.temperature / 60f * 100f).coerceIn(0f, 100f),
                                label        = stringResource(R.string.hud_temp),
                                displayValue = "${stats.temperature.toInt()}°",
                                unit         = "°C",
                                color        = tempColor,
                                gaugeSize    = 76.dp,
                                strokeWidth  = 8.dp
                            )
                            CircularGauge(
                                value        = if (stats.ping < 0) 0f else (stats.ping / 200f * 100f).coerceIn(0f, 100f),
                                label        = stringResource(R.string.hud_latency),
                                displayValue = pingDisplay,
                                unit         = "ms",
                                color        = pingColor,
                                gaugeSize    = 76.dp,
                                strokeWidth  = 8.dp
                            )
                        }
                    }
                    // Stats strip
                    Row(
                        Modifier.fillMaxWidth().background(Card)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MiniStat("🧠", stringResource(R.string.label_ram),  "${stats.ramUsed.toInt()}%",                   ramColor)
                        Box(Modifier.width(1.dp).height(24.dp).background(DividerColor))
                        MiniStat("⚡",  stringResource(R.string.label_cpu),  "${stats.cpuLoad}%",                          cpuColor)
                        Box(Modifier.width(1.dp).height(24.dp).background(DividerColor))
                        MiniStat("🌡", stringResource(R.string.label_temp), "${stats.temperature.toInt()}°C",              tempColor)
                        Box(Modifier.width(1.dp).height(24.dp).background(DividerColor))
                        MiniStat("📡", stringResource(R.string.label_ping), if (stats.ping < 0) "—" else "${stats.ping}ms", pingColor)
                    }
                }
            }

            // Detailed metric bars
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(14.dp)).background(Surface)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp)).padding(16.dp)
            ) {
                Text(stringResource(R.string.hud_detailed_metrics), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 14.dp))
                HUDBar(stringResource(R.string.metric_memory_usage), stats.ramUsed,                  100f,             ramColor,  "%")
                HUDBar(stringResource(R.string.metric_cpu_load),     stats.cpuLoad.toFloat(),         100f,             cpuColor,  "%")
                HUDBar(stringResource(R.string.metric_storage),      stats.storageUsed,               stats.storageTotal.toFloat(), Color(0xFF6655FF), "GB")
                HUDBar(stringResource(R.string.metric_temperature),  stats.temperature,               70f,              tempColor, "°C")
                HUDBar(stringResource(R.string.metric_network_ping), stats.ping.toFloat().coerceAtMost(200f), 200f,     pingColor, "ms")
            }

            // Network status
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(14.dp)).background(Surface)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp)).padding(16.dp)
            ) {
                Text(stringResource(R.string.hud_network_status), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 14.dp))
                Row(Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                    listOf("⬇" to "${"%.1f".format(stats.networkDown)}" to stringResource(R.string.label_mbps_down),
                        "⬆" to "${"%.1f".format(stats.networkUp)}"   to stringResource(R.string.label_mbps_up),
                        "📡" to pingDisplay                           to stringResource(R.string.label_ms_ping)
                    ).forEachIndexed { i, (pair, unit) ->
                        val (icon, value) = pair
                        if (i > 0) Box(Modifier.width(1.dp).height(50.dp).background(DividerColor).align(Alignment.CenterVertically))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(icon, fontSize = 20.sp)
                            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = if (i == 2) pingColor else TextPrimary)
                            Text(unit,  fontSize = 9.sp,  color = TextMuted, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .background(pingColor.copy(0.07f)).border(1.dp, pingColor.copy(0.25f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(pingColor))
                    val connQuality = when {
                        stats.ping < 30  -> stringResource(R.string.connection_excellent)
                        stats.ping < 60  -> stringResource(R.string.connection_good)
                        stats.ping < 100 -> stringResource(R.string.connection_fair)
                        else             -> stringResource(R.string.connection_poor)
                    }
                    Text(
                        connQuality + stringResource(R.string.connection_suffix),
                        fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = pingColor, letterSpacing = 1.sp
                    )
                }
            }

            // Storage analysis
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(14.dp)).background(Surface)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp)).padding(16.dp)
            ) {
                Text(stringResource(R.string.hud_storage_analysis), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp))
                Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(GaugeTrack)) {
                    Box(
                        Modifier.fillMaxHeight()
                            .fillMaxWidth((stats.storageUsed / stats.storageTotal).coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(5.dp)).background(Orange)
                    )
                }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.storage_gb_used, stats.storageUsed),                    fontSize = 11.sp, color = Orange, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.storage_gb_free, stats.storageTotal - stats.storageUsed), fontSize = 11.sp, color = Primary, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.storage_gb_total, stats.storageTotal),                    fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                }
            }

            // Tips
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(14.dp)).background(Blue.copy(0.04f))
                    .border(1.dp, Blue.copy(0.14f), RoundedCornerShape(14.dp)).padding(14.dp)
            ) {
                Text(stringResource(R.string.hud_performance_tips), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Blue, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp))
                listOf(
                    "🧹" to stringResource(R.string.hud_tip_1),
                    "❄" to  stringResource(R.string.hud_tip_2),
                    "📶" to stringResource(R.string.hud_tip_3),
                    "🔋" to stringResource(R.string.hud_tip_4),
                ).forEach { (icon, tip) ->
                    Row(Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.Top) {
                        Text(icon, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp, top = 1.dp))
                        Text(tip, fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HUDCard(icon: String, title: String, value: String, unit: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp)).background(Card)
            .border(1.dp, color.copy(0.25f), RoundedCornerShape(12.dp)).padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(icon, fontSize = 18.sp)
            Text(title, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = color)
            Text(unit,  fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)).background(GaugeTrack))
    }
}

@Composable
private fun HUDBar(label: String, value: Float, max: Float, color: Color, unit: String) {
    val pct  = (value / max).coerceIn(0f, 1f)
    val anim by animateFloatAsState(pct, tween(600), label = "bar")
    Column(Modifier.padding(bottom = 10.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(label.uppercase(), fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("${"%.1f".format(value)}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = color)
                Text(unit, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(5.dp))
        Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(GaugeTrack)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(anim).clip(RoundedCornerShape(3.dp)).background(color))
        }
        Text("${(pct * 100).toInt()}%", fontSize = 9.sp, color = TextMuted, modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End).padding(top = 2.dp))
    }
}
