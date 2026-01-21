package com.asensiodev.settings.impl.presentation

import com.asensiodev.ui.UiText

internal data class SettingsUiState(
    val isLoading: Boolean = false,
    val isAnonymous: Boolean = false,
    val error: UiText? = null,
    val isLinkAccountSuccessful: Boolean = false,
    val showAccountCollisionDialog: Boolean = false,
)
