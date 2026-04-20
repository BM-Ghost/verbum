package com.verbum.feature.missal.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class MissalAssetDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun loadReadings(date: LocalDate): LocalMissalData? {
        val year = date.year
        val monthDay = "%02d-%02d".format(date.monthValue, date.dayOfMonth)
        val readingPath = "missal/readings/$year/$monthDay.json"
        val calendarPath = "missal/liturgical-calendar/$year/$monthDay.json"

        val readingsJson = readJsonObject(readingPath) ?: return null
        val calendarJson = readJsonObject(calendarPath)

        val readingsNode = readingsJson.optJSONObject("readings") ?: return null
        val celebration = calendarJson?.optJSONObject("celebration")

        val readingItems = buildList {
            add(
                LocalReading(
                    type = "first_reading",
                    title = "First Reading",
                    reference = readingsNode.optString("firstReading"),
                )
            )
            add(
                LocalReading(
                    type = "psalm",
                    title = "Responsorial Psalm",
                    reference = readingsNode.optString("psalm"),
                )
            )
            readingsNode.optString("secondReading").takeIf { it.isNotBlank() }?.let {
                add(
                    LocalReading(
                        type = "second_reading",
                        title = "Second Reading",
                        reference = it,
                    )
                )
            }
            add(
                LocalReading(
                    type = "gospel",
                    title = "Gospel",
                    reference = readingsNode.optString("gospel"),
                )
            )
        }.filter { it.reference.isNotBlank() }

        return LocalMissalData(
            dateIso = readingsJson.optString("date", date.toString()),
            season = readingsJson.optString("season").ifBlank { calendarJson?.optString("season").orEmpty() },
            celebrationName = celebration?.optString("name")?.takeIf { it.isNotBlank() },
            celebrationType = celebration?.optString("type")?.takeIf { it.isNotBlank() },
            readings = readingItems,
        )
    }

    fun loadFixedFeasts(): List<FixedFeast> {
        val raw = context.assets.open("missal/fixed-feasts.json").bufferedReader().use { it.readText() }
        val array = JSONArray(raw)
        return buildList(array.length()) {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    FixedFeast(
                        date = obj.getString("date"),
                        name = obj.getString("name"),
                        type = obj.getString("type"),
                        rank = obj.optInt("rank", 3),
                    )
                )
            }
        }
    }

    private fun readJsonObject(path: String): JSONObject? {
        return runCatching {
            context.assets.open(path).bufferedReader().use { JSONObject(it.readText()) }
        }.getOrNull()
    }
}

data class LocalMissalData(
    val dateIso: String,
    val season: String,
    val celebrationName: String?,
    val celebrationType: String?,
    val readings: List<LocalReading>,
)

data class LocalReading(
    val type: String,
    val title: String,
    val reference: String,
)

data class FixedFeast(
    val date: String,
    val name: String,
    val type: String,
    val rank: Int,
)
