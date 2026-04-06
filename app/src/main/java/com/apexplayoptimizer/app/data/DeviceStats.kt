package com.apexplayoptimizer.app.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
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
    val ramUsed: Float     = 50f,   // 0–100 %
    val ramTotal: Int      = 4,     // GB
    val cpuLoad: Int       = 25,    // 0–100 %
    val temperature: Float = 36f,   // °C  (battery temperature)
    val ping: Int          = -1,    // ms, -1 = loading
    val storageUsed: Float = 32f,   // GB used
    val storageTotal: Int  = 64,    // GB total
    val networkDown: Float = 0f,    // MB/s download
    val networkUp: Float   = 0f,    // MB/s upload
)

// ── private helpers (all run on Dispatchers.IO) ──────────────────────────────

private data class CpuTick(val total: Long, val idle: Long)

private fun readCpuTick(): CpuTick = try {
    BufferedReader(FileReader("/proc/stat")).use { br ->
        val vals = br.readLine().trim().split("\\s+".toRegex()).drop(1)
            .map { it.toLongOrNull() ?: 0L }
        val idle = vals.getOrElse(3) { 0L } + vals.getOrElse(4) { 0L }  // idle + iowait
        CpuTick(vals.sum(), idle)
    }
} catch (_: Exception) { CpuTick(1L, 0L) }

private fun readRam(ctx: Context): Pair<Float, Int> {
    val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
    val usedPct  = ((mi.totalMem - mi.availMem) * 100f / mi.totalMem).coerceIn(0f, 100f)
    val totalGb  = (mi.totalMem / 1_073_741_824f).toInt().coerceAtLeast(1)
    return usedPct to totalGb
}

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
        var prevTick = withContext(Dispatchers.IO) { readCpuTick() }
        var prevRx   = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
        var prevTx   = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)

        while (true) {
            delay(2_000L)

            val curTick = withContext(Dispatchers.IO) { readCpuTick() }
            val curRx   = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
            val curTx   = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)

            val dTotal = (curTick.total - prevTick.total).coerceAtLeast(1L)
            val dIdle  = (curTick.idle  - prevTick.idle ).coerceAtLeast(0L)
            val cpu    = ((1f - dIdle.toFloat() / dTotal) * 100).toInt().coerceIn(0, 100)

            // bytes → MB over the 2-second window
            val downMb = (curRx - prevRx).coerceAtLeast(0L) / 2_097_152f
            val upMb   = (curTx - prevTx).coerceAtLeast(0L) / 2_097_152f

            val (ramPct, ramGb)       = withContext(Dispatchers.IO) { readRam(ctx) }
            val temp                  = withContext(Dispatchers.IO) { readBatteryTemp(ctx) }
            val (stUsed, stTotal)     = withContext(Dispatchers.IO) { readStorage() }

            prevTick = curTick; prevRx = curRx; prevTx = curTx

            state.value = state.value.copy(
                ramUsed      = ramPct,
                ramTotal     = ramGb,
                cpuLoad      = cpu,
                temperature  = temp,
                storageUsed  = stUsed,
                storageTotal = stTotal,
                networkDown  = downMb,
                networkUp    = upMb,
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
