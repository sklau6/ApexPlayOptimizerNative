package com.apexplayoptimizer.app.ui.screens

import android.app.Activity
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.apexplayoptimizer.app.LocaleHelper
import com.apexplayoptimizer.app.R
import com.apexplayoptimizer.app.ui.theme.*

private data class SettingState(
    val autoBoost: Boolean        = true,
    val gamingMode: Boolean       = true,
    val hudOverlay: Boolean       = false,
    val notifications: Boolean    = true,
    val vibration: Boolean        = true,
    val darkTheme: Boolean        = true,
    val autoKillApps: Boolean     = true,
    val cpuOptimize: Boolean      = true,
    val networkOptimize: Boolean  = false,
    val thermalProtect: Boolean   = true,
    val batteryMode: Boolean      = false,
    val fpsCap: Boolean           = false,
)

private data class LangOption(val code: String, val displayRes: Int)

private val LANG_OPTIONS = listOf(
    LangOption("en",    R.string.lang_english),
    LangOption("zh",    R.string.lang_chinese),
    LangOption("zh-TW", R.string.lang_chinese_tw),
    LangOption("hi",    R.string.lang_hindi),
    LangOption("ar",    R.string.lang_arabic),
    LangOption("in",    R.string.lang_indonesian),
    LangOption("pt",    R.string.lang_portuguese),
)

