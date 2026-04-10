package com.apexplayoptimizer.app.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

// ── Ad Unit IDs ───────────────────────────────────────────────────────────────
object AdUnitIds {
    // App Open
    const val APP_OPEN          = "ca-app-pub-5428983411611852/4062951434"
    // Banner
    const val BANNER            = "ca-app-pub-5428983411611852/4788875593"
    // Interstitial (shown after boost / between screens)
    const val INTERSTITIAL      = "ca-app-pub-5428983411611852/5787096644"
    // Native Advanced
    const val NATIVE_ADVANCED   = "ca-app-pub-5428983411611852/3464185637"
    // Rewarded (shown when user taps "Watch ad for credits")
    const val REWARDED          = "ca-app-pub-5428983411611852/1255261591"
    // Rewarded Interstitial
    const val REWARDED_INTERSTITIAL = "ca-app-pub-5428983411611852/8221688294"
}

private const val TAG = "AdManager"

// ── Interstitial ──────────────────────────────────────────────────────────────

object InterstitialAdManager {

    private var ad: InterstitialAd? = null
    private var loading = false

    val isReady: Boolean get() = ad != null

    fun preload(ctx: Context) {
        if (ad != null || loading) {
            Log.d(TAG, "Interstitial preload skipped: ad=${ad != null}, loading=$loading")
            return
        }
        loading = true
        Log.d(TAG, "📥 Loading interstitial ad...")
        InterstitialAd.load(
            ctx, AdUnitIds.INTERSTITIAL, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    ad = interstitialAd
                    loading = false
                    Log.d(TAG, "✅ Interstitial ad loaded")
                }
                override fun onAdFailedToLoad(err: LoadAdError) {
                    ad = null
                    loading = false
                    Log.e(TAG, "❌ Interstitial failed to load: ${err.message} (code: ${err.code}, domain: ${err.domain})")
                    // Retry after delay
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        preload(ctx)
                    }, 30000) // Retry after 30 seconds
                }
            }
        )
    }

    /** Show the interstitial if ready, then reload for next time. [onDismiss] is called when done. */
    fun show(activity: Activity, onDismiss: () -> Unit = {}) {
        val current = ad
        if (current == null) {
            Log.w(TAG, "⚠️ Interstitial not ready, skipping show")
            onDismiss()
            preload(activity)
            return
        }
        Log.d(TAG, "▶️ Showing interstitial ad")
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial dismissed")
                ad = null
                onDismiss()
                preload(activity)
            }
            override fun onAdFailedToShowFullScreenContent(err: AdError) {
                Log.e(TAG, "❌ Interstitial show failed: ${err.message}")
                ad = null
                onDismiss()
                preload(activity)
            }
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial showed full screen")
            }
            override fun onAdClicked() {
                Log.d(TAG, "Interstitial clicked")
            }
            override fun onAdImpression() {
                Log.d(TAG, "Interstitial impression recorded")
            }
        }
        current.show(activity)
    }
}

// ── Rewarded ──────────────────────────────────────────────────────────────────

object RewardedAdManager {

    private var ad: RewardedAd? = null
    private var loading = false

    val isReady: Boolean get() = ad != null

    fun preload(ctx: Context) {
        if (ad != null || loading) {
            Log.d(TAG, "Rewarded preload skipped: ad=${ad != null}, loading=$loading")
            return
        }
        loading = true
        Log.d(TAG, "📥 Loading rewarded ad...")
        RewardedAd.load(
            ctx, AdUnitIds.REWARDED, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    ad = rewardedAd
                    loading = false
                    Log.d(TAG, "✅ Rewarded ad loaded")
                }
                override fun onAdFailedToLoad(err: LoadAdError) {
                    ad = null
                    loading = false
                    Log.e(TAG, "❌ Rewarded failed to load: ${err.message} (code: ${err.code}, domain: ${err.domain})")
                    // Retry after delay
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        preload(ctx)
                    }, 30000) // Retry after 30 seconds
                }
            }
        )
    }

    /**
     * Show the rewarded ad.
     * [onRewarded] is called with the reward amount when the user earns the reward.
     * [onDismiss] is always called after the ad closes (rewarded or not).
     */
    fun show(activity: Activity, onRewarded: (Int) -> Unit, onDismiss: () -> Unit) {
        val current = ad
        if (current == null) {
            Log.w(TAG, "⚠️ Rewarded ad not ready, skipping show")
            onDismiss()
            preload(activity)
            return
        }
        Log.d(TAG, "▶️ Showing rewarded ad")
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded ad dismissed")
                ad = null
                onDismiss()
                preload(activity)
            }
            override fun onAdFailedToShowFullScreenContent(err: AdError) {
                Log.e(TAG, "❌ Rewarded show failed: ${err.message}")
                ad = null
                onDismiss()
                preload(activity)
            }
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad showed full screen")
            }
            override fun onAdClicked() {
                Log.d(TAG, "Rewarded ad clicked")
            }
            override fun onAdImpression() {
                Log.d(TAG, "Rewarded ad impression recorded")
            }
        }
        current.show(activity) { rewardItem ->
            Log.d(TAG, "✅ User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            onRewarded(rewardItem.amount)
        }
    }
}
