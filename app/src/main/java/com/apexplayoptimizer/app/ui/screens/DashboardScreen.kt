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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.apexplayoptimizer.app.R
import com.apexplayoptimizer.app.data.rememberDeviceStats
import com.apexplayoptimizer.app.ui.navigation.Screen
import com.apexplayoptimizer.app.ui.theme.*
import kotlinx.coroutines.delay

private val GFX_GRAPHICS   = listOf("Super Smooth","Smooth","Balanced","HD","HDR","Ultra HD")
private val GFX_FRAMERATE  = listOf("30Hz","60Hz","90Hz","120Hz","144Hz")
private val GFX_RESOLUTION = listOf("720p","1080p","1440p","1920p","2K","2560p")
private val GFX_STYLE      = listOf("Default","Colorful","Realistic","Cinematic")

@Composable
fun DashboardScreen(nav: NavController) {
    val sensLabels = listOf(
        stringResource(R.string.scope_general),
        stringResource(R.string.scope_red_dot),
        stringResource(R.string.scope_2x),
        stringResource(R.string.scope_3x),
        stringResource(R.string.scope_4x_acog),
        stringResource(R.string.scope_6x),
        stringResource(R.string.scope_8x),
    )
    val stats by rememberDeviceStats()
    var tab   by remember { mutableIntStateOf(0) }
    var showAI by remember { mutableStateOf(false) }
    var monster by remember { mutableStateOf(false) }

    var gfxGraphics   by remember { mutableStateOf("HD") }
    var gfxFramerate  by remember { mutableStateOf("60Hz") }
    var gfxResolution by remember { mutableStateOf("1080p") }
    var gfxStyle      by remember { mutableStateOf("Default") }

    var sensVals  by remember { mutableStateOf(listOf(86, 91, 84, 93, 76, 68, 55)) }
    var activated by remember { mutableStateOf(false) }

    LaunchedEffect(activated) {
        if (activated) { delay(2500); activated = false }
    }

    val ramColor  = if (stats.ramUsed > 80) Danger else if (stats.ramUsed > 60) Orange else Primary
    val tempColor = if (stats.temperature > 48) Danger else if (stats.temperature > 38) Orange else Primary
    val pingColor = if (stats.ping < 0) TextMuted else if (stats.ping > 80) Danger else if (stats.ping > 40) Orange else Primary
    val pingDisplay = if (stats.ping < 0) "—" else "${stats.ping}ms"

    Box(Modifier.fillMaxSize().background(Background)) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Box(Modifier.fillMaxWidth().background(Surface).statusBarsPadding().padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(stringResource(R.string.dashboard_title), fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = 3.sp)
                        Text(stringResource(R.string.dashboard_subtitle, stats.cpuLoad), fontSize = 11.sp, color = TextMuted)
                    }
                    Box(
                        Modifier.size(36.dp).clip(RoundedCornerShape(18.dp))
                            .background(Card).border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                            .clickable { nav.navigate(Screen.Settings.route) },
                        Alignment.Center
                    ) { Text("⚙", fontSize = 16.sp) }
                }
            }

            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 88.dp)) {

                // Stats card
                Column(
                    Modifier.fillMaxWidth().padding(12.dp)
                        .clip(RoundedCornerShape(16.dp)).background(Surface)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp)).padding(16.dp)
                ) {
                    StatBar(stringResource(R.string.label_ram),         "${stats.ramUsed.format1}% of ${stats.ramTotal}GB",   stats.ramUsed,                        100f,                         ramColor)
                    StatBar(stringResource(R.string.label_storage),     "${stats.storageUsed.format2}GB / ${stats.storageTotal}GB", stats.storageUsed,              stats.storageTotal.toFloat(),  Purple)
                    StatBar(stringResource(R.string.label_temperature), "${stats.temperature.format1}°C",                       stats.temperature,                    60f,                          tempColor)
                    StatBar(stringResource(R.string.label_ping),        pingDisplay,                                            stats.ping.coerceAtLeast(0).toFloat(), 200f,                         pingColor)
                }

                // AI Boost + Monster
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                            .background(Card).border(1.dp, Orange.copy(0.3f), RoundedCornerShape(12.dp))
                            .clickable { showAI = true }.padding(vertical = 13.dp),
                        Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🤖", fontSize = 16.sp)
                            Text(stringResource(R.string.label_ai_boost), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        }
                    }
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                            .background(if (monster) Orange.copy(0.1f) else Card)
                            .border(1.dp, if (monster) Orange.copy(0.45f) else CardBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                monster = !monster
                                if (monster) { gfxGraphics = "Ultra HD"; gfxFramerate = "144Hz"; gfxResolution = "2K" }
                            }.padding(vertical = 13.dp),
                        Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💀", fontSize = 16.sp)
                            Text(if (monster) stringResource(R.string.label_monster_on) else stringResource(R.string.label_monster_mode), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = if (monster) Orange else TextPrimary)
                        }
                    }
                }

                // Tab bar
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp)).background(Surface)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                ) {
                    listOf(stringResource(R.string.tab_sensitivity), stringResource(R.string.tab_gfx_tool)).forEachIndexed { i, t ->
                        Column(
                            Modifier.weight(1f).clickable { tab = i }.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(t, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (tab == i) Orange else TextMuted, letterSpacing = 0.5.sp)
                            if (tab == i) {
                                Spacer(Modifier.height(4.dp))
                                Box(Modifier.width(40.dp).height(2.dp).clip(RoundedCornerShape(1.dp)).background(Orange))
                            }
                        }
                    }
                }

                // Tab content
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(top = 2.dp)
                        .clip(RoundedCornerShape(12.dp)).background(Surface)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp)).padding(top = 4.dp)
                ) {
                    if (tab == 0) {
                        Column {
                            sensVals.forEachIndexed { i, v ->
                                SensRow(sensLabels[i], v, Orange) { newVal -> sensVals = sensVals.toMutableList().also { list -> list[i] = newVal } }
                            }
                            Box(
                                Modifier.fillMaxWidth().padding(12.dp, 4.dp, 12.dp, 8.dp)
                                    .clip(RoundedCornerShape(12.dp)).background(Card)
                                    .border(1.dp, Orange.copy(0.25f), RoundedCornerShape(12.dp))
                                    .clickable { nav.navigate(Screen.Sensitivity.route) }.padding(12.dp)
                            ) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("🏆", fontSize = 14.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.btn_view_presets), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Orange, modifier = Modifier.weight(1f))
                                    Text("›", fontSize = 18.sp, color = Orange)
                                }
                            }
                            Box(
                                Modifier.fillMaxWidth().padding(12.dp).clip(RoundedCornerShape(12.dp))
                                    .background(if (activated) Primary else Orange)
                                    .clickable { activated = true }.padding(vertical = 14.dp),
                                Alignment.Center
                            ) {
                                Text(if (activated) stringResource(R.string.btn_activated) else stringResource(R.string.btn_activate), fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black, letterSpacing = 2.sp)
                            }
                        }
                    } else {
                        Column(Modifier.padding(bottom = 4.dp)) {
                            SimpleDropdown(stringResource(R.string.dropdown_graphics),   GFX_GRAPHICS,   gfxGraphics)   { gfxGraphics = it }
                            SimpleDropdown(stringResource(R.string.dropdown_framerate),  GFX_FRAMERATE,  gfxFramerate)  { gfxFramerate = it }
                            SimpleDropdown(stringResource(R.string.dropdown_resolution), GFX_RESOLUTION, gfxResolution) { gfxResolution = it }
                            SimpleDropdown(stringResource(R.string.dropdown_style),      GFX_STYLE,      gfxStyle)      { gfxStyle = it }
                            Box(
                                Modifier.fillMaxWidth().padding(12.dp).clip(RoundedCornerShape(12.dp))
                                    .background(Card).border(1.dp, Orange.copy(0.25f), RoundedCornerShape(12.dp))
                                    .clickable { nav.navigate(Screen.GFXTool.route) }.padding(vertical = 12.dp),
                                Alignment.Center
                            ) { Text(stringResource(R.string.btn_open_gfx), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Orange, letterSpacing = 0.5.sp) }
                        }
                    }
                }

                // Network row
                Row(
                    Modifier.fillMaxWidth().padding(12.dp, 10.dp, 12.dp, 0.dp)
                        .clip(RoundedCornerShape(14.dp)).background(Surface)
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp)).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("⬇" to "${"%.1f".format(stats.networkDown)} MB/s" to stringResource(R.string.label_download),
                        "⬆" to "${"%.1f".format(stats.networkUp)} MB/s" to stringResource(R.string.label_upload),
                        "📡" to pingDisplay to stringResource(R.string.label_latency)
                    ).forEachIndexed { i, (pair, label) ->
                        val (icon, value) = pair
                        if (i > 0) Box(Modifier.width(1.dp).height(40.dp).background(DividerColor))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(icon, fontSize = 16.sp)
                            Text(value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = if (i == 2) pingColor else TextPrimary)
                            Text(label, fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                        }
                    }
                }
            }
        }

        // AI Boost overlay
        if (showAI) AIBoostOverlay(stats.ramUsed, stats.cpuLoad, stats.temperature.toInt()) { showAI = false }
    }
}