@Composable
fun SettingsScreen(nav: NavController) {
    var s by remember { mutableStateOf(SettingState()) }
    val context = LocalContext.current
    val currentLang = remember { LocaleHelper.getSavedLanguage(context) }
    var selectedLang by remember { mutableStateOf(currentLang) }

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
            Text(stringResource(R.string.settings_title), fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = 3.sp)
            Box(Modifier.size(36.dp))
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 88.dp)) {

            // Profile banner
            Box(
                Modifier.fillMaxWidth().padding(12.dp).clip(RoundedCornerShape(16.dp))
                    .background(Card).border(1.dp, Primary.copy(0.25f), RoundedCornerShape(16.dp)).padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        Modifier.size(52.dp).clip(RoundedCornerShape(26.dp))
                            .background(Primary.copy(0.1f)).border(2.dp, Primary, RoundedCornerShape(26.dp)),
                        Alignment.Center
                    ) { Text("🎮", fontSize = 26.sp) }
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_profile_name), fontSize = 13.sp, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = 0.5.sp)
                        Text(stringResource(R.string.settings_premium_desc), fontSize = 11.sp, color = TextMuted)
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier.clip(RoundedCornerShape(10.dp)).background(Yellow.copy(0.1f))
                                .border(1.dp, Yellow.copy(0.27f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) { Text(stringResource(R.string.settings_premium_badge), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Yellow, letterSpacing = 0.5.sp) }
                    }
                }
            }

            // Game Boost section
            SettingsSectionTitle(stringResource(R.string.section_game_boost))
            SettingsGroup {
                SettingsToggle("⚡", stringResource(R.string.toggle_auto_boost),   stringResource(R.string.toggle_auto_boost_sub),   s.autoBoost)    { s = s.copy(autoBoost = !s.autoBoost) }
                SettingsDivider()
                SettingsToggle("🎮", stringResource(R.string.toggle_gaming_mode),  stringResource(R.string.toggle_gaming_mode_sub),  s.gamingMode)   { s = s.copy(gamingMode = !s.gamingMode) }
                SettingsDivider()
                SettingsToggle("🧹", stringResource(R.string.toggle_auto_kill),    stringResource(R.string.toggle_auto_kill_sub),    s.autoKillApps) { s = s.copy(autoKillApps = !s.autoKillApps) }
                SettingsDivider()
                SettingsToggle("🖥", stringResource(R.string.toggle_cpu_optimize), stringResource(R.string.toggle_cpu_optimize_sub), s.cpuOptimize)  { s = s.copy(cpuOptimize = !s.cpuOptimize) }
            }

            SettingsSectionTitle(stringResource(R.string.section_hud_display))
            SettingsGroup {
                SettingsToggle("📊", stringResource(R.string.toggle_hud_overlay),  stringResource(R.string.toggle_hud_overlay_sub),  s.hudOverlay)   { s = s.copy(hudOverlay = !s.hudOverlay) }
                SettingsDivider()
                SettingsToggle("🌙", stringResource(R.string.toggle_dark_theme),   stringResource(R.string.toggle_dark_theme_sub),   s.darkTheme)    { s = s.copy(darkTheme = !s.darkTheme) }
                SettingsDivider()
                SettingsToggle("🎯", stringResource(R.string.toggle_fps_cap),      stringResource(R.string.toggle_fps_cap_sub),      s.fpsCap)       { s = s.copy(fpsCap = !s.fpsCap) }
            }

            SettingsSectionTitle(stringResource(R.string.section_network))
            SettingsGroup {
                SettingsToggle("📶", stringResource(R.string.toggle_network_optimize), stringResource(R.string.toggle_network_optimize_sub), s.networkOptimize) { s = s.copy(networkOptimize = !s.networkOptimize) }
                SettingsDivider()
                SettingsValueRow("📡", stringResource(R.string.label_ping_monitor), null, stringResource(R.string.value_active))
            }

            SettingsSectionTitle(stringResource(R.string.section_power_thermal))
            SettingsGroup {
                SettingsToggle("🌡", stringResource(R.string.toggle_thermal_protect), stringResource(R.string.toggle_thermal_protect_sub), s.thermalProtect) { s = s.copy(thermalProtect = !s.thermalProtect) }
                SettingsDivider()
                SettingsToggle("🔋", stringResource(R.string.toggle_battery_mode),    stringResource(R.string.toggle_battery_mode_sub),    s.batteryMode)    { s = s.copy(batteryMode = !s.batteryMode) }
            }

            SettingsSectionTitle(stringResource(R.string.section_notifications))
            SettingsGroup {
                SettingsToggle("🔔", stringResource(R.string.toggle_notifications), stringResource(R.string.toggle_notifications_sub), s.notifications) { s = s.copy(notifications = !s.notifications) }
                SettingsDivider()
                SettingsToggle("📳", stringResource(R.string.toggle_vibration),     stringResource(R.string.toggle_vibration_sub),     s.vibration)     { s = s.copy(vibration = !s.vibration) }
            }

            // Language section
            SettingsSectionTitle(stringResource(R.string.section_language))
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(12.dp)).background(Surface)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            ) {
                LANG_OPTIONS.forEachIndexed { i, lang ->
                    val isSelected = selectedLang == lang.code
                    if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                    Row(
                        Modifier.fillMaxWidth()
                            .background(if (isSelected) Primary.copy(0.07f) else Color.Transparent)
                            .clickable {
                                if (!isSelected) {
                                    selectedLang = lang.code
                                    LocaleHelper.saveAndApply(context, lang.code)
                                    (context as? Activity)?.recreate()
                                }
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(lang.displayRes), fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Primary else TextPrimary, modifier = Modifier.weight(1f))
                        if (isSelected) {
                            Box(
                                Modifier.size(20.dp).clip(RoundedCornerShape(10.dp)).background(Primary),
                                Alignment.Center
                            ) { Text("✓", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black) }
                        }
                    }
                }
            }

            SettingsSectionTitle(stringResource(R.string.section_about))
            SettingsGroup {
                SettingsValueRow("📱", stringResource(R.string.about_app_version), null, "v1.0.0")
                SettingsDivider()
                SettingsNavRow("🛡", stringResource(R.string.about_data_safety),      stringResource(R.string.about_data_safety_sub))
                SettingsDivider()
                SettingsNavRow("📜", stringResource(R.string.about_privacy_policy),   null)
                SettingsDivider()
                SettingsNavRow("📧", stringResource(R.string.about_contact_support),  null)
                SettingsDivider()
                SettingsNavRow("⭐", stringResource(R.string.about_rate_app),         null)
            }

            // Safety card
            Box(
                Modifier.fillMaxWidth().padding(12.dp).padding(top = 4.dp)
                    .clip(RoundedCornerShape(12.dp)).background(Primary.copy(0.05f))
                    .border(1.dp, Primary.copy(0.15f), RoundedCornerShape(12.dp)).padding(16.dp)
            ) {
                Column {
                    Text(stringResource(R.string.safety_title), fontSize = 12.sp, fontWeight = FontWeight.Black, color = Primary, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Text(stringResource(R.string.safety_text), fontSize = 11.sp, color = TextMuted, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(title, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = TextMuted, letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 4.dp))
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(12.dp)).background(Surface)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
        content = content
    )
}

@Composable
private fun SettingsDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
}

@Composable
private fun SettingsToggle(icon: String, title: String, subtitle: String?, checked: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onToggle() }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(Card), Alignment.Center) { Text(icon, fontSize = 16.sp) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            if (subtitle != null) Text(subtitle, fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 1.dp))
        }
        Switch(
            checked         = checked,
            onCheckedChange = { onToggle() },
            colors          = SwitchDefaults.colors(
                checkedThumbColor   = Color.Black,
                checkedTrackColor   = Primary,
                uncheckedThumbColor = Color(0xFF888888),
                uncheckedTrackColor = SliderTrack
            )
        )
    }
}

@Composable
private fun SettingsValueRow(icon: String, title: String, subtitle: String?, rightText: String) {
    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(Card), Alignment.Center) { Text(icon, fontSize = 16.sp) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            if (subtitle != null) Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Text(rightText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
    }
}

@Composable
private fun SettingsNavRow(icon: String, title: String, subtitle: String?) {
    Row(Modifier.fillMaxWidth().clickable {}.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(Card), Alignment.Center) { Text(icon, fontSize = 16.sp) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            if (subtitle != null) Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Text("›", fontSize = 22.sp, color = TextMuted, fontWeight = FontWeight.Light)
    }
}
