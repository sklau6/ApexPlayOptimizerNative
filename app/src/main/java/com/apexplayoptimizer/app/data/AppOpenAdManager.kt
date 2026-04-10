package com.apexplayoptimizer.app.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

private const val TAG = "AppOpenAdManager"

/**
 * Manager for App Open ads that show when the app is brought to foreground.
 */
object AppOpenAdManager {

    private var ad: AppOpenAd? = null
    private var loading = false
    private var wasAdShown = false

    val isReady: Boolean get() = ad != null

    fun preload(ctx: Context) {
        if (ad != null || loading) {
            Log.d(TAG, "App Open preload skipped: ad=${ad != null}, loading=$loading")
            return
        }
        loading = true
        Log.d(TAG, "📥 Loading App Open ad...")
        AppOpenAd.load(
            ctx, AdUnitIds.APP_OPEN, AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(appOpenAd: AppOpenAd) {
                    ad = appOpenAd
                    loading = false
                    Log.d(TAG, "✅ App Open ad loaded")
                }
                override fun onAdFailedToLoad(err: LoadAdError) {
                    ad = null
                    loading = false
                    Log.e(TAG, "❌ App Open failed: ${err.message} (code: ${err.code})")
                    // Retry after 30 seconds
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        preload(ctx)
                    }, 30000)
                }
            }
        )
    }

    /**
     * Show app open ad if ready. Call this in onResume() of MainActivity.
     * [onDismiss] called when ad is closed or not shown.
     */
    fun show(activity: Activity, onDismiss: () -> Unit = {}) {
        // Don't show more than once per session
        if (wasAdShown) {
            onDismiss()
            return
        }

        val current = ad
        if (current == null) {
            Log.w(TAG, "⚠️ App Open not ready")
            onDismiss()
            preload(activity)
            return
        }

        Log.d(TAG, "▶️ Showing App Open ad")
        wasAdShown = true
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "App Open dismissed")
                ad = null
                onDismiss()
                preload(activity)
            }
            override fun onAdFailedToShowFullScreenContent(err: AdError) {
                Log.e(TAG, "❌ App Open show failed: ${err.message}")
                ad = null
                onDismiss()
                preload(activity)
            }
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "App Open showed")
            }
            override fun onAdClicked() {
                Log.d(TAG, "App Open clicked")
            }
            override fun onAdImpression() {
                Log.d(TAG, "App Open impression")
            }
        }
        current.show(activity)
    }

    fun resetForNextSession() {
        wasAdShown = false
    }
}
