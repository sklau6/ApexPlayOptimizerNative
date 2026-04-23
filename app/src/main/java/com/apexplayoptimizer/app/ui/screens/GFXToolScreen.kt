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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.apexplayoptimizer.app.R
import com.apexplayoptimizer.app.data.DeviceOptimizer
import com.apexplayoptimizer.app.data.GFXConfig
import com.apexplayoptimizer.app.data.GFXConfigManager
import com.apexplayoptimizer.app.data.InterstitialAdManager
import com.apexplayoptimizer.app.ui.theme.*
import kotlinx.coroutines.delay

private val QUALITY_OPTIONS  = listOf("Super Smooth", "Smooth", "Balanced", "HD", "HDR", "Ultra HD")
private val FPS_OPTIONS      = listOf("30fps", "60fps", "90fps", "120fps", "144fps")
private val RESOLUTION_OPTS  = listOf("720p", "1080p", "1440p", "1920p", "2K", "2560p")
private val STYLE_OPTIONS    = listOf("Default", "Colorful", "Realistic", "Cinematic")
private val SHADOW_OPTIONS   = listOf("Disabled", "Low", "Medium", "High", "Ultra")
private val ANTIALIASING_OPT = listOf("Off", "2x MSAA", "4x MSAA", "8x MSAA", "TAA")

private data class GFXSlider(val label: String, val max: Int, val color: Color)

