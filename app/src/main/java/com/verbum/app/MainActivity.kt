package com.verbum.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.verbum.app.bootstrap.DataBootstrapCoordinator
import com.verbum.app.icon.SeasonalIconManager
import com.verbum.app.navigation.VerbumApp
import com.verbum.core.common.model.LiturgicalSeason
import com.verbum.core.ui.theme.VerbumTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var seasonalIconManager: SeasonalIconManager
    @Inject lateinit var dataBootstrapCoordinator: DataBootstrapCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !dataBootstrapCoordinator.criticalDataReady.value }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val season = LiturgicalSeasonProvider.currentSeason.value
        seasonalIconManager.updateIcon(season)

        setContent {
            val currentSeason by LiturgicalSeasonProvider.currentSeason
                .collectAsStateWithLifecycle()

            VerbumTheme(liturgicalSeason = currentSeason) {
                VerbumApp()
            }
        }
    }
}

/**
 * Simple provider that calculates the current Liturgical Season.
 * In production this would be injected and use a proper calendar engine.
 */
object LiturgicalSeasonProvider {
    val currentSeason = MutableStateFlow(calculateSeason())

    private fun calculateSeason(): LiturgicalSeason {
        val today = java.time.LocalDate.now()
        val month = today.monthValue
        val day = today.dayOfMonth

        return when {
            // Advent: ~4 Sundays before Christmas (approx Nov 27 – Dec 24)
            (month == 11 && day >= 27) || (month == 12 && day <= 24) -> LiturgicalSeason.ADVENT
            // Christmas: Dec 25 – Jan 6 (Epiphany)
            (month == 12 && day >= 25) || (month == 1 && day <= 6) -> LiturgicalSeason.CHRISTMAS
            // Lent: ~46 days before Easter (approx Feb/Mar/Apr — simplified)
            month == 3 || (month == 4 && day <= 12) -> LiturgicalSeason.LENT
            // Pentecost week: ~7th Sunday after Easter (late May / early June)
            (month == 5 && day >= 25) || (month == 6 && day <= 7) -> LiturgicalSeason.PENTECOST
            // Easter: ~50 days (approx Easter Sunday through Pentecost)
            (month == 4 && day >= 13) || (month == 5 && day < 25) -> LiturgicalSeason.EASTER
            // Everything else
            else -> LiturgicalSeason.ORDINARY_TIME
        }
    }
}
