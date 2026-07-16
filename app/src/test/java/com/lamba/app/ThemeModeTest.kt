package com.lamba.app

import androidx.appcompat.app.AppCompatDelegate
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `enabled theme uses night mode`() {
        assertEquals(
            AppCompatDelegate.MODE_NIGHT_YES,
            ThemeMode.fromEnabled(true).nightMode,
        )
    }

    @Test
    fun `disabled theme uses light mode`() {
        assertEquals(
            AppCompatDelegate.MODE_NIGHT_NO,
            ThemeMode.fromEnabled(false).nightMode,
        )
    }
}
