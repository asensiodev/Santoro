package com.asensiodev.settings.impl.presentation.profile

internal sealed interface ProfileEffect {
    data object NavigateToSettings : ProfileEffect
}
