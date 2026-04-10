package com.apexplayoptimizer.app.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

private const val TAG = "RewardedInterstitialAdManager"

/**
 * Manager for Rewarded Interstitial ads - full screen ads that reward users.
 * These show automatically at natural breaks (better than regular interstitials).
 */
object RewardedInterstitialAdManager {

    private var ad: RewardedInterstitialAd? = null
    private var loading = false

    val isReady: Boolean get() = ad != null

    fun preload(ctx: Context) {
        if (ad != null || loading) {
            Log.d(TAG, "Rewarded Interstitial preload skipped: ad=${ad != null}, loading=$loading")
            return
        }
        loading = true
        Log.d(TAG, "📥 Loading Rewarded Interstitial ad...")
        RewardedInterstitialAd.load(
            ctx, AdUnitIds.REWARDED_INTERSTITIAL, AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                    ad = rewardedInterstitialAd
                    loading = false
                    Log.d(TAG, "✅ Rewarded Interstitial ad loaded")
                }
                override fun onAdFailedToLoad(err: LoadAdError) {
                    ad = null
                    loading = false
                    Log.e(TAG, "❌ Rewarded Interstitial failed: ${err.message} (code: ${err.code})")
                    // Retry after 30 seconds
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        preload(ctx)
                    }, 30000)
                }
            }
        )
    }

    /**
     * Show rewarded interstitial ad if ready.
     * [onRewarded] called when user earns reward.
     * [onDismiss] called when ad closes (rewarded or not).
     */
    fun show(activity: Activity, onRewarded: (Int) -> Unit = {}, onDismiss: () -> Unit = {}) {
        val current = ad
        if (current == null) {
            Log.w(TAG, "⚠️ Rewarded Interstitial not ready")
            onDismiss()
            preload(activity)
            return
        }

        Log.d(TAG, "▶️ Showing Rewarded Interstitial ad")
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded Interstitial dismissed")
                ad = null
                onDismiss()
                preload(activity)
            }
            override fun onAdFailedToShowFullScreenContent(err: AdError) {
                Log.e(TAG, "❌ Rewarded Interstitial show failed: ${err.message}")
                ad = null
                onDismiss()
                preload(activity)
            }
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded Interstitial showed")
            }
            override fun onAdClicked() {
                Log.d(TAG, "Rewarded Interstitial clicked")
            }
            override fun onAdImpression() {
                Log.d(TAG, "Rewarded Interstitial impression")
            }
        }
        current.show(activity) { rewardItem ->
            Log.d(TAG, "✅ User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            onRewarded(rewardItem.amount)
        }
    }
}
