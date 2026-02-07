package com.asensiodev.settings.impl.presentation.profile

import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.ui.UiText

internal data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: SantoroUser? = null,
    val isAnonymous: Boolean = true,
    val error: UiText? = null,
    val isLinkAccountSuccessful: Boolean = false,
    val showAccountCollisionDialog: Boolean = false,
)
