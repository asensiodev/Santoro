package com.asensiodev.settings.impl.presentation.settings

import com.asensiodev.core.domain.model.ThemeOption
import com.asensiodev.ui.UiText

internal data class SettingsUiState(
    val isLoading: Boolean = false,
    val isAnonymous: Boolean = false,
    val error: UiText? = null,
    val isLinkAccountSuccessful: Boolean = false,
    val showAccountCollisionDialog: Boolean = false,
    val currentTheme: ThemeOption = ThemeOption.SYSTEM,
    val showThemePicker: Boolean = false,
)
