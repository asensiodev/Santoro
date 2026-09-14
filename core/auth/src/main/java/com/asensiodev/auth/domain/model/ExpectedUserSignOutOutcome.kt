package com.asensiodev.auth.domain.model

sealed interface ExpectedUserSignOutOutcome {
    data object SignedOut : ExpectedUserSignOutOutcome

    data object NoAuthenticatedUser : ExpectedUserSignOutOutcome

    data object AuthenticatedUserMismatch : ExpectedUserSignOutOutcome
}