@Composable
private fun StatBar(label: String, displayVal: String, num: Float, max: Float, color: Color) {
    val pct = (num / max).coerceIn(0f, 1f)
    val anim by animateFloatAsState(pct, tween(700), label = "bar")
    Column(Modifier.padding(bottom = 10.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("$label: ", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(displayVal, fontSize = 13.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(5.dp))
        Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(GaugeTrack)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(anim).clip(RoundedCornerShape(3.dp)).background(color))
        }
    }
}

@Composable
private fun SensRow(label: String, value: Int, color: Color, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.width(80.dp))
        Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(SliderTrack).clickable {}) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(value / 100f).clip(RoundedCornerShape(2.dp)).background(color))
        }
        Spacer(Modifier.width(10.dp))
        Text("$value", fontSize = 13.sp, fontWeight = FontWeight.Black, color = color, modifier = Modifier.width(30.dp))
    }
}

@Composable
private fun SimpleDropdown(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.padding(horizontal = 12.dp).padding(top = 8.dp)) {
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp)).clickable { expanded = !expanded }.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = TextMuted, letterSpacing = 1.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(selected, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Orange)
                Text(if (expanded) "▲" else "▼", fontSize = 10.sp, color = Orange)
            }
        }
        if (expanded) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Surface).border(1.dp, CardBorder, RoundedCornerShape(10.dp))) {
                options.forEach { opt ->
                    Box(Modifier.fillMaxWidth().background(if (opt == selected) Orange.copy(0.08f) else Color.Transparent)
                        .clickable { onSelect(opt); expanded = false }.padding(12.dp, 10.dp)) {
                        Text(opt, fontSize = 13.sp, color = if (opt == selected) Orange else TextSecondary, fontWeight = if (opt == selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
private fun AIBoostOverlay(ram: Float, cpu: Int, temp: Int, onClose: () -> Unit) {
    var phase by remember { mutableIntStateOf(0) }
    val prog by animateFloatAsState(phase / 5f * 100f, tween(600), label = "prog")
    val phases = listOf(
        stringResource(R.string.ai_phase_1),
        stringResource(R.string.ai_phase_2),
        stringResource(R.string.ai_phase_3),
        stringResource(R.string.ai_phase_4),
        stringResource(R.string.ai_phase_5),
        stringResource(R.string.ai_phase_6),
    )
    LaunchedEffect(Unit) { repeat(6) { delay(700); phase = it + 1 } }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.88f)).clickable {}, Alignment.Center) {
        Column(
            Modifier.fillMaxWidth(0.88f).clip(RoundedCornerShape(20.dp))
                .background(Surface).border(1.dp, Orange.copy(0.3f), RoundedCornerShape(20.dp)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.ai_boost_title), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Orange, letterSpacing = 1.sp)
            Spacer(Modifier.height(16.dp))
            Text(phases[phase.coerceAtMost(5)], fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(GaugeTrack)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(prog / 100f).clip(RoundedCornerShape(3.dp)).background(Orange))
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
                listOf(stringResource(R.string.ai_boost_ram_free) to "${(100 - ram).toInt()}%" to Primary, stringResource(R.string.ai_boost_cpu) to "$cpu%" to Blue, stringResource(R.string.ai_boost_temp) to "$temp°" to Orange)
                    .forEach { (pair, color) ->
                        val (label, value) = pair
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
                            Text(label, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                        }
                    }
            }
            if (phase >= 6) {
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Orange).clickable { onClose() }.padding(vertical = 14.dp), Alignment.Center) {
                    Text(stringResource(R.string.ai_boost_done), fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.Black, letterSpacing = 1.5.sp)
                }
            }
        }
    }
}

// Extension helpers
private val Float.format1 get() = "%.1f".format(this)
private val Float.format2 get() = "%.2f".format(this)