@Composable
fun GFXToolScreen(nav: NavController) {
    val ctx = LocalContext.current
    val saved = remember { GFXConfigManager.load(ctx) }
    val sliders = listOf(
        GFXSlider(stringResource(R.string.slider_render_scale),    100, Blue),
        GFXSlider(stringResource(R.string.slider_texture_quality), 100, Primary),
        GFXSlider(stringResource(R.string.slider_shadow_detail),   100, Orange),
        GFXSlider(stringResource(R.string.slider_particle_fx),     100, Purple),
        GFXSlider(stringResource(R.string.slider_post_processing), 100, Yellow),
        GFXSlider(stringResource(R.string.slider_lod_distance),    100, Color(0xFF00CCFF)),
    )
    var quality    by remember { mutableStateOf(saved.quality) }
    var fps        by remember { mutableStateOf(saved.fps) }
    var resolution by remember { mutableStateOf(saved.resolution) }
    var style      by remember { mutableStateOf(saved.style) }
    var shadows    by remember { mutableStateOf(saved.shadows) }
    var antiAlias  by remember { mutableStateOf(saved.antiAlias) }
    var sliderVals by remember { mutableStateOf(saved.sliderVals) }
    var page       by remember { mutableIntStateOf(0) }
    var applied    by remember { mutableStateOf(GFXConfigManager.isSaved(ctx)) }

    val inf = rememberInfiniteTransition(label = "rot")
    val rot by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "r")

    LaunchedEffect(applied) { if (applied) { delay(3000); applied = false } }

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
                Text(stringResource(R.string.gfx_title), fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = 3.sp)
                Text(stringResource(R.string.gfx_subtitle), fontSize = 10.sp, color = Blue, letterSpacing = 1.sp)
            }
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(Blue.copy(0.1f))
                .border(1.dp, Blue.copy(0.3f), RoundedCornerShape(18.dp)), Alignment.Center
            ) { Text("🖥", fontSize = 16.sp) }
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = 72.dp)) {

            // Logo + gear
            Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), Alignment.Center) {
                Box(Modifier.size(90.dp).rotate(rot).clip(RoundedCornerShape(45.dp))
                    .border(2.dp, Blue.copy(0.3f), RoundedCornerShape(45.dp)).background(Blue.copy(0.06f)))
                Box(Modifier.size(70.dp).clip(RoundedCornerShape(35.dp)).background(Color(0xFF141414))
                    .border(1.dp, Blue.copy(0.4f), RoundedCornerShape(35.dp)), Alignment.Center) {
                    Text("⚙", fontSize = 32.sp)
                }
            }

            // Page indicator
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                listOf(stringResource(R.string.gfx_page_quick), stringResource(R.string.gfx_page_advanced)).forEachIndexed { i, label ->
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp))
                            .background(if (page == i) Blue.copy(0.15f) else Card)
                            .border(1.dp, if (page == i) Blue.copy(0.5f) else CardBorder, RoundedCornerShape(20.dp))
                            .clickable { page = i }.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (page == i) Blue else TextMuted, letterSpacing = 0.5.sp) }
                }
            }
            Spacer(Modifier.height(12.dp))

            if (page == 0) {
                // Quick Settings
                Column(Modifier.padding(horizontal = 12.dp)) {
                    listOf(
                        stringResource(R.string.dropdown_graphics_quality) to quality to QUALITY_OPTIONS  to { v: String -> quality = v },
                        stringResource(R.string.dropdown_frame_rate)       to fps    to FPS_OPTIONS        to { v: String -> fps = v },
                        stringResource(R.string.dropdown_resolution)       to resolution to RESOLUTION_OPTS to { v: String -> resolution = v },
                        stringResource(R.string.dropdown_graphic_style)    to style  to STYLE_OPTIONS      to { v: String -> style = v },
                        stringResource(R.string.dropdown_shadows)          to shadows to SHADOW_OPTIONS    to { v: String -> shadows = v },
                        stringResource(R.string.dropdown_anti_aliasing)    to antiAlias to ANTIALIASING_OPT to { v: String -> antiAlias = v },
                    ).forEach { (triple, onSel) ->
                        val (labelPair, options) = triple
                        val (label, selected) = labelPair
                        GFXDropdown(label, options, selected, onSel)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            } else {
                // Advanced Sliders
                Column(Modifier.padding(horizontal = 12.dp)) {
                    sliders.forEachIndexed { i, s ->
                        GFXSliderRow(s.label, sliderVals[i], s.color) { newVal ->
                            sliderVals = sliderVals.toMutableList().also { it[i] = newVal }
                        }
                        Spacer(Modifier.height(14.dp))
                    }
                }
            }

            // Presets row
            SectionHeader(stringResource(R.string.section_quick_presets))
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(stringResource(R.string.preset_ultra) to stringResource(R.string.preset_ultra_sub) to Danger, stringResource(R.string.preset_balanced) to stringResource(R.string.preset_balanced_sub) to Blue, stringResource(R.string.preset_smooth) to stringResource(R.string.preset_smooth_sub) to Primary)
                    .forEachIndexed { idx, (pair, color) ->
                        val (label, sub) = pair
                        Box(
                            Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(color.copy(0.06f))
                                .border(1.dp, color.copy(0.3f), RoundedCornerShape(12.dp))
                                .clickable {
                                    when (idx) {
                                        0    -> { quality = "Ultra HD"; fps = "60fps";  resolution = "2K" }
                                        2    -> { quality = "Smooth";   fps = "144fps"; resolution = "720p" }
                                        else -> { quality = "HD";       fps = "60fps";  resolution = "1080p" }
                                    }
                                }.padding(12.dp),
                            Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Black, color = color, letterSpacing = 0.5.sp)
                                Text(sub,   fontSize = 9.sp,  color = TextMuted)
                            }
                        }
                    }
            }
            Spacer(Modifier.height(12.dp))

            // Tips
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(12.dp))
                    .background(Blue.copy(0.05f)).border(1.dp, Blue.copy(0.15f), RoundedCornerShape(12.dp)).padding(14.dp)
            ) {
                Column {
                    Text(stringResource(R.string.gfx_tips_title), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Blue)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        stringResource(R.string.gfx_tip_1),
                        stringResource(R.string.gfx_tip_2),
                        stringResource(R.string.gfx_tip_3),
                    ).forEach { tip ->
                        Row(Modifier.padding(bottom = 6.dp), verticalAlignment = Alignment.Top) {
                            Text("•", fontSize = 12.sp, color = Blue, modifier = Modifier.padding(end = 6.dp, top = 1.dp))
                            Text(tip, fontSize = 11.sp, color = TextMuted, lineHeight = 17.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Apply button + success banner
            if (applied) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(10.dp))
                    .background(Primary.copy(0.1f)).border(1.dp, Primary.copy(0.3f), RoundedCornerShape(10.dp)).padding(14.dp), Alignment.Center) {
                    Text(stringResource(R.string.gfx_settings_applied), fontSize = 12.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
            }
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(10.dp))
                    .background(Blue)
                    .clickable {
                        val cfg = GFXConfig(
                            quality    = quality,
                            fps        = fps,
                            resolution = resolution,
                            style      = style,
                            shadows    = shadows,
                            antiAlias  = antiAlias,
                            sliderVals = sliderVals
                        )
                        GFXConfigManager.save(ctx, cfg)
                        // Apply preferred refresh rate to this app's window
                        try {
                            val activity = ctx as? Activity
                            val lp = activity?.window?.attributes
                            if (lp != null) {
                                lp.preferredRefreshRate = DeviceOptimizer.preferredRefreshRate(fps)
                                activity.window.attributes = lp
                            }
                        } catch (_: Exception) {}
                        applied = true
                        // Show interstitial ad after applying settings
                        (ctx as? Activity)?.let { InterstitialAdManager.show(it) }
                    }
                    .padding(vertical = 16.dp),
                Alignment.Center
            ) { Text(stringResource(R.string.btn_apply_settings), fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black, letterSpacing = 2.sp) }
        }
    }
}

