package com.lamba.app

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

enum class ThemeMode(
    val isEnabled: Boolean,
    val nightMode: Int,
) {
    LIGHT(false, AppCompatDelegate.MODE_NIGHT_NO),
    DARK(true, AppCompatDelegate.MODE_NIGHT_YES),
    ;

    companion object {
        fun fromEnabled(enabled: Boolean): ThemeMode = if (enabled) DARK else LIGHT
    }
}

object ThemeManager {
    private const val PREFERENCES_NAME = "lamba_theme"
    private const val KEY_DARK_THEME_ENABLED = "dark_theme_enabled"

    fun current(context: Context): ThemeMode = ThemeMode.fromEnabled(
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK_THEME_ENABLED, false),
    )

    fun applySavedTheme(context: Context) {
        AppCompatDelegate.setDefaultNightMode(current(context).nightMode)
    }

    fun save(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_THEME_ENABLED, enabled)
            .apply()
        AppCompatDelegate.setDefaultNightMode(ThemeMode.fromEnabled(enabled).nightMode)
    }
}
