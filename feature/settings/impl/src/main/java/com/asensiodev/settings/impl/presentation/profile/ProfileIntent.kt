package com.asensiodev.settings.impl.presentation.profile

import android.content.Context

internal sealed interface ProfileIntent {
    data object ObserveAuth : ProfileIntent
    data class OnLinkGoogleClicked(
        val context: Context,
    ) : ProfileIntent
    data object DismissLinkSuccess : ProfileIntent
    data object DismissAccountCollision : ProfileIntent
    data object ConfirmAccountCollision : ProfileIntent
}
