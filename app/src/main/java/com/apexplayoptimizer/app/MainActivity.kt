package com.apexplayoptimizer.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.apexplayoptimizer.app.data.AppOpenAdManager
import com.apexplayoptimizer.app.data.InterstitialAdManager
import com.apexplayoptimizer.app.data.RewardedAdManager
import com.apexplayoptimizer.app.data.RewardedInterstitialAdManager
import com.apexplayoptimizer.app.ui.navigation.AppNavigation
import com.apexplayoptimizer.app.ui.theme.ApexPlayTheme
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize AdMob SDK, then preload ALL ad units for maximum revenue
        MobileAds.initialize(this) {
            // Preload all 6 ad types
            AppOpenAdManager.preload(this)
            InterstitialAdManager.preload(this)
            RewardedAdManager.preload(this)
            RewardedInterstitialAdManager.preload(this)
        }

        setContent {
            ApexPlayTheme {
                AppNavigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Show App Open ad when app comes to foreground (if ready)
        AppOpenAdManager.show(this)
    }
}
