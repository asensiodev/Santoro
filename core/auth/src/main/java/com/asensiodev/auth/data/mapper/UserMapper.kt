package com.asensiodev.auth.data.mapper

import com.asensiodev.core.domain.model.SantoroUser
import com.google.firebase.auth.FirebaseUser

internal fun FirebaseUser.toSantoroUser(): SantoroUser =
    SantoroUser(
        uid = this.uid,
        email = this.email,
        displayName = this.displayName,
        isAnonymous = this.isAnonymous,
    )
