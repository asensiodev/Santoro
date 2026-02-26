package com.asensiodev.settings.impl.presentation.settings

internal sealed interface SettingsEffect {
    data object NavigateBack : SettingsEffect
}
