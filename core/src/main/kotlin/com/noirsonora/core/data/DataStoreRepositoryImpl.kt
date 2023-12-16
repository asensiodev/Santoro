package com.noirsonora.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.noirsonora.core.domain.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

class DataStoreRepositoryImpl(val context: Context) : DataStoreRepository {

    override suspend fun saveOnboardingState(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_STATE] = completed
        }
    }

    override fun readOnboardingState(): Flow<Boolean> {
        return context.dataStore.data
            .map { preferences ->
                preferences[ONBOARDING_STATE] ?: false
            }
    }

    companion object {
        val ONBOARDING_STATE = booleanPreferencesKey("onboarding_state")
    }
}