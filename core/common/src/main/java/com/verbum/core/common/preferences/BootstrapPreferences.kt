package com.verbum.core.common.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import com.verbum.core.common.constants.VerbumConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.bootstrapDataStore: DataStore<Preferences> by preferencesDataStore(
    name = VerbumConstants.DATASTORE_NAME,
)

@Singleton
class BootstrapPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private companion object {
        val BIBLE_PRELOADED = booleanPreferencesKey("bible_preloaded")
        val PRAYERS_PRELOADED = booleanPreferencesKey("prayers_preloaded")
        val PREFERRED_BIBLE_LANGUAGE = stringPreferencesKey("preferred_bible_language")
    }

    suspend fun isBiblePreloaded(): Boolean {
        return context.bootstrapDataStore.data.map { prefs ->
            prefs[BIBLE_PRELOADED] ?: false
        }.first()
    }

    suspend fun markBiblePreloaded() {
        context.bootstrapDataStore.edit { prefs ->
            prefs[BIBLE_PRELOADED] = true
        }
    }

    suspend fun getPreferredBibleLanguage(): String? {
        return context.bootstrapDataStore.data.map { prefs ->
            prefs[PREFERRED_BIBLE_LANGUAGE]
        }.first()
    }

    suspend fun setPreferredBibleLanguage(languageCode: String) {
        context.bootstrapDataStore.edit { prefs ->
            prefs[PREFERRED_BIBLE_LANGUAGE] = languageCode.lowercase()
        }
    }

    suspend fun isPrayersPreloaded(): Boolean {
        return context.bootstrapDataStore.data.map { prefs ->
            prefs[PRAYERS_PRELOADED] ?: false
        }.first()
    }

    suspend fun markPrayersPreloaded() {
        context.bootstrapDataStore.edit { prefs ->
            prefs[PRAYERS_PRELOADED] = true
        }
    }
}
