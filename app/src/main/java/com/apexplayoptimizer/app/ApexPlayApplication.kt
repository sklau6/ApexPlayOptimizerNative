package com.apexplayoptimizer.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.apexplayoptimizer.app.data.AppOpenAdManager
import com.apexplayoptimizer.app.data.InterstitialAdManager
import com.apexplayoptimizer.app.data.RewardedAdManager
import com.apexplayoptimizer.app.data.RewardedInterstitialAdManager
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.Lifecycle

private const val TAG = "ApexPlayApp"

class ApexPlayApplication : Application(), DefaultLifecycleObserver {

    private var currentActivity: Activity? = null
    private var backgroundTimeMs = 0L

    // Don't show app-open ad if user was away for less than 3 seconds (quick task switch)
    private val minBackgroundMs = 3_000L

    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Track current activity — use onActivityStarted so currentActivity is set
        // before ProcessLifecycleOwner.onStart fires (onResume comes after onStart)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity)  { currentActivity = activity }
            override fun onActivityResumed(activity: Activity)  { currentActivity = activity }
            override fun onActivityPaused(activity: Activity)   {}
            override fun onActivityCreated(activity: Activity, b: Bundle?) {}
            override fun onActivityStopped(activity: Activity)  {}
            override fun onActivitySaveInstanceState(activity: Activity, b: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (currentActivity == activity) currentActivity = null
            }
        })

        // ── Initialize AdMob SDK on a background thread (Dispatchers.IO) ──────────
        // Official recommendation per Google YouTube tutorials (video: giwn21KeYoQ,
        // kSh8-quhFHM). Avoids blocking the main thread during SDK startup.
        // All ad preloads happen back on the main thread inside the init callback.
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@ApexPlayApplication) { initStatus ->
                Log.d(TAG, "MobileAds initialized — adapters: ${initStatus.adapterStatusMap.keys}")
                CoroutineScope(Dispatchers.Main).launch {
                    // Cold-start App Open: once the ad loads, if the app is still in the
                    // foreground (ProcessLifecycleOwner STARTED), show it immediately.
                    // Per tutorial aZfdufLRZYU — monetise the loading / splash phase.
                    AppOpenAdManager.preload(this@ApexPlayApplication, onLoaded = {
                        if (ProcessLifecycleOwner.get().lifecycle.currentState
                                .isAtLeast(Lifecycle.State.STARTED)
                        ) {
                            currentActivity?.let { AppOpenAdManager.showIfAvailable(it) }
                        }
                    })
                    InterstitialAdManager.preload(this@ApexPlayApplication)
                    RewardedAdManager.preload(this@ApexPlayApplication)
                    RewardedInterstitialAdManager.preload(this@ApexPlayApplication)
                    Log.d(TAG, "All ad units preloaded")
                }
            }
        }
        Log.d(TAG, "Application created, MobileAds init dispatched to IO thread")
    }

    override fun onStop(owner: LifecycleOwner) {
        backgroundTimeMs = System.currentTimeMillis()
    }

    override fun onStart(owner: LifecycleOwner) {
        val elapsed = System.currentTimeMillis() - backgroundTimeMs
        if (backgroundTimeMs > 0 && elapsed < minBackgroundMs) {
            Log.d(TAG, "Quick task switch (${elapsed}ms), skipping app-open ad")
            return
        }
        currentActivity?.let {
            Log.d(TAG, "Foreground after ${elapsed}ms — attempting app-open ad")
            AppOpenAdManager.showIfAvailable(it)
        }
    }
}
