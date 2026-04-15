package com.verbum.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.verbum.core.common.model.LiturgicalSeason

/**
 * Composition local providing the current liturgical season throughout the app.
 */
val LocalLiturgicalSeason = compositionLocalOf { LiturgicalSeason.ORDINARY_TIME }

/**
 * Verbum root theme — wraps Material 3 with liturgical season awareness.
 *
 * The entire app color system dynamically shifts based on the Church calendar.
 * Advent brings hushed purples, Easter radiates gold and white,
 * and Ordinary Time pulses with life-giving green.
 */
@Composable
fun VerbumTheme(
    liturgicalSeason: LiturgicalSeason = LiturgicalSeason.ORDINARY_TIME,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = LiturgicalThemeEngine.colorScheme(liturgicalSeason, darkTheme)

    CompositionLocalProvider(
        LocalLiturgicalSeason provides liturgicalSeason,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = VerbumTypography,
            shapes = VerbumShapes,
            content = content,
        )
    }
}
