package com.apexplayoptimizer.app.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import java.io.File
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader
import java.net.InetSocketAddress
import java.net.Socket

data class DeviceStats(
    val ramUsed: Float          = 50f,      // 0–100 %
    val ramTotal: Int           = 4,        // GB
    val ramAvailMb: Int         = 0,        // MB available
    val cpuLoad: Int            = 0,         // 0–100 %
    val cpuCoreFreqsMhz: List<Int> = emptyList(), // per-core MHz
    val temperature: Float      = 36f,      // °C (battery)
    val batteryPct: Int         = 0,        // 0–100
    val batteryVoltage: Float   = 0f,       // Volts
    val batteryCurrent: Int     = 0,        // mA (negative = discharging)
    val batteryCharging: Boolean = false,
    val ping: Int               = -1,       // ms, -1 = loading
    val storageUsed: Float      = 32f,      // GB used
    val storageTotal: Int       = 64,       // GB total
    val networkDown: Float      = 0f,       // MB/s download
    val networkUp: Float        = 0f,       // MB/s upload
)

// ── private helpers (all run on Dispatchers.IO) ──────────────────────────────

private data class CpuTick(val total: Long, val idle: Long)

private fun readCpuTick(): CpuTick? = try {
    BufferedReader(FileReader("/proc/stat")).use { br ->
        val line = br.readLine() ?: return@use null
        val vals = line.trim().split("\\s+".toRegex()).drop(1)
            .map { it.toLongOrNull() ?: 0L }
        if (vals.size < 5) return@use null
        val total = vals.sum()
        if (total == 0L) return@use null
        val idle = vals[3] + vals[4]          // idle + iowait
        CpuTick(total, idle)
    }
} catch (_: Exception) { null }

private data class RamInfo(val usedPct: Float, val totalGb: Int, val availMb: Int)

private fun readRam(ctx: Context): RamInfo {
    val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
    val usedPct  = ((mi.totalMem - mi.availMem) * 100f / mi.totalMem).coerceIn(0f, 100f)
    val totalGb  = (mi.totalMem / 1_073_741_824f).toInt().coerceAtLeast(1)
    val availMb  = (mi.availMem / 1_048_576L).toInt()
    return RamInfo(usedPct, totalGb, availMb)
}

private fun readCoreFreqsMhz(): List<Int> = try {
    File("/sys/devices/system/cpu")
        .listFiles { f -> f.name.matches(Regex("cpu\\d+")) }
        ?.sortedBy { it.name.removePrefix("cpu").toIntOrNull() ?: 0 }
        ?.map { d ->
            try { File("${d.path}/cpufreq/scaling_cur_freq").readText().trim().toLong().div(1000).toInt() }
            catch (_: Exception) { 0 }
        } ?: emptyList()
} catch (_: Exception) { emptyList() }

private fun readMaxCoreFreqsMhz(): List<Int> = try {
    File("/sys/devices/system/cpu")
        .listFiles { f -> f.name.matches(Regex("cpu\\d+")) }
        ?.sortedBy { it.name.removePrefix("cpu").toIntOrNull() ?: 0 }
        ?.map { d ->
            try { File("${d.path}/cpufreq/cpuinfo_max_freq").readText().trim().toLong().div(1000).toInt() }
            catch (_: Exception) {
                try { File("${d.path}/cpufreq/scaling_max_freq").readText().trim().toLong().div(1000).toInt() }
                catch (_: Exception) { 0 }
            }
        } ?: emptyList()
} catch (_: Exception) { emptyList() }

private fun estimateCpuFromFreqs(curMhz: List<Int>, maxMhz: List<Int>): Int? {
    val pairs = curMhz.zip(maxMhz).filter { (_, max) -> max > 0 }
    if (pairs.isEmpty()) return null
    val avgRatio = pairs.sumOf { (cur, max) -> cur.toDouble() / max } / pairs.size
    return (avgRatio * 100).toInt().coerceIn(0, 99)
}

private data class BattDetails(val pct: Int, val voltage: Float, val currentMa: Int, val charging: Boolean)

private fun readBattDetails(ctx: Context): BattDetails = try {
    val intent  = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val pct     = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
    val voltage = ((intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000f)
    val bm      = ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val rawCur  = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
    val curMa   = if (rawCur == Int.MIN_VALUE) 0 else rawCur / 1000
    val charging = bm.isCharging
    BattDetails(pct, voltage, curMa, charging)
} catch (_: Exception) { BattDetails(0, 0f, 0, false) }

private fun readBatteryTemp(ctx: Context): Float = try {
    val intent = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    ((intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 360) ?: 360) / 10f)
        .coerceIn(20f, 90f)
} catch (_: Exception) { 36f }

