package com.asensiodev.santoro.core.data.repository

import com.asensiodev.core.domain.model.ThemeOption
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
        private val _hasSeenGuestOnboarding = MutableStateFlow(getInitialOnboardingValue())
        override val hasSeenGuestOnboarding: Flow<Boolean> = _hasSeenGuestOnboarding.asStateFlow()

        private val _hasSeenDetailTooltip = MutableStateFlow(getInitialDetailTooltipValue())
        override val hasSeenDetailTooltip: Flow<Boolean> = _hasSeenDetailTooltip.asStateFlow()

        private val _theme = MutableStateFlow(getInitialThemeValue())
        override val theme: Flow<ThemeOption> = _theme.asStateFlow()

        override suspend fun setHasSeenGuestOnboarding(hasSeen: Boolean) {
            secureKeyValueStore.writeString(KEY_HAS_SEEN_ONBOARDING, hasSeen.toString())
            _hasSeenGuestOnboarding.value = hasSeen
        }

        override suspend fun setHasSeenDetailTooltip(hasSeen: Boolean) {
            secureKeyValueStore.writeString(KEY_HAS_SEEN_DETAIL_TOOLTIP, hasSeen.toString())
            _hasSeenDetailTooltip.value = hasSeen
        }

        override suspend fun setTheme(option: ThemeOption) {
            secureKeyValueStore.writeString(KEY_THEME_OPTION, option.name)
            _theme.value = option
        }

        private fun getInitialOnboardingValue(): Boolean {
            val value = secureKeyValueStore.readString(KEY_HAS_SEEN_ONBOARDING)
            return value?.toBoolean() ?: false
        }

        private fun getInitialDetailTooltipValue(): Boolean {
            val value = secureKeyValueStore.readString(KEY_HAS_SEEN_DETAIL_TOOLTIP)
            return value?.toBoolean() ?: false
        }

        private fun getInitialThemeValue(): ThemeOption {
            val value = secureKeyValueStore.readString(KEY_THEME_OPTION)
            return value?.let { runCatching { ThemeOption.valueOf(it) }.getOrNull() }
                ?: ThemeOption.SYSTEM
        }

        companion object {
            private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_guest_onboarding"
            private const val KEY_HAS_SEEN_DETAIL_TOOLTIP = "has_seen_detail_tooltip"
            private const val KEY_THEME_OPTION = "theme_option"
        }
    }
