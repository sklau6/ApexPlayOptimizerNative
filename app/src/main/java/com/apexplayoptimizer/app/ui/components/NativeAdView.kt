package com.apexplayoptimizer.app.ui.components

import android.graphics.Color as AColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.apexplayoptimizer.app.data.AdUnitIds
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

private const val TAG = "NativeAdCard"
private const val MAX_RETRY = 3
private val retryHandler = Handler(Looper.getMainLooper())

@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    // isDisposed flag — per official Google Native Ad tutorial (Bta2FE1MFjE).
    // Prevents setting Compose state or running retries after the screen is disposed,
    // which would cause memory leaks and potential crashes.
    val disposed      = remember { BooleanArray(1) }
    val pendingRetry  = remember { mutableListOf<Runnable>() }

    DisposableEffect(Unit) {
        onDispose {
            disposed[0] = true
            pendingRetry.forEach { retryHandler.removeCallbacks(it) }
            pendingRetry.clear()
            nativeAd?.destroy()
            nativeAd = null
            Log.d(TAG, "NativeAd: destroyed")
        }
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            val adView = NativeAdView(context)
            adView.visibility = View.GONE   // hidden until an ad fills it
            adView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Root container with rounded dark background
            val containerBg = GradientDrawable().apply {
                setColor(AColor.parseColor("#1A1A2E"))
                cornerRadius = dp(context, 16).toFloat()
            }
            val container = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                background = containerBg
                setPadding(dp(context, 16), dp(context, 12), dp(context, 16), dp(context, 14))
            }

            // "Ad" badge
            val adBadge = TextView(context).apply {
                text = "Ad"
                setTextColor(AColor.parseColor("#888888"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                typeface = Typeface.DEFAULT_BOLD
            }
            container.addView(adBadge)

            // Media view (image/video)
            val mediaView = MediaView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(context, 180)
                ).also { it.topMargin = dp(context, 8) }
                setBackgroundColor(AColor.parseColor("#0F0F23"))
                clipToOutline = true
            }
            container.addView(mediaView)
            adView.mediaView = mediaView

            // Headline + Icon row
            val headlineRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = dp(context, 10) }
            }
            val iconView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(dp(context, 40), dp(context, 40))
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            headlineRow.addView(iconView)
            adView.iconView = iconView

            val headlineText = TextView(context).apply {
                setTextColor(AColor.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                typeface = Typeface.DEFAULT_BOLD
                maxLines = 1
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    .also { it.marginStart = dp(context, 10) }
            }
            headlineRow.addView(headlineText)
            adView.headlineView = headlineText
            container.addView(headlineRow)

            // Advertiser text
            val advertiserText = TextView(context).apply {
                setTextColor(AColor.parseColor("#777777"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                maxLines = 1
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = dp(context, 2) }
            }
            container.addView(advertiserText)
            adView.advertiserView = advertiserText

            // Body text
            val bodyText = TextView(context).apply {
                setTextColor(AColor.parseColor("#AAAAAA"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                maxLines = 2
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = dp(context, 6) }
            }
            container.addView(bodyText)
            adView.bodyView = bodyText

            // CTA button with rounded background
            val ctaBg = GradientDrawable().apply {
                setColor(AColor.parseColor("#4F46E5"))
                cornerRadius = dp(context, 10).toFloat()
            }
            val ctaBtn = Button(context).apply {
                setTextColor(AColor.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                typeface = Typeface.DEFAULT_BOLD
                background = ctaBg
                isAllCaps = false
                setPadding(dp(context, 16), dp(context, 12), dp(context, 16), dp(context, 12))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = dp(context, 10) }
            }
            container.addView(ctaBtn)
            adView.callToActionView = ctaBtn

            adView.addView(container)

            // Load function with retry
            fun loadNativeAd(attempt: Int = 0) {
                if (disposed[0]) return          // screen already gone — do nothing
                Log.d(TAG, "NativeAd: loading (attempt ${attempt + 1}/$MAX_RETRY)...")
                val adLoader = AdLoader.Builder(context, AdUnitIds.NATIVE_ADVANCED)
                    .forNativeAd { ad ->
                        if (disposed[0]) {
                            // Screen disposed before ad arrived — destroy immediately to
                            // prevent memory leak (per Google tutorial Bta2FE1MFjE)
                            ad.destroy()
                            Log.d(TAG, "NativeAd: screen disposed, ad discarded")
                            return@forNativeAd
                        }
                        nativeAd?.destroy()
                        nativeAd = ad
                        headlineText.text = ad.headline ?: ""
                        bodyText.text = ad.body ?: ""
                        ctaBtn.text = ad.callToAction ?: "Learn More"
                        advertiserText.text = ad.advertiser ?: ""
                        ad.icon?.drawable?.let { iconView.setImageDrawable(it) }
                        ad.mediaContent?.let { mediaView.mediaContent = it }
                        adView.setNativeAd(ad)
                        adView.visibility = View.VISIBLE  // show now that content is ready
                        Log.d(TAG, "NativeAd: loaded '${ad.headline}' advertiser=${ad.advertiser}")
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(err: LoadAdError) {
                            if (disposed[0]) return
                            Log.e(TAG, "NativeAd: failed code=${err.code} domain=${err.domain} msg=${err.message}")
                            if (attempt + 1 < MAX_RETRY) {
                                val delay = 15_000L * (1L shl attempt)
                                Log.d(TAG, "NativeAd: retrying in ${delay / 1000}s")
                                val runnable = Runnable { loadNativeAd(attempt + 1) }
                                pendingRetry.add(runnable)
                                retryHandler.postDelayed(runnable, delay)
                            }
                        }
                        override fun onAdClicked()    { Log.d(TAG, "NativeAd: clicked") }
                        override fun onAdImpression() { Log.d(TAG, "NativeAd: impression") }
                    })
                    .withNativeAdOptions(
                        NativeAdOptions.Builder()
                            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                            .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                            .build()
                    )
                    .build()
                adLoader.loadAd(AdRequest.Builder().build())
            }

            loadNativeAd()
            adView
        }
    )
}

private fun dp(context: android.content.Context, value: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value.toFloat(),
        context.resources.displayMetrics
    ).toInt()
}
