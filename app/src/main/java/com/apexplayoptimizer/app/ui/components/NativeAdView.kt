package com.apexplayoptimizer.app.ui.components

import android.util.Log
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.apexplayoptimizer.app.data.AdUnitIds
import com.apexplayoptimizer.app.ui.theme.CardBorder
import com.apexplayoptimizer.app.ui.theme.Surface
import com.apexplayoptimizer.app.ui.theme.TextMuted
import com.apexplayoptimizer.app.ui.theme.TextPrimary
import com.apexplayoptimizer.app.ui.theme.TextSecondary
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

private const val TAG = "NativeAdView"

/**
 * Composable that displays a Native Advanced ad from AdMob.
 * Shows a loading placeholder until the ad loads.
 */
@Composable
fun NativeAdView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val nativeAd = remember { mutableSetOf<NativeAd>() }

    DisposableEffect(Unit) {
        onDispose {
            nativeAd.forEach { it.destroy() }
            nativeAd.clear()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val adView = NativeAdView(ctx)

                val adLoader = AdLoader.Builder(ctx, AdUnitIds.NATIVE_ADVANCED)
                    .forNativeAd { ad ->
                        Log.d(TAG, "✅ Native ad loaded")
                        nativeAd.forEach { it.destroy() }
                        nativeAd.clear()
                        nativeAd.add(ad)
                        populateNativeAdView(ad, adView)
                        adView.visibility = View.VISIBLE
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e(TAG, "❌ Native ad failed: ${error.message} (code: ${error.code})")
                            adView.visibility = View.GONE
                        }
                        override fun onAdClicked() {
                            Log.d(TAG, "Native ad clicked")
                        }
                        override fun onAdImpression() {
                            Log.d(TAG, "Native ad impression")
                        }
                    })
                    .withNativeAdOptions(
                        NativeAdOptions.Builder()
                            .setRequestCustomMuteThisAd(true)
                            .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                            .build()
                    )
                    .build()

                adLoader.loadAd(AdRequest.Builder().build())
                adView
            },
            update = {}
        )

        // Show placeholder while loading
        if (nativeAd.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📢", fontSize = 24.sp)
                Text("Sponsored", fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    // Set the media view (main image/video)
    val mediaView = com.google.android.gms.ads.nativead.MediaView(adView.context)
    adView.mediaView = mediaView

    // Set headline
    val headlineView = android.widget.TextView(adView.context).apply {
        text = nativeAd.headline
        setTextColor(android.graphics.Color.WHITE)
        textSize = 14f
        setTypeface(null, android.graphics.Typeface.BOLD)
    }
    adView.headlineView = headlineView

    // Set body
    nativeAd.body?.let { body ->
        val bodyView = android.widget.TextView(adView.context).apply {
            text = body
            setTextColor(android.graphics.Color.LTGRAY)
            textSize = 12f
        }
        adView.bodyView = bodyView
    }

    // Set call to action
    nativeAd.callToAction?.let { cta ->
        val ctaView = android.widget.TextView(adView.context).apply {
            text = cta
            setTextColor(android.graphics.Color.WHITE)
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setBackgroundColor(android.graphics.Color.parseColor("#4A90E2"))
            setPadding(16, 8, 16, 8)
        }
        adView.callToActionView = ctaView
    }

    // Set icon
    nativeAd.icon?.let { icon ->
        val iconView = android.widget.ImageView(adView.context).apply {
            setImageDrawable(icon.drawable)
        }
        adView.iconView = iconView
    }

    // Add views to layout
    val layout = android.widget.LinearLayout(adView.context).apply {
        orientation = android.widget.LinearLayout.HORIZONTAL
        setPadding(12, 12, 12, 12)

        // Left side: icon + text
        val leftLayout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            weightSum = 1f
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            addView(headlineView)
            nativeAd.body?.let { addView(adView.bodyView) }
        }

        addView(leftLayout)

        // Right side: CTA
        nativeAd.callToAction?.let {
            addView(adView.callToActionView)
        }
    }

    adView.removeAllViews()
    adView.addView(layout)
    adView.setNativeAd(nativeAd)
}
