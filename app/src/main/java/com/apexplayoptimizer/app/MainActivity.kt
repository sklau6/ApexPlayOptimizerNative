package com.apexplayoptimizer.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.apexplayoptimizer.app.data.SettingsPrefs
import com.apexplayoptimizer.app.ui.navigation.AppNavigation
import com.apexplayoptimizer.app.ui.theme.ApexPlayTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val isDark = SettingsPrefs.getBoolean(this, "darkTheme", true)

        // AdMob SDK is initialized in ApexPlayApplication.onCreate() — earliest possible.
        // All ad preloads are triggered there after the init callback fires.
        setContent {
            ApexPlayTheme(isDark = isDark) {
                AppNavigation()
            }
        }
    }
}
