package com.noirsonora.onboarding_data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.noirsonora.onboarding_domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "onboarding_data_store")

class OnboardingDataStoreRepositoryImpl(context: Context) : OnboardingRepository {

    private val dataStore = context.dataStore

    override suspend fun saveOnboardingState(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    override fun getOnboardingState(): Flow<Boolean> {
        return dataStore.data
            .map { preferences ->
                preferences[ONBOARDING_COMPLETED] ?: false
            }
    }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey(name = "onboarding_completed")
    }

}