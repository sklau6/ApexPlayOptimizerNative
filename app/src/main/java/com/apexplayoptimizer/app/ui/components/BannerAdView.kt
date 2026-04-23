package com.apexplayoptimizer.app.ui.components

import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.apexplayoptimizer.app.data.AdUnitIds
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private const val TAG = "BannerAdView"

/**
 * Composable banner ad using AdMob's [AdView] with large anchored adaptive sizing.
 *
 * Follows the official Google Jetpack Compose pattern (YouTube tutorial mcHbnWClWFc):
 * - AdView is created once via [remember]
 * - Ad size uses [AdSize.getLargeAnchoredAdaptiveBannerAdSize] (replaces the now-deprecated
 *   getCurrentOrientationAnchoredAdaptiveBannerAdSize per SDK 24.x release notes)
 * - [LaunchedEffect] triggers load once after the first composition
 * - Lifecycle observer handles pause / resume
 * - [DisposableEffect] destroys the AdView when the composable leaves the tree
 */
@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var adLoaded by remember { mutableStateOf(false) }

    // Compute screen width in dp (API-safe)
    val widthDp = remember {
        val wm = context.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            (bounds.width() / context.resources.displayMetrics.density).toInt()
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getMetrics(metrics)
            (metrics.widthPixels / metrics.density).toInt()
        }
    }

    // Create AdView once — set unit ID and current-orientation adaptive size
    val adView = remember {
        AdView(context).also { av ->
            av.adUnitId = AdUnitIds.BANNER
            av.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp))
            av.adListener = object : AdListener() {
                override fun onAdLoaded()    { adLoaded = true; Log.d(TAG, "Banner: loaded") }
                override fun onAdFailedToLoad(err: LoadAdError) {
                    Log.e(TAG, "Banner: failed code=${err.code} domain=${err.domain} msg=${err.message}")
                }
                override fun onAdClicked()    { Log.d(TAG, "Banner: clicked") }
                override fun onAdImpression() { Log.d(TAG, "Banner: impression") }
                override fun onAdOpened()     { Log.d(TAG, "Banner: opened") }
                override fun onAdClosed()     { Log.d(TAG, "Banner: closed") }
            }
        }
    }

    // Load the ad once after the first composition (not inside remember — per official pattern)
    LaunchedEffect(adView) {
        Log.d(TAG, "Banner: loading ad (widthDp=$widthDp)")
        adView.loadAd(AdRequest.Builder().build())
    }

    // Pause / resume with the host Activity lifecycle; destroy on dispose
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE  -> adView.pause()
                Lifecycle.Event.ON_RESUME -> adView.resume()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
            Log.d(TAG, "Banner: destroyed")
        }
    }

    if (adLoaded) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory  = { adView }
        )
    }
}
