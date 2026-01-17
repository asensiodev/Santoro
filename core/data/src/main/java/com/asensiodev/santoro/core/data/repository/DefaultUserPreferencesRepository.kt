package com.asensiodev.santoro.core.data.repository

import com.asensiodev.core.domain.repository.UserPreferencesRepository
import com.asensiodev.library.securestorage.api.SecureKeyValueStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class DefaultUserPreferencesRepository
    @Inject
    constructor(
        @Named("user_preferences") private val secureKeyValueStore: SecureKeyValueStore,
    ) : UserPreferencesRepository {
        private val _hasSeenGuestOnboarding = MutableStateFlow(getInitialValue())
        override val hasSeenGuestOnboarding: Flow<Boolean> = _hasSeenGuestOnboarding.asStateFlow()

        override suspend fun setHasSeenGuestOnboarding(hasSeen: Boolean) {
            secureKeyValueStore.writeString(KEY_HAS_SEEN_ONBOARDING, hasSeen.toString())
            _hasSeenGuestOnboarding.value = hasSeen
        }

        private fun getInitialValue(): Boolean {
            val value = secureKeyValueStore.readString(KEY_HAS_SEEN_ONBOARDING)
            return value?.toBoolean() ?: false
        }

        companion object {
            private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_guest_onboarding"
        }
    }
