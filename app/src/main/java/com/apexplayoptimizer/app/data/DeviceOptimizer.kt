package com.apexplayoptimizer.app.data

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

enum class OptimizeMode { POWER_SAVE, BALANCED, GAMER }

data class BoostResult(
    val killedProcesses: Int  = 0,
    val ramFreedMb: Int       = 0,
    val wakeLockActive: Boolean = false,
    val dndEnabled: Boolean   = false,
)

object DeviceOptimizer {

    private var wakeLock: PowerManager.WakeLock? = null

    // ── Main entry point ─────────────────────────────────────────────────────

    suspend fun runBoost(
        ctx: Context,
        mode: OptimizeMode = OptimizeMode.BALANCED
    ): BoostResult = withContext(Dispatchers.IO) {
        val app = ctx.applicationContext
        val am  = app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Snapshot available RAM before optimisation
        val miBefore = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }

        // Kill background processes
        val killed = killBackgroundProcesses(app, am, mode)

        // GC hints — best-effort
        System.runFinalization()
        Runtime.getRuntime().gc()
        System.gc()

        // Give Android a moment to reclaim pages
        delay(400)

        // Snapshot available RAM after
        val miAfter  = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val freedMb  = ((miAfter.availMem - miBefore.availMem) / 1_048_576f)
            .toInt().coerceAtLeast(0)

        // WakeLock: hold CPU awake for gaming modes
        val wakeLockOn = when (mode) {
            OptimizeMode.GAMER, OptimizeMode.BALANCED -> {
                acquireWakeLock(app, 90 * 60_000L)   // 90 minutes
                true
            }
            OptimizeMode.POWER_SAVE -> {
                releaseWakeLock()
                false
            }
        }

        // Do-Not-Disturb: silence notifications during intense gaming
        val dnd = when (mode) {
            OptimizeMode.GAMER      -> enableDND(app)
            OptimizeMode.POWER_SAVE -> { disableDND(app); false }
            else                    -> false
        }

        BoostResult(killed, freedMb, wakeLockOn, dnd)
    }

    // ── Kill background processes ─────────────────────────────────────────────

    private fun killBackgroundProcesses(
        ctx: Context,
        am: ActivityManager,
        mode: OptimizeMode
    ): Int {
        var count = 0
        val own   = ctx.packageName
        try {
            val procs = am.runningAppProcesses ?: return 0
            // importance threshold: lower value = higher priority
            val threshold = when (mode) {
                OptimizeMode.POWER_SAVE -> ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE
                OptimizeMode.BALANCED   -> ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED
                OptimizeMode.GAMER      -> ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE
            }
            procs
                .filter { p -> p.importance >= threshold }
                .flatMap { p -> p.pkgList?.toList() ?: emptyList() }
                .filter  { pkg -> pkg != own }
                .distinct()
                .forEach { pkg ->
                    try { am.killBackgroundProcesses(pkg); count++ }
                    catch (_: Exception) {}
                }
        } catch (_: SecurityException) {}
        return count
    }

    // ── Do Not Disturb ────────────────────────────────────────────────────────

    fun enableDND(ctx: Context): Boolean = try {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.isNotificationPolicyAccessGranted) {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            true
        } else false
    } catch (_: Exception) { false }

    fun disableDND(ctx: Context): Boolean = try {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.isNotificationPolicyAccessGranted) {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            true
        } else false
    } catch (_: Exception) { false }

    fun isDNDEnabled(ctx: Context): Boolean = try {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE
    } catch (_: Exception) { false }

    // ── WakeLock ──────────────────────────────────────────────────────────────

    fun acquireWakeLock(ctx: Context, timeoutMs: Long = 3_600_000L) {
        try {
            releaseWakeLock()
            val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ApexPlay::GamingMode")
                .also { it.acquire(timeoutMs) }
        } catch (_: Exception) {}
    }

    fun releaseWakeLock() {
        try { wakeLock?.takeIf { it.isHeld }?.release() } catch (_: Exception) {}
        wakeLock = null
    }

    fun isWakeLockHeld(): Boolean = wakeLock?.isHeld == true

    // ── Refresh rate (applies to THIS app's window) ───────────────────────────

    fun preferredRefreshRate(fpsLabel: String): Float = when (fpsLabel) {
        "30fps"  -> 30f
        "60fps"  -> 60f
        "90fps"  -> 90f
        "120fps" -> 120f
        "144fps" -> 144f
        else     -> 60f
    }
}
