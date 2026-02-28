package com.asensiodev.feature.searchmovies.impl.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.asensiodev.feature.searchmovies.impl.domain.repository.RecentSearchesRepository
import com.asensiodev.feature.searchmovies.impl.domain.usecase.SaveRecentSearchUseCase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val KEY_RECENT_SEARCHES = stringPreferencesKey("recent_searches")
private val listType = object : TypeToken<List<String>>() {}.type

class DataStoreRecentSearchesDataSource
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
        private val gson: Gson,
    ) : RecentSearchesRepository {
        override fun getRecentSearches(): Flow<List<String>> =
            dataStore.data.map { preferences ->
                val json = preferences[KEY_RECENT_SEARCHES] ?: return@map emptyList()
                gson.fromJson(json, listType) ?: emptyList()
            }

        override suspend fun saveSearch(query: String) {
            dataStore.edit { preferences ->
                val current = deserialize(preferences[KEY_RECENT_SEARCHES])
                val updated =
                    (listOf(query) + current.filter { it != query })
                        .take(SaveRecentSearchUseCase.MAX_ENTRIES)
                preferences[KEY_RECENT_SEARCHES] = gson.toJson(updated)
            }
        }

        override suspend fun clearAll() {
            dataStore.edit { preferences ->
                preferences.remove(KEY_RECENT_SEARCHES)
            }
        }

        private fun deserialize(json: String?): List<String> {
            if (json.isNullOrBlank()) return emptyList()
            return runCatching { gson.fromJson<List<String>>(json, listType) ?: emptyList() }
                .getOrDefault(emptyList())
        }
    }
