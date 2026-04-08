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
// TODO: Replace all test IDs below with your real AdMob Ad Unit IDs.
//       Test IDs work only during development; real IDs are required for production.
object AdUnitIds {
    // Banner
    const val BANNER        = "ca-app-pub-3940256099942544/6300978111"   // TODO: replace
    // Interstitial (shown after boost / between screens)
    const val INTERSTITIAL  = "ca-app-pub-3940256099942544/1033173712"   // TODO: replace
    // Rewarded (shown when user taps "Watch ad for credits")
    const val REWARDED      = "ca-app-pub-3940256099942544/5224354917"   // TODO: replace
}

private const val TAG = "AdManager"

// ── Interstitial ──────────────────────────────────────────────────────────────

object InterstitialAdManager {

    private var ad: InterstitialAd? = null
    private var loading = false

    fun preload(ctx: Context) {
        if (ad != null || loading) return
        loading = true
        InterstitialAd.load(
            ctx, AdUnitIds.INTERSTITIAL, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    ad = interstitialAd
                    loading = false
                    Log.d(TAG, "Interstitial loaded")
                }
                override fun onAdFailedToLoad(err: LoadAdError) {
                    loading = false
                    Log.w(TAG, "Interstitial failed: ${err.message}")
                }
            }
        )
    }

    /** Show the interstitial if ready, then reload for next time. [onDismiss] is called when done. */
    fun show(activity: Activity, onDismiss: () -> Unit = {}) {
        val current = ad
        if (current == null) {
            onDismiss()
            preload(activity)
            return
        }
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                ad = null
                onDismiss()
                preload(activity)
            }
            override fun onAdFailedToShowFullScreenContent(err: AdError) {
                ad = null
                onDismiss()
                preload(activity)
                Log.w(TAG, "Interstitial show failed: ${err.message}")
            }
        }
        current.show(activity)
    }
}

// ── Rewarded ──────────────────────────────────────────────────────────────────

object RewardedAdManager {

    private var ad: RewardedAd? = null
    private var loading = false

    fun preload(ctx: Context) {
        if (ad != null || loading) return
        loading = true
        RewardedAd.load(
            ctx, AdUnitIds.REWARDED, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    ad = rewardedAd
                    loading = false
                    Log.d(TAG, "Rewarded loaded")
                }
                override fun onAdFailedToLoad(err: LoadAdError) {
                    loading = false
                    Log.w(TAG, "Rewarded failed: ${err.message}")
                }
            }
        )
    }

    val isReady: Boolean get() = ad != null

    /**
     * Show the rewarded ad.
     * [onRewarded] is called with the reward amount when the user earns the reward.
     * [onDismiss] is always called after the ad closes (rewarded or not).
     */
    fun show(activity: Activity, onRewarded: (Int) -> Unit, onDismiss: () -> Unit) {
        val current = ad
        if (current == null) {
            onDismiss()
            preload(activity)
            return
        }
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                ad = null
                onDismiss()
                preload(activity)
            }
            override fun onAdFailedToShowFullScreenContent(err: AdError) {
                ad = null
                onDismiss()
                preload(activity)
                Log.w(TAG, "Rewarded show failed: ${err.message}")
            }
        }
        current.show(activity) { rewardItem ->
            onRewarded(rewardItem.amount)
        }
    }
}
