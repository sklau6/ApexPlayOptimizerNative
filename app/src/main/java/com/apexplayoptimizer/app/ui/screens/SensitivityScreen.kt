package com.apexplayoptimizer.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.apexplayoptimizer.app.R
import com.apexplayoptimizer.app.ui.theme.*
import kotlinx.coroutines.delay

private data class Preset(
    val name: String,
    val icon: String,
    val team: String,
    val vals: List<Int>,
    val color: Color
)

private val PRESETS = listOf(
    Preset("JONATHAN",    "🏆", "T1",     listOf(100, 95, 90, 85, 80, 75, 70), Primary),
    Preset("SCOUT",       "🎯", "Team IND",listOf(95, 100, 88, 82, 76, 68, 60), Orange),
    Preset("MAVI",        "⚡", "BTR",    listOf(88,  92, 86, 80, 74, 66, 58), Blue),
    Preset("MORTAL",      "🔥", "STE",    listOf(90,  88, 84, 78, 72, 64, 56), Purple),
    Preset("NEYOO",       "🚀", "GodLike",listOf(85,  90, 82, 76, 70, 62, 54), Yellow),
    Preset("ZGOD",        "💀", "GodLike",listOf(80,  86, 78, 72, 66, 60, 52), Danger),
    Preset("CUSTOM",      "🎮", "You",    listOf(86,  91, 84, 93, 76, 68, 55), TextSecondary),
)

@Composable
fun SensitivityScreen(nav: NavController) {
    val sensLabels = listOf(
        stringResource(R.string.scope_general),
        stringResource(R.string.scope_red_dot),
        stringResource(R.string.scope_2x),
        stringResource(R.string.scope_3x),
        stringResource(R.string.scope_4x_acog),
        stringResource(R.string.scope_6x),
        stringResource(R.string.scope_8x),
    )
    var selectedPreset by remember { mutableIntStateOf(0) }
    var activated      by remember { mutableStateOf(false) }
    var lagMode        by remember { mutableStateOf(true) }
    var autoCalibrate  by remember { mutableStateOf(true) }
    var haptic         by remember { mutableStateOf(false) }
    var fingerprint    by remember { mutableStateOf(true) }

    val preset = PRESETS[selectedPreset]

    LaunchedEffect(activated) { if (activated) { delay(2500); activated = false } }

    val buttonScale by animateFloatAsState(if (activated) 1.05f else 1f, spring(0.4f, 300f), label = "bs")

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
                Text(stringResource(R.string.sensitivity_title),    fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = 3.sp)
                Text(stringResource(R.string.sensitivity_subtitle), fontSize = 10.sp, color = Orange, letterSpacing = 1.sp)
            }
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(Orange.copy(0.1f))
                .border(1.dp, Orange.copy(0.3f), RoundedCornerShape(18.dp)), Alignment.Center
            ) { Text("🎯", fontSize = 16.sp) }
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 88.dp)) {

            // Preset selector (horizontal scroll)
            Text(stringResource(R.string.sensitivity_select_preset), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = TextMuted, letterSpacing = 2.sp, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 10.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PRESETS.forEachIndexed { i, p ->
                    val sel = selectedPreset == i
                    Column(
                        Modifier.width(80.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (sel) p.color.copy(0.12f) else Card)
                            .border(1.dp, if (sel) p.color.copy(0.6f) else CardBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedPreset = i }.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(p.icon, fontSize = 24.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(p.name, fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (sel) p.color else TextSecondary, letterSpacing = 0.3.sp)
                        Text(p.team, fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Selected preset detail
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(14.dp)).background(Surface)
                    .border(1.dp, preset.color.copy(0.27f), RoundedCornerShape(14.dp)).padding(16.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(preset.icon, fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(preset.name, fontSize = 14.sp, fontWeight = FontWeight.Black, color = preset.color, letterSpacing = 0.5.sp)
                        Text(preset.team, fontSize = 11.sp, color = TextMuted)
                    }
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp)).background(preset.color.copy(0.12f))
                            .border(1.dp, preset.color.copy(0.3f), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) { Text(stringResource(R.string.badge_pro), fontSize = 10.sp, fontWeight = FontWeight.Black, color = preset.color) }
                }

                sensLabels.forEachIndexed { i, label ->
                    SensSlider(label, preset.vals[i], preset.color)
                    if (i < sensLabels.size - 1) Spacer(Modifier.height(12.dp))
                }
            }

            // Toggles
            SectionHeader(stringResource(R.string.section_lag_settings))
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(14.dp)).background(Surface)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            ) {
                listOf(
                    "🌐" to stringResource(R.string.toggle_lag_reduction)  to stringResource(R.string.toggle_lag_reduction_sub)  to lagMode       to { lagMode = !lagMode },
                    "🎯" to stringResource(R.string.toggle_auto_calibrate) to stringResource(R.string.toggle_auto_calibrate_sub) to autoCalibrate to { autoCalibrate = !autoCalibrate },
                    "📳" to stringResource(R.string.toggle_haptic)          to stringResource(R.string.toggle_haptic_sub)          to haptic        to { haptic = !haptic },
                    "👆" to stringResource(R.string.toggle_fingerprint)     to stringResource(R.string.toggle_fingerprint_sub)     to fingerprint   to { fingerprint = !fingerprint },
                ).forEachIndexed { i, (triple, onToggle) ->
                    val (pair2, value) = triple
                    val (pair1, subtitle) = pair2
                    val (icon, title) = pair1
                    if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(Card), Alignment.Center) { Text(icon, fontSize = 16.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(title,    fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(subtitle, fontSize = 11.sp, color = TextMuted)
                        }
                        Switch(
                            checked  = value,
                            onCheckedChange = { onToggle() },
                            colors   = SwitchDefaults.colors(
                                checkedThumbColor        = Color.Black,
                                checkedTrackColor        = Orange,
                                uncheckedThumbColor      = Color(0xFF888888),
                                uncheckedTrackColor      = SliderTrack
                            )
                        )
                    }
                }
            }

            // Pro Tips
            SectionHeader(stringResource(R.string.section_pro_tips_lag))
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(14.dp)).background(Orange.copy(0.04f))
                    .border(1.dp, Orange.copy(0.15f), RoundedCornerShape(14.dp)).padding(14.dp)
            ) {
                listOf(
                    "🧹" to stringResource(R.string.sens_tip_1),
                    "📶" to stringResource(R.string.sens_tip_2),
                    "🔋" to stringResource(R.string.sens_tip_3),
                    "📱" to stringResource(R.string.sens_tip_4),
                ).forEach { (icon, tip) ->
                    Row(Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.Top) {
                        Text(icon, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp, top = 1.dp))
                        Text(tip, fontSize = 11.sp, color = TextMuted, lineHeight = 17.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Activate button
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                    .scale(buttonScale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (activated) Primary else Orange)
                    .clickable { activated = true }.padding(vertical = 16.dp),
                Alignment.Center
            ) {
                Text(
                    if (activated) stringResource(R.string.btn_settings_activated) else stringResource(R.string.btn_activate_settings),
                    fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black, letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
private fun SensSlider(label: String, value: Int, color: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(78.dp))
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(SliderTrack)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(value / 100f).clip(RoundedCornerShape(3.dp)).background(color))
        }
        Spacer(Modifier.width(10.dp))
        Text("$value", fontSize = 13.sp, fontWeight = FontWeight.Black, color = color, modifier = Modifier.width(28.dp))
    }
}
