package com.apexplayoptimizer.app.data

import android.content.Context

object SettingsPrefs {
    private const val PREFS = "apex_settings"

    fun getBoolean(ctx: Context, key: String, default: Boolean): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(key, default)

    fun saveAll(
        ctx: Context,
        autoBoost: Boolean,
        gamingMode: Boolean,
        hudOverlay: Boolean,
        notifications: Boolean,
        vibration: Boolean,
        darkTheme: Boolean,
        autoKillApps: Boolean,
        cpuOptimize: Boolean,
        networkOptimize: Boolean,
        thermalProtect: Boolean,
        batteryMode: Boolean,
        fpsCap: Boolean,
    ) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            putBoolean("autoBoost",       autoBoost)
            putBoolean("gamingMode",      gamingMode)
            putBoolean("hudOverlay",      hudOverlay)
            putBoolean("notifications",   notifications)
            putBoolean("vibration",       vibration)
            putBoolean("darkTheme",       darkTheme)
            putBoolean("autoKillApps",    autoKillApps)
            putBoolean("cpuOptimize",     cpuOptimize)
            putBoolean("networkOptimize", networkOptimize)
            putBoolean("thermalProtect",  thermalProtect)
            putBoolean("batteryMode",     batteryMode)
            putBoolean("fpsCap",          fpsCap)
            apply()
        }
    }
}
