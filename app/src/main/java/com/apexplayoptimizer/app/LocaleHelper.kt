package com.apexplayoptimizer.app

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    private const val PREF_FILE = "apex_prefs"
    private const val PREF_LANGUAGE = "language"

    data class Language(val code: String, val nameRes: Int)

    val SUPPORTED_LANGUAGES = listOf(
        Language("en",    android.R.string.cancel),
        Language("zh",    android.R.string.cancel),
        Language("zh-TW", android.R.string.cancel),
        Language("hi",    android.R.string.cancel),
        Language("ar",    android.R.string.cancel),
        Language("in",    android.R.string.cancel),
        Language("pt",    android.R.string.cancel),
    )

    fun wrap(context: Context): Context {
        val language = getSavedLanguage(context)
        return applyLocale(context, language)
    }

    fun getSavedLanguage(context: Context): String {
        return context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .getString(PREF_LANGUAGE, "en") ?: "en"
    }

    fun saveAndApply(context: Context, language: String): Context {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit().putString(PREF_LANGUAGE, language).apply()
        return applyLocale(context, language)
    }

    private fun applyLocale(context: Context, language: String): Context {
        val locale = when (language) {
            "zh"    -> Locale.SIMPLIFIED_CHINESE
            "zh-TW" -> Locale("zh", "TW")
            "pt"    -> Locale("pt", "BR")
            else    -> Locale(language)
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
