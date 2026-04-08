package com.apexplayoptimizer.app.data

import android.content.Context
import org.json.JSONObject

data class GFXConfig(
    val quality:    String   = "HD",
    val fps:        String   = "60fps",
    val resolution: String   = "1080p",
    val style:      String   = "Default",
    val shadows:    String   = "Low",
    val antiAlias:  String   = "Off",
    val sliderVals: List<Int> = listOf(85, 70, 40, 65, 55, 60)
)

object GFXConfigManager {
    private const val PREFS = "apex_gfx"
    private const val KEY   = "gfx_config"

    fun save(ctx: Context, cfg: GFXConfig) {
        val json = JSONObject().apply {
            put("quality",    cfg.quality)
            put("fps",        cfg.fps)
            put("resolution", cfg.resolution)
            put("style",      cfg.style)
            put("shadows",    cfg.shadows)
            put("antiAlias",  cfg.antiAlias)
            put("sliders",    cfg.sliderVals.joinToString(","))
        }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, json.toString()).apply()
    }

    fun load(ctx: Context): GFXConfig {
        val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return GFXConfig()
        return try {
            val j = JSONObject(raw)
            GFXConfig(
                quality    = j.optString("quality",    "HD"),
                fps        = j.optString("fps",        "60fps"),
                resolution = j.optString("resolution", "1080p"),
                style      = j.optString("style",      "Default"),
                shadows    = j.optString("shadows",    "Low"),
                antiAlias  = j.optString("antiAlias",  "Off"),
                sliderVals = j.optString("sliders", "85,70,40,65,55,60")
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .let { if (it.size == 6) it else listOf(85, 70, 40, 65, 55, 60) }
            )
        } catch (_: Exception) { GFXConfig() }
    }

    fun isSaved(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(KEY)
}