private fun readStorage(): Pair<Float, Int> = try {
    val fs      = StatFs(Environment.getExternalStorageDirectory().path)
    val bsz     = fs.blockSizeLong
    val total   = fs.blockCountLong * bsz
    val avail   = fs.availableBlocksLong * bsz
    val usedGb  = ((total - avail) / 1_073_741_824f).coerceAtLeast(0f)
    val totalGb = (total / 1_073_741_824f).toInt().coerceAtLeast(1)
    usedGb to totalGb
} catch (_: Exception) { 32f to 64 }

private suspend fun measurePing(): Int = withContext(Dispatchers.IO) {
    try {
        val t0 = System.currentTimeMillis()
        Socket().use { it.connect(InetSocketAddress("8.8.8.8", 53), 3_000) }
        (System.currentTimeMillis() - t0).toInt().coerceAtLeast(1)
    } catch (_: Exception) { -1 }
}

// ── public composable ────────────────────────────────────────────────────────

@Composable
fun rememberDeviceStats(): State<DeviceStats> {
    val ctx   = LocalContext.current.applicationContext
    val state = remember { mutableStateOf(DeviceStats()) }

    // Metrics loop — every 2 s
    LaunchedEffect(Unit) {
        var prevTick: CpuTick? = withContext(Dispatchers.IO) { readCpuTick() }
        var prevRx   = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
        var prevTx   = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)
        // Read max frequencies once — they don't change at runtime
        val maxFreqs = withContext(Dispatchers.IO) { readMaxCoreFreqsMhz() }

        while (true) {
            delay(2_000L)

            val curTick = withContext(Dispatchers.IO) { readCpuTick() }
            val curRx   = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
            val curTx   = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)

            // Primary: /proc/stat delta; Fallback: cur_freq/max_freq proxy
            val cpu: Int? = if (curTick != null && prevTick != null) {
                val dTotal = (curTick.total - prevTick.total).coerceAtLeast(1L)
                val dIdle  = (curTick.idle  - prevTick.idle ).coerceAtLeast(0L)
                ((1f - dIdle.toFloat() / dTotal) * 100).toInt().coerceIn(0, 99)
            } else null  // resolved after coreFreqs are read below

            // bytes → MB over the 2-second window
            val downMb = (curRx - prevRx).coerceAtLeast(0L) / 2_097_152f
            val upMb   = (curTx - prevTx).coerceAtLeast(0L) / 2_097_152f

            val ramInfo     = withContext(Dispatchers.IO) { readRam(ctx) }
            val temp        = withContext(Dispatchers.IO) { readBatteryTemp(ctx) }
            val (stUsed, stTotal) = withContext(Dispatchers.IO) { readStorage() }
            val coreFreqs   = withContext(Dispatchers.IO) { readCoreFreqsMhz() }
            val battDetails = withContext(Dispatchers.IO) { readBattDetails(ctx) }

            prevTick = curTick ?: prevTick
            prevRx = curRx; prevTx = curTx

            // cpu from /proc/stat, or freq-ratio proxy, or keep last known value
            val resolvedCpu = cpu ?: estimateCpuFromFreqs(coreFreqs, maxFreqs) ?: state.value.cpuLoad

            state.value = state.value.copy(
                ramUsed          = ramInfo.usedPct,
                ramTotal         = ramInfo.totalGb,
                ramAvailMb       = ramInfo.availMb,
                cpuLoad          = resolvedCpu,
                cpuCoreFreqsMhz  = coreFreqs,
                temperature      = temp,
                batteryPct       = battDetails.pct,
                batteryVoltage   = battDetails.voltage,
                batteryCurrent   = battDetails.currentMa,
                batteryCharging  = battDetails.charging,
                storageUsed      = stUsed,
                storageTotal     = stTotal,
                networkDown      = downMb,
                networkUp        = upMb,
            )
        }
    }

    // Ping loop — every 5 s (background, non-blocking)
    LaunchedEffect(Unit) {
        while (true) {
            val p = measurePing()
            if (p > 0) state.value = state.value.copy(ping = p)
            delay(5_000L)
        }
    }

    return state
}
