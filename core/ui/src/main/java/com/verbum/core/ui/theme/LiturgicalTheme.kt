package com.verbum.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.verbum.core.common.model.LiturgicalSeason

/**
 * Liturgical Theme Engine — the spiritual heart of Verbum's visual system.
 *
 * Dynamically generates Material 3 color schemes based on the
 * current liturgical season of the Catholic Church calendar.
 */
object LiturgicalThemeEngine {

    fun colorScheme(season: LiturgicalSeason, darkTheme: Boolean): ColorScheme {
        return if (darkTheme) darkScheme(season) else lightScheme(season)
    }

    private fun lightScheme(season: LiturgicalSeason): ColorScheme = when (season) {
        LiturgicalSeason.ADVENT -> lightColorScheme(
            primary = AdventColors.primary,
            onPrimary = AdventColors.onPrimary,
            primaryContainer = AdventColors.primaryContainer,
            onPrimaryContainer = AdventColors.onPrimaryContainer,
            secondary = AdventColors.secondary,
            onSecondary = AdventColors.onSecondary,
            secondaryContainer = AdventColors.secondaryContainer,
            onSecondaryContainer = AdventColors.onSecondaryContainer,
            tertiary = AdventColors.tertiary,
            onTertiary = AdventColors.onTertiary,
            background = AdventColors.background,
            onBackground = AdventColors.onBackground,
            surface = AdventColors.surface,
            onSurface = AdventColors.onSurface,
            surfaceVariant = AdventColors.surfaceVariant,
            onSurfaceVariant = AdventColors.onSurfaceVariant,
            error = AdventColors.error,
            onError = AdventColors.onError,
        )

        LiturgicalSeason.CHRISTMAS -> lightColorScheme(
            primary = ChristmasColors.primary,
            onPrimary = ChristmasColors.onPrimary,
            primaryContainer = ChristmasColors.primaryContainer,
            onPrimaryContainer = ChristmasColors.onPrimaryContainer,
            secondary = ChristmasColors.secondary,
            onSecondary = ChristmasColors.onSecondary,
            secondaryContainer = ChristmasColors.secondaryContainer,
            onSecondaryContainer = ChristmasColors.onSecondaryContainer,
            tertiary = ChristmasColors.tertiary,
            onTertiary = ChristmasColors.onTertiary,
            background = ChristmasColors.background,
            onBackground = ChristmasColors.onBackground,
            surface = ChristmasColors.surface,
            onSurface = ChristmasColors.onSurface,
            surfaceVariant = ChristmasColors.surfaceVariant,
            onSurfaceVariant = ChristmasColors.onSurfaceVariant,
            error = ChristmasColors.error,
            onError = ChristmasColors.onError,
        )

        LiturgicalSeason.LENT -> lightColorScheme(
            primary = LentColors.primary,
            onPrimary = LentColors.onPrimary,
            primaryContainer = LentColors.primaryContainer,
            onPrimaryContainer = LentColors.onPrimaryContainer,
            secondary = LentColors.secondary,
            onSecondary = LentColors.onSecondary,
            secondaryContainer = LentColors.secondaryContainer,
            onSecondaryContainer = LentColors.onSecondaryContainer,
            tertiary = LentColors.tertiary,
            onTertiary = LentColors.onTertiary,
            background = LentColors.background,
            onBackground = LentColors.onBackground,
            surface = LentColors.surface,
            onSurface = LentColors.onSurface,
            surfaceVariant = LentColors.surfaceVariant,
            onSurfaceVariant = LentColors.onSurfaceVariant,
            error = LentColors.error,
            onError = LentColors.onError,
        )

        LiturgicalSeason.EASTER -> lightColorScheme(
            primary = EasterColors.primary,
            onPrimary = EasterColors.onPrimary,
            primaryContainer = EasterColors.primaryContainer,
            onPrimaryContainer = EasterColors.onPrimaryContainer,
            secondary = EasterColors.secondary,
            onSecondary = EasterColors.onSecondary,
            secondaryContainer = EasterColors.secondaryContainer,
            onSecondaryContainer = EasterColors.onSecondaryContainer,
            tertiary = EasterColors.tertiary,
            onTertiary = EasterColors.onTertiary,
            background = EasterColors.background,
            onBackground = EasterColors.onBackground,
            surface = EasterColors.surface,
            onSurface = EasterColors.onSurface,
            surfaceVariant = EasterColors.surfaceVariant,
            onSurfaceVariant = EasterColors.onSurfaceVariant,
            error = EasterColors.error,
            onError = EasterColors.onError,
        )

        LiturgicalSeason.PENTECOST -> lightColorScheme(
            primary = PentecostColors.primary,
            onPrimary = PentecostColors.onPrimary,
            primaryContainer = PentecostColors.primaryContainer,
            onPrimaryContainer = PentecostColors.onPrimaryContainer,
            secondary = PentecostColors.secondary,
            onSecondary = PentecostColors.onSecondary,
            secondaryContainer = PentecostColors.secondaryContainer,
            onSecondaryContainer = PentecostColors.onSecondaryContainer,
            tertiary = PentecostColors.tertiary,
            onTertiary = PentecostColors.onTertiary,
            background = PentecostColors.background,
            onBackground = PentecostColors.onBackground,
            surface = PentecostColors.surface,
            onSurface = PentecostColors.onSurface,
            surfaceVariant = PentecostColors.surfaceVariant,
            onSurfaceVariant = PentecostColors.onSurfaceVariant,
            error = PentecostColors.error,
            onError = PentecostColors.onError,
        )

        LiturgicalSeason.ORDINARY_TIME -> lightColorScheme(
            primary = OrdinaryTimeColors.primary,
            onPrimary = OrdinaryTimeColors.onPrimary,
            primaryContainer = OrdinaryTimeColors.primaryContainer,
            onPrimaryContainer = OrdinaryTimeColors.onPrimaryContainer,
            secondary = OrdinaryTimeColors.secondary,
            onSecondary = OrdinaryTimeColors.onSecondary,
            secondaryContainer = OrdinaryTimeColors.secondaryContainer,
            onSecondaryContainer = OrdinaryTimeColors.onSecondaryContainer,
            tertiary = OrdinaryTimeColors.tertiary,
            onTertiary = OrdinaryTimeColors.onTertiary,
            background = OrdinaryTimeColors.background,
            onBackground = OrdinaryTimeColors.onBackground,
            surface = OrdinaryTimeColors.surface,
            onSurface = OrdinaryTimeColors.onSurface,
            surfaceVariant = OrdinaryTimeColors.surfaceVariant,
            onSurfaceVariant = OrdinaryTimeColors.onSurfaceVariant,
            error = OrdinaryTimeColors.error,
            onError = OrdinaryTimeColors.onError,
        )
    }

