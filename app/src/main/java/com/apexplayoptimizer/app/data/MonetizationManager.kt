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
    private const val KEY_TRIAL_END  = "plus_trial_end"
    private const val KEY_DAILY_CREDITS_DATE = "daily_credits_date"
    private const val KEY_DAILY_CREDITS_LEFT = "daily_credits_left"
    private const val KEY_AD_VIEWS = "daily_ad_views"
    private const val KEY_AD_DATE  = "ad_view_date"
    const val MAX_DAILY_ADS = 3

    fun getTier(ctx: Context): UserTier {
        val savedTier = runCatching {
            UserTier.valueOf(
                ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getString(KEY_TIER, "FREE") ?: "FREE"
            )
        }.getOrDefault(UserTier.FREE)
        
        if (savedTier == UserTier.FREE) {
            val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            if (System.currentTimeMillis() < p.getLong(KEY_TRIAL_END, 0L)) {
                return UserTier.PLUS
            }
        }
        return savedTier
    }

    fun setTier(ctx: Context, tier: UserTier) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_TIER, tier.name).apply()

    fun startPlusTrial(ctx: Context) {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().putLong(KEY_TRIAL_END, System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000).apply()
    }

    fun getCredits(ctx: Context): Int {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val purchased = p.getInt(KEY_CREDITS, 0)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val daily = if (p.getString(KEY_DAILY_CREDITS_DATE, "") == today) p.getInt(KEY_DAILY_CREDITS_LEFT, 3) else 3
        return purchased + daily
    }

    fun getTokens(ctx: Context): Int {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val purchased = p.getInt(KEY_TOKENS, 0)
        val tier = getTier(ctx)
        if (tier == UserTier.PLUS) {
            val currentMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US).format(java.util.Date())
            val left = if (p.getString("plus_tokens_month", "") == currentMonth) p.getInt("plus_tokens_left", 100) else 100
            return purchased + left
        }
        return purchased
    }

    fun addCredits(ctx: Context, amount: Int) {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().putInt(KEY_CREDITS, p.getInt(KEY_CREDITS, 0) + amount).apply()
    }

    fun addTokens(ctx: Context, amount: Int) {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().putInt(KEY_TOKENS, p.getInt(KEY_TOKENS, 0) + amount).apply()
    }

    fun useCredit(ctx: Context): Boolean {
        val tier = getTier(ctx)
        if (tier == UserTier.PRO || tier == UserTier.PLUS) return true

        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val dailyLeft = if (p.getString(KEY_DAILY_CREDITS_DATE, "") == today) p.getInt(KEY_DAILY_CREDITS_LEFT, 3) else 3
        
        if (dailyLeft > 0) {
            p.edit().putString(KEY_DAILY_CREDITS_DATE, today).putInt(KEY_DAILY_CREDITS_LEFT, dailyLeft - 1).apply()
            return true
        }
        
        val cur = p.getInt(KEY_CREDITS, 0)
        if (cur > 0) {
            p.edit().putInt(KEY_CREDITS, cur - 1).apply()
            return true
        }
        return false
    }

    fun useToken(ctx: Context): Boolean {
        val tier = getTier(ctx)
        if (tier == UserTier.PRO) return true

        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (tier == UserTier.PLUS) {
            val currentMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US).format(java.util.Date())
            val savedMonth = p.getString("plus_tokens_month", "")
            if (savedMonth != currentMonth) {
                p.edit().putString("plus_tokens_month", currentMonth).putInt("plus_tokens_left", 100).apply()
            }
            val left = p.getInt("plus_tokens_left", 100)
            if (left > 0) {
                p.edit().putInt("plus_tokens_left", left - 1).apply()
                return true
            }
        }

        val cur = p.getInt(KEY_TOKENS, 0)
        if (cur > 0) {
            p.edit().putInt(KEY_TOKENS, cur - 1).apply()
            return true
        }
        return false
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
