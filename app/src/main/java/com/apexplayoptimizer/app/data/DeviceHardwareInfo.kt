package com.apexplayoptimizer.app.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import kotlin.math.pow
import kotlin.math.sqrt

data class DeviceHardwareInfo(
    val manufacturer: String      = "",
    val model: String             = "",
    val board: String             = "",
    val osVersion: String         = "",
    val androidCodename: String   = "",
    val securityPatch: String     = "",
    val cpuModel: String          = "",
    val coreCount: Int            = 0,
    val architecture: String      = "",
    val supportedAbis: String     = "",
    val displayInches: String     = "",
    val displayWidth: Int         = 0,
    val displayHeight: Int        = 0,
    val displayDpi: Int           = 0,
    val displayDensityBucket: String = "",
    val totalApps: Int            = 0,
    val userApps: Int             = 0,
    val systemApps: Int           = 0,
    val wifiIp: String            = "",
    val wifiSignalDbm: Int        = 0,
    val wifiSpeedMbps: Int        = 0,
    val batteryCapacityMah: Int   = 0,
    val batteryTechnology: String = "",
    val batteryHealth: String     = "",
    val batteryChargeCycles: Int  = 0,
)

@Composable
fun rememberDeviceHardwareInfo(): State<DeviceHardwareInfo> {
    val ctx   = LocalContext.current.applicationContext
    val state = remember { mutableStateOf(DeviceHardwareInfo()) }
    LaunchedEffect(Unit) {
        state.value = withContext(Dispatchers.IO) { readHardwareInfo(ctx) }
    }
    return state
}

@SuppressLint("HardwareIds")
private fun readHardwareInfo(ctx: Context): DeviceHardwareInfo {
    // CPU model from /proc/cpuinfo
    val cpuModel = try {
        BufferedReader(FileReader("/proc/cpuinfo")).useLines { lines ->
            lines.firstOrNull { it.startsWith("Hardware") || it.startsWith("model name") }
                ?.substringAfter(":")?.trim()
        } ?: Build.HARDWARE
    } catch (_: Exception) { Build.HARDWARE }

    // Core count via sysfs
    val coreCount = try {
        File("/sys/devices/system/cpu")
            .listFiles { f -> f.name.matches(Regex("cpu\\d+")) }?.size
            ?: Runtime.getRuntime().availableProcessors()
    } catch (_: Exception) { Runtime.getRuntime().availableProcessors() }

    // Display metrics
    val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val dm = DisplayMetrics()
    @Suppress("DEPRECATION") wm.defaultDisplay.getRealMetrics(dm)
    val inchesVal = sqrt(
        (dm.widthPixels / dm.xdpi).toDouble().pow(2.0) +
        (dm.heightPixels / dm.ydpi).toDouble().pow(2.0)
    )
    val bucket = when {
        dm.densityDpi <= 120 -> "ldpi"
        dm.densityDpi <= 160 -> "mdpi"
        dm.densityDpi <= 240 -> "hdpi"
        dm.densityDpi <= 320 -> "xhdpi"
        dm.densityDpi <= 480 -> "xxhdpi"
        else -> "xxxhdpi"
    }

    // App counts
    val allPkgs = try { ctx.packageManager.getInstalledPackages(0) } catch (_: Exception) { emptyList() }
    val sysCnt  = allPkgs.count { (it.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0 }

    // WiFi info
    val (wifiIp, wifiSig, wifiSpeed) = try {
        val wifiMgr = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiMgr.connectionInfo
        Triple(Formatter.formatIpAddress(info.ipAddress).takeIf { it != "0.0.0.0" } ?: "", info.rssi, info.linkSpeed)
    } catch (_: Exception) { Triple("", 0, 0) }

    // Battery static info via reflection (no root needed)
    val battCap = try {
        val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
        val powerProfile = powerProfileClass.getConstructor(Context::class.java).newInstance(ctx)
        val cap = powerProfileClass.getMethod("getBatteryCapacity").invoke(powerProfile)
        (cap as? Double)?.toInt() ?: 0
    } catch (_: Exception) { 0 }

    val battIntent = try {
        ctx.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
    } catch (_: Exception) { null }
    val battTech   = battIntent?.getStringExtra(android.os.BatteryManager.EXTRA_TECHNOLOGY) ?: ""
    val healthCode = battIntent?.getIntExtra(android.os.BatteryManager.EXTRA_HEALTH, 0) ?: 0
    val battHealth = when (healthCode) {
        android.os.BatteryManager.BATTERY_HEALTH_GOOD          -> "Good"
        android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT      -> "Overheat"
        android.os.BatteryManager.BATTERY_HEALTH_DEAD          -> "Dead"
        android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE  -> "Over Voltage"
        android.os.BatteryManager.BATTERY_HEALTH_COLD          -> "Cold"
        else -> "Unknown"
    }

    return DeviceHardwareInfo(
        manufacturer         = Build.MANUFACTURER.replaceFirstChar { it.uppercaseChar() },
        model                = Build.MODEL,
        board                = Build.BOARD,
        osVersion            = Build.VERSION.RELEASE,
        androidCodename      = Build.VERSION.CODENAME,
        securityPatch        = Build.VERSION.SECURITY_PATCH,
        cpuModel             = cpuModel,
        coreCount            = coreCount,
        architecture         = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a",
        supportedAbis        = Build.SUPPORTED_ABIS.take(2).joinToString(", "),
        displayInches        = "%.1f".format(inchesVal),
        displayWidth         = dm.widthPixels,
        displayHeight        = dm.heightPixels,
        displayDpi           = dm.densityDpi,
        displayDensityBucket = bucket,
        totalApps            = allPkgs.size,
        userApps             = allPkgs.size - sysCnt,
        systemApps           = sysCnt,
        wifiIp               = wifiIp,
        wifiSignalDbm        = wifiSig,
        wifiSpeedMbps        = wifiSpeed,
        batteryCapacityMah   = battCap,
        batteryTechnology    = battTech,
        batteryHealth        = battHealth,
        batteryChargeCycles  = 0,
    )
}
