package com.verbum.app.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.verbum.core.common.model.LiturgicalSeason
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dynamically switches the launcher icon to match the current liturgical season.
 *
 * Uses activity-alias toggling: exactly one alias is enabled at any time so the
 * launcher always shows the season-appropriate background colour behind the
 * shared cross-and-book foreground.
 */
@Singleton
class SeasonalIconManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val ordinaryAlias = ".MainActivityOrdinary"

    private val aliases = mapOf(
        LiturgicalSeason.ORDINARY_TIME to ".MainActivityOrdinaryLauncher",
        LiturgicalSeason.ADVENT to ".MainActivityAdvent",
        LiturgicalSeason.CHRISTMAS to ".MainActivityChristmas",
        LiturgicalSeason.LENT to ".MainActivityLent",
        LiturgicalSeason.EASTER to ".MainActivityEaster",
        LiturgicalSeason.PENTECOST to ".MainActivityPentecost",
    )

    fun updateIcon(season: LiturgicalSeason) {
        val pm = context.packageManager
        val pkg = context.packageName

        // Keep the ordinary launcher alias enabled so IDE/device launch targets stay valid.
        val ordinaryComponent = ComponentName(pkg, "$pkg$ordinaryAlias")
        if (pm.getComponentEnabledSetting(ordinaryComponent) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            pm.setComponentEnabledSetting(
                ordinaryComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
        }

        aliases.forEach { (aliasSeason, suffix) ->
            val component = ComponentName(pkg, "$pkg$suffix")
            val desired = if (aliasSeason == season) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }

            if (pm.getComponentEnabledSetting(component) != desired) {
                pm.setComponentEnabledSetting(
                    component,
                    desired,
                    PackageManager.DONT_KILL_APP,
                )
            }
        }
    }
}
