package com.asensiodev.settings.impl.presentation.settings

import com.asensiodev.core.domain.model.AppLanguage
import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.ui.UiText

internal data class SettingsPendingMessage(
    val id: Long,
    val message: UiText,
)

internal data class SettingsUiState(
    val isLoading: Boolean = false,
    val showAccountActions: Boolean = false,
    val currentTheme: ThemeOption = ThemeOption.SYSTEM,
    val showThemePicker: Boolean = false,
    val showLanguagePicker: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val pendingMessage: SettingsPendingMessage? = null,
)
