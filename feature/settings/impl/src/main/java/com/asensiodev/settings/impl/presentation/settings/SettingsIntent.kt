package com.asensiodev.settings.impl.presentation.settings

import com.asensiodev.core.domain.model.AppLanguage
import com.asensiodev.core.domain.model.ThemeOption

internal sealed interface SettingsIntent {
    data object ObserveAuth : SettingsIntent
    data object ObserveTheme : SettingsIntent
    data object OnAppearanceClicked : SettingsIntent
    data class SetTheme(
        val option: ThemeOption,
    ) : SettingsIntent
    data object DismissThemePicker : SettingsIntent
    data object OnLanguageClicked : SettingsIntent
    data class SetLanguage(
        val language: AppLanguage,
    ) : SettingsIntent
    data object DismissLanguagePicker : SettingsIntent
    data object OnLogoutClicked : SettingsIntent
}