@Composable
private fun GFXDropdown(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Card)
                .border(1.dp, if (expanded) Blue.copy(0.5f) else CardBorder, RoundedCornerShape(10.dp))
                .clickable { expanded = !expanded }.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = TextMuted, letterSpacing = 1.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(selected, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Blue)
                Text(if (expanded) "▲" else "▼", fontSize = 10.sp, color = Blue)
            }
        }
        if (expanded) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(Surface).border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            ) {
                options.forEach { opt ->
                    Box(
                        Modifier.fillMaxWidth()
                            .background(if (opt == selected) Blue.copy(0.12f) else Color.Transparent)
                            .clickable { onSelect(opt); expanded = false }
                            .padding(14.dp, 11.dp)
                    ) { Text(opt, fontSize = 13.sp, color = if (opt == selected) Blue else TextSecondary, fontWeight = if (opt == selected) FontWeight.Bold else FontWeight.Normal) }
                }
            }
        }
    }
}

@Composable
private fun GFXSliderRow(label: String, value: Int, color: Color, onChange: (Int) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(label, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
            Text("$value%", fontSize = 14.sp, fontWeight = FontWeight.Black, color = color)
        }
        Spacer(Modifier.height(8.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val fullW = maxWidth
            Box(
                Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(SliderTrack)
                    .clickable { }
            ) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(value / 100f).clip(RoundedCornerShape(4.dp)).background(color))
                Row(Modifier.fillMaxWidth().fillMaxHeight(), Arrangement.SpaceEvenly) {
                    repeat(4) { Box(Modifier.width(1.dp).fillMaxHeight().background(Color(0xFF0A0A0A))) }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), Arrangement.SpaceBetween) {
            Text("0%", fontSize = 9.sp, color = TextMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(Card).border(1.dp, CardBorder, RoundedCornerShape(6.dp)).clickable { if (value > 0) onChange(value - 5) }, Alignment.Center) {
                    Text("−", fontSize = 14.sp, color = color, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(Card).border(1.dp, CardBorder, RoundedCornerShape(6.dp)).clickable { if (value < 100) onChange(value + 5) }, Alignment.Center) {
                    Text("+", fontSize = 14.sp, color = color, fontWeight = FontWeight.Bold)
                }
            }
            Text("100%", fontSize = 9.sp, color = TextMuted)
        }
    }
}
