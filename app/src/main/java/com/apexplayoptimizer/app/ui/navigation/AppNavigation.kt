package com.apexplayoptimizer.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexplayoptimizer.app.R
import androidx.annotation.StringRes
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.apexplayoptimizer.app.ui.screens.*
import com.apexplayoptimizer.app.ui.theme.*

sealed class Screen(val route: String) {
    object Splash      : Screen("splash")
    object Home        : Screen("home")
    object Dashboard   : Screen("dashboard")
    object GFXTool     : Screen("gfx_tool")
    object Sensitivity : Screen("sensitivity")
    object Settings    : Screen("settings")
    object HUDMonitor  : Screen("hud_monitor")
    object ZeroLag     : Screen("zero_lag")
    object Premium     : Screen("premium")
    object Store       : Screen("store")
}

private data class BottomNavItem(val screen: Screen, val icon: String, val labelRes: Int)

private val bottomItemDefs = listOf(
    BottomNavItem(Screen.Home,        "🏠", R.string.nav_home),
    BottomNavItem(Screen.Dashboard,   "📊", R.string.nav_dash),
    BottomNavItem(Screen.Store,       "🛍️", R.string.nav_store),
    BottomNavItem(Screen.Sensitivity, "🎯", R.string.nav_sens),
    BottomNavItem(Screen.Settings,    "⚙️",  R.string.nav_more),
)

private val bottomRoutes = bottomItemDefs.map { it.screen.route }
private val noBarRoutes  = setOf(Screen.Splash.route)

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val backEntry by nav.currentBackStackEntryAsState()
    val current  = backEntry?.destination?.route

    Box(Modifier.fillMaxSize().background(Background)) {
        NavHost(nav, startDestination = Screen.Splash.route, Modifier.fillMaxSize()) {
            composable(Screen.Splash.route)      { SplashScreen      { nav.navigate(Screen.Home.route) { popUpTo(Screen.Splash.route) { inclusive = true } } } }
            composable(Screen.Home.route)        { HomeScreen(nav) }
            composable(Screen.Dashboard.route)   { DashboardScreen(nav) }
            composable(Screen.GFXTool.route)     { GFXToolScreen(nav) }
            composable(Screen.Sensitivity.route) { SensitivityScreen(nav) }
            composable(Screen.Settings.route)    { SettingsScreen(nav) }
            composable(Screen.HUDMonitor.route)  { HUDMonitorScreen(nav) }
            composable(Screen.ZeroLag.route)     { ZeroLagScreen(nav) }
            composable(Screen.Premium.route)     { PremiumScreen(nav) }
            composable(Screen.Store.route)       { StoreScreen(nav) }
        }
        if (current !in noBarRoutes) {
            BottomBar(current, nav, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun BottomBar(current: String?, nav: NavController, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0A))
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF141414))
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            bottomItemDefs.forEach { item ->
                val selected = current == item.screen.route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (!selected) {
                                nav.navigate(item.screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(item.icon, fontSize = if (selected) 21.sp else 19.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        stringResource(item.labelRes),
                        fontSize   = 9.sp,
                        fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
                        color      = if (selected) Primary else TextMuted,
                        letterSpacing = 0.5.sp
                    )
                    if (selected) {
                        Spacer(Modifier.height(3.dp))
                        Box(Modifier.width(18.dp).height(2.dp).clip(RoundedCornerShape(1.dp)).background(Primary))
                    }
                }
            }
        }
    }
}
