package com.apexplayoptimizer.app.data

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

// ── Ad Unit IDs (6 unique units) ─────────────────────────────────────────────
object AdUnitIds {
    const val APP_OPEN              = "ca-app-pub-3863350102940366/6807094881"
    const val BANNER                = "ca-app-pub-3863350102940366/9161795486"
    const val INTERSTITIAL          = "ca-app-pub-3863350102940366/3059421567"
    const val NATIVE_ADVANCED       = "ca-app-pub-3863350102940366/6212198512"
    const val REWARDED              = "ca-app-pub-3863350102940366/8838361857"
    const val REWARDED_INTERSTITIAL = "ca-app-pub-3863350102940366/3597431434"
}

private const val TAG = "AdManager"
private val mainHandler = Handler(Looper.getMainLooper())

// ── Frequency Capping ────────────────────────────────────────────────────────
// Prevents over-serving ads and protects against high-CTR policy violations.
object AdFrequencyManager {
    // Per tutorial Ccrj8D0m1KA ("Best practices to win with interstitial ads"):
    // keep gaps short enough to maximise revenue while still respecting UX.
    private const val INTERSTITIAL_COOLDOWN_MS   = 30_000L   // 30 s between interstitials
    private const val REWARDED_INTER_COOLDOWN_MS = 60_000L   // 60 s between rewarded interstitials
    // App Open frequency cap removed — Google's own SDK manages fill rate.
    // Per tutorial aZfdufLRZYU: show on every valid foreground transition.

    private var lastInterstitialMs  = 0L
    private var lastRewardedInterMs = 0L

    fun canShowInterstitial(): Boolean =
        System.currentTimeMillis() - lastInterstitialMs > INTERSTITIAL_COOLDOWN_MS

    fun canShowRewardedInterstitial(): Boolean =
        System.currentTimeMillis() - lastRewardedInterMs > REWARDED_INTER_COOLDOWN_MS

    fun recordInterstitial()         { lastInterstitialMs  = System.currentTimeMillis() }
    fun recordRewardedInterstitial() { lastRewardedInterMs = System.currentTimeMillis() }
}

// ── Exponential Backoff helper ───────────────────────────────────────────────
private fun retryWithBackoff(
    tag: String,
    attempt: Int,
    maxAttempts: Int = 5,
    baseDelayMs: Long = 15_000L,
    block: () -> Unit
) {
    if (attempt >= maxAttempts) {
        Log.w(TAG, "$tag: max retry attempts ($maxAttempts) reached, giving up")
        return
    }
    val delay = baseDelayMs * (1L shl attempt.coerceAtMost(4)) // 15s, 30s, 60s, 120s, 240s
    Log.d(TAG, "$tag: retrying in ${delay / 1000}s (attempt ${attempt + 1}/$maxAttempts)")
    mainHandler.postDelayed(block, delay)
}

private fun logLoadError(name: String, err: LoadAdError) {
    Log.e(TAG, "$name load failed: code=${err.code} domain=${err.domain} " +
            "message=${err.message} cause=${err.cause?.message ?: "none"}")
}

// ═══════════════════════════════════════════════════════════════════════════════
// 1. APP OPEN — shown when user returns from background
// ═══════════════════════════════════════════════════════════════════════════════

object AppOpenAdManager {
    private var ad: AppOpenAd? = null
    private var loading = false
    private var loadTimeMs = 0L
    private var isShowingAd = false
    private var retryAttempt = 0

    val isReady: Boolean get() = ad != null && !isExpired()

    private fun isExpired(): Boolean =
        System.currentTimeMillis() - loadTimeMs > 4 * 3_600_000L // 4 hours

    fun preload(ctx: Context, onLoaded: (() -> Unit)? = null) {
        if (ad != null || loading) return
        loading = true
        Log.d(TAG, "AppOpen: loading...")
        AppOpenAd.load(
            ctx, AdUnitIds.APP_OPEN, AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(appOpenAd: AppOpenAd) {
                    ad = appOpenAd; loading = false; retryAttempt = 0
                    loadTimeMs = System.currentTimeMillis()
                    Log.d(TAG, "AppOpen: loaded successfully")
                    onLoaded?.invoke()
                }
                override fun onAdFailedToLoad(err: LoadAdError) {
                    ad = null; loading = false
                    logLoadError("AppOpen", err)
                    retryWithBackoff("AppOpen", retryAttempt++) { preload(ctx) }
                }
            }
        )
    }

    fun showIfAvailable(activity: Activity) {
        if (isShowingAd) return
        val current = ad
        if (current == null || isExpired()) { preload(activity); return }

        isShowingAd = true
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "AppOpen: dismissed")
                ad = null; isShowingAd = false; preload(activity)
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                Log.e(TAG, "AppOpen: show failed: ${e.message} (code=${e.code})")
                ad = null; isShowingAd = false; preload(activity)
            }
            override fun onAdShowedFullScreenContent() { Log.d(TAG, "AppOpen: showed") }
            override fun onAdClicked()    { Log.d(TAG, "AppOpen: clicked") }
            override fun onAdImpression() { Log.d(TAG, "AppOpen: impression") }
        }
        current.show(activity)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 2. BANNER — handled by BannerAdView composable (uses AdUnitIds.BANNER)
// ═══════════════════════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════════════════════
// 3. INTERSTITIAL — shown after boost / GFX apply / sensitivity activate
// ═══════════════════════════════════════════════════════════════════════════════

object InterstitialAdManager {
    private var ad: InterstitialAd? = null
    private var loading = false
    private var retryAttempt = 0
    val isReady: Boolean get() = ad != null

