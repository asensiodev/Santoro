package com.asensiodev.settings.impl.presentation.settings

import com.asensiodev.ui.UiText

internal sealed interface SettingsEffect {
    data class ShowError(
        val message: UiText,
    ) : SettingsEffect
}
