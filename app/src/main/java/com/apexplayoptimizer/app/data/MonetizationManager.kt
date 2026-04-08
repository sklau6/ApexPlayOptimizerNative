package com.apexplayoptimizer.app.data

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

enum class UserTier(val displayName: String, val monthly: String, val yearly: String) {
    FREE("Free",  "Free",       "Free"),
    PLUS("Plus",  "$4.99/mo",   "$3.33/mo"),
    PRO ("Pro",   "$29.99/mo",  "$19.99/mo")
}

object MonetizationManager {
    private const val PREFS        = "apex_monetization"
    private const val KEY_TIER     = "tier"
    private const val KEY_CREDITS  = "boost_credits"
    private const val KEY_TOKENS   = "speed_tokens"
    private const val KEY_AD_VIEWS = "daily_ad_views"
    private const val KEY_AD_DATE  = "ad_view_date"
    const val MAX_DAILY_ADS = 3

    fun getTier(ctx: Context): UserTier =
        runCatching {
            UserTier.valueOf(
                ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getString(KEY_TIER, "FREE") ?: "FREE"
            )
        }.getOrDefault(UserTier.FREE)

    fun setTier(ctx: Context, tier: UserTier) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_TIER, tier.name).apply()

    fun getCredits(ctx: Context): Int =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_CREDITS, 3)

    fun getTokens(ctx: Context): Int =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_TOKENS, 10)

    fun addCredits(ctx: Context, amount: Int) {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().putInt(KEY_CREDITS, p.getInt(KEY_CREDITS, 3) + amount).apply()
    }

    fun addTokens(ctx: Context, amount: Int) {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().putInt(KEY_TOKENS, p.getInt(KEY_TOKENS, 10) + amount).apply()
    }

    fun useCredit(ctx: Context): Boolean {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val cur = p.getInt(KEY_CREDITS, 3)
        if (cur <= 0) return false
        p.edit().putInt(KEY_CREDITS, cur - 1).apply()
        return true
    }

    fun getDailyAdViews(ctx: Context): Int {
        val p    = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val date = p.getString(KEY_AD_DATE, "")
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        return if (date == today) p.getInt(KEY_AD_VIEWS, 0) else 0
    }

    fun recordAdView(ctx: Context) {
        val p     = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        val views = getDailyAdViews(ctx)
        p.edit().putString(KEY_AD_DATE, today).putInt(KEY_AD_VIEWS, views + 1).apply()
    }

    fun canWatchAd(ctx: Context): Boolean = getDailyAdViews(ctx) < MAX_DAILY_ADS
}

@Composable
fun rememberUserTier(): MutableState<UserTier> {
    val ctx = LocalContext.current
    return remember { mutableStateOf(MonetizationManager.getTier(ctx)) }
}

@Composable
fun rememberBoostCredits(): MutableState<Int> {
    val ctx = LocalContext.current
    return remember { mutableStateOf(MonetizationManager.getCredits(ctx)) }
}
