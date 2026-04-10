package com.apexplayoptimizer.app.ui.components

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.apexplayoptimizer.app.data.AdUnitIds
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private const val TAG = "BannerAdView"

/**
 * Returns an adaptive banner ad size that fits the full width of the screen.
 */
private fun getAdaptiveAdSize(context: Context): AdSize {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val metrics = DisplayMetrics()
    display.getMetrics(metrics)
    val widthPixels = metrics.widthPixels
    val density = metrics.density
    val widthDp = (widthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
}

/**
 * Composable banner ad using AdMob's [AdView] with adaptive sizing.
 * Uses [AdUnitIds.BANNER] with proper error handling and logging.
 */
@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory  = { context ->
            val adSize = getAdaptiveAdSize(context)
            Log.d(TAG, "Creating banner with adaptive size: ${adSize.width}x${adSize.height}")
            AdView(context).apply {
                setAdSize(adSize)
                adUnitId = AdUnitIds.BANNER
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d(TAG, "✅ Banner ad loaded successfully")
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.e(TAG, "❌ Banner ad failed to load: ${error.message} (code: ${error.code}, domain: ${error.domain})")
                        // Common error codes:
                        // 0 = INTERNAL_ERROR, 1 = INVALID_REQUEST, 2 = NETWORK_ERROR, 3 = NO_FILL
                    }
                    override fun onAdClicked() {
                        Log.d(TAG, "Banner ad clicked")
                    }
                    override fun onAdClosed() {
                        Log.d(TAG, "Banner ad closed")
                    }
                    override fun onAdImpression() {
                        Log.d(TAG, "Banner ad impression recorded")
                    }
                    override fun onAdOpened() {
                        Log.d(TAG, "Banner ad opened")
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            // Reload ad when update is called (e.g., on configuration changes)
            adView.loadAd(AdRequest.Builder().build())
        }
    )
}