    private fun darkScheme(season: LiturgicalSeason): ColorScheme = when (season) {
        LiturgicalSeason.ADVENT -> darkColorScheme(
            primary = AdventColors.primaryDark,
            onPrimary = AdventColors.onPrimaryDark,
            primaryContainer = AdventColors.primary,
            onPrimaryContainer = AdventColors.primaryContainer,
            background = AdventColors.backgroundDark,
            onBackground = AdventColors.onBackgroundDark,
            surface = AdventColors.surfaceDark,
            onSurface = AdventColors.onSurfaceDark,
        )

        LiturgicalSeason.CHRISTMAS -> darkColorScheme(
            primary = ChristmasColors.primaryDark,
            onPrimary = ChristmasColors.onPrimaryDark,
            primaryContainer = ChristmasColors.primary,
            onPrimaryContainer = ChristmasColors.primaryContainer,
            background = ChristmasColors.backgroundDark,
            onBackground = ChristmasColors.onBackgroundDark,
            surface = ChristmasColors.surfaceDark,
            onSurface = ChristmasColors.onSurfaceDark,
        )

        LiturgicalSeason.LENT -> darkColorScheme(
            primary = LentColors.primaryDark,
            onPrimary = LentColors.onPrimaryDark,
            primaryContainer = LentColors.primary,
            onPrimaryContainer = LentColors.primaryContainer,
            background = LentColors.backgroundDark,
            onBackground = LentColors.onBackgroundDark,
            surface = LentColors.surfaceDark,
            onSurface = LentColors.onSurfaceDark,
        )

        LiturgicalSeason.EASTER -> darkColorScheme(
            primary = EasterColors.primaryDark,
            onPrimary = EasterColors.onPrimaryDark,
            primaryContainer = EasterColors.primary,
            onPrimaryContainer = EasterColors.primaryContainer,
            background = EasterColors.backgroundDark,
            onBackground = EasterColors.onBackgroundDark,
            surface = EasterColors.surfaceDark,
            onSurface = EasterColors.onSurfaceDark,
        )

        LiturgicalSeason.PENTECOST -> darkColorScheme(
            primary = PentecostColors.primaryDark,
            onPrimary = PentecostColors.onPrimaryDark,
            primaryContainer = PentecostColors.primary,
            onPrimaryContainer = PentecostColors.primaryContainer,
            background = PentecostColors.backgroundDark,
            onBackground = PentecostColors.onBackgroundDark,
            surface = PentecostColors.surfaceDark,
            onSurface = PentecostColors.onSurfaceDark,
        )

        LiturgicalSeason.ORDINARY_TIME -> darkColorScheme(
            primary = OrdinaryTimeColors.primaryDark,
            onPrimary = OrdinaryTimeColors.onPrimaryDark,
            primaryContainer = OrdinaryTimeColors.primary,
            onPrimaryContainer = OrdinaryTimeColors.primaryContainer,
            background = OrdinaryTimeColors.backgroundDark,
            onBackground = OrdinaryTimeColors.onBackgroundDark,
            surface = OrdinaryTimeColors.surfaceDark,
            onSurface = OrdinaryTimeColors.onSurfaceDark,
        )
    }
}
