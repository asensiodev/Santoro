package com.asensiodev.auth.domain.exception

class AccountCollisionException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)