    fun preload(ctx: Context) {
        if (ad != null || loading) return
        loading = true
        Log.d(TAG, "Interstitial: loading...")
        InterstitialAd.load(
            ctx, AdUnitIds.INTERSTITIAL, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    ad = interstitialAd; loading = false; retryAttempt = 0
                    Log.d(TAG, "Interstitial: loaded successfully")
                }
                override fun onAdFailedToLoad(err: LoadAdError) {
                    ad = null; loading = false
                    logLoadError("Interstitial", err)
                    retryWithBackoff("Interstitial", retryAttempt++) { preload(ctx) }
                }
            }
        )
    }

    fun show(activity: Activity, onDismiss: () -> Unit = {}) {
        if (!AdFrequencyManager.canShowInterstitial()) {
            Log.d(TAG, "Interstitial: frequency capped, skipping"); onDismiss(); return
        }
        val current = ad
        if (current == null) {
            Log.w(TAG, "Interstitial: not ready"); onDismiss(); preload(activity); return
        }
        AdFrequencyManager.recordInterstitial()
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial: dismissed"); ad = null; onDismiss(); preload(activity)
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                Log.e(TAG, "Interstitial: show failed: ${e.message}"); ad = null; onDismiss(); preload(activity)
            }
            override fun onAdShowedFullScreenContent() { Log.d(TAG, "Interstitial: showed") }
            override fun onAdClicked()    { Log.d(TAG, "Interstitial: clicked") }
            override fun onAdImpression() { Log.d(TAG, "Interstitial: impression") }
        }
        current.show(activity)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 4. NATIVE ADVANCED — handled by NativeAdCard composable (uses AdUnitIds.NATIVE_ADVANCED)
// ═══════════════════════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════════════════════
// 5. REWARDED — watch ad for credits (user-initiated, no frequency cap)
// ═══════════════════════════════════════════════════════════════════════════════

object RewardedAdManager {
    private var ad: RewardedAd? = null
    private var loading = false
    private var retryAttempt = 0
    val isReady: Boolean get() = ad != null

    fun preload(ctx: Context) {
        if (ad != null || loading) return
        loading = true
        Log.d(TAG, "Rewarded: loading...")
        RewardedAd.load(
            ctx, AdUnitIds.REWARDED, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    ad = rewardedAd; loading = false; retryAttempt = 0
                    Log.d(TAG, "Rewarded: loaded successfully")
                }
                override fun onAdFailedToLoad(err: LoadAdError) {
                    ad = null; loading = false
                    logLoadError("Rewarded", err)
                    retryWithBackoff("Rewarded", retryAttempt++) { preload(ctx) }
                }
            }
        )
    }

    fun show(activity: Activity, onRewarded: (Int) -> Unit, onDismiss: () -> Unit) {
        val current = ad
        if (current == null) {
            Log.w(TAG, "Rewarded: not ready"); onDismiss(); preload(activity); return
        }
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded: dismissed"); ad = null; onDismiss(); preload(activity)
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                Log.e(TAG, "Rewarded: show failed: ${e.message}"); ad = null; onDismiss(); preload(activity)
            }
            override fun onAdShowedFullScreenContent() { Log.d(TAG, "Rewarded: showed") }
            override fun onAdClicked()    { Log.d(TAG, "Rewarded: clicked") }
            override fun onAdImpression() { Log.d(TAG, "Rewarded: impression") }
        }
        current.show(activity) { reward ->
            Log.d(TAG, "Rewarded: earned ${reward.amount} ${reward.type}")
            onRewarded(reward.amount)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 6. REWARDED INTERSTITIAL — shown on transitions (e.g. after optimization)
// ═══════════════════════════════════════════════════════════════════════════════

object RewardedInterstitialAdManager {
    private var ad: RewardedInterstitialAd? = null
    private var loading = false
    private var retryAttempt = 0
    val isReady: Boolean get() = ad != null

    fun preload(ctx: Context) {
        if (ad != null || loading) return
        loading = true
        Log.d(TAG, "RewardedInterstitial: loading...")
        RewardedInterstitialAd.load(
            ctx, AdUnitIds.REWARDED_INTERSTITIAL, AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(riAd: RewardedInterstitialAd) {
                    ad = riAd; loading = false; retryAttempt = 0
                    Log.d(TAG, "RewardedInterstitial: loaded successfully")
                }
                override fun onAdFailedToLoad(err: LoadAdError) {
                    ad = null; loading = false
                    logLoadError("RewardedInterstitial", err)
                    retryWithBackoff("RewardedInterstitial", retryAttempt++) { preload(ctx) }
                }
            }
        )
    }

    fun show(activity: Activity, onRewarded: () -> Unit = {}, onDismiss: () -> Unit = {}) {
        if (!AdFrequencyManager.canShowRewardedInterstitial()) {
            Log.d(TAG, "RewardedInterstitial: frequency capped, skipping"); onDismiss(); return
        }
        val current = ad
        if (current == null) {
            Log.w(TAG, "RewardedInterstitial: not ready"); onDismiss(); preload(activity); return
        }
        AdFrequencyManager.recordRewardedInterstitial()
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "RewardedInterstitial: dismissed"); ad = null; onDismiss(); preload(activity)
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                Log.e(TAG, "RewardedInterstitial: show failed: ${e.message}"); ad = null; onDismiss(); preload(activity)
            }
            override fun onAdShowedFullScreenContent() { Log.d(TAG, "RewardedInterstitial: showed") }
            override fun onAdClicked()    { Log.d(TAG, "RewardedInterstitial: clicked") }
            override fun onAdImpression() { Log.d(TAG, "RewardedInterstitial: impression") }
        }
        current.show(activity) {
            Log.d(TAG, "RewardedInterstitial: user earned reward")
            onRewarded()
        }
    }
}
