package com.asensiodev.auth.data.repository

import com.asensiodev.auth.data.mapper.toSantoroUser
import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.model.SantoroUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class FirebaseAuthRepository
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth,
    ) : AuthRepository {
        override val currentUser: Flow<SantoroUser?> =
            callbackFlow {
                val authStateListener =
                    FirebaseAuth.AuthStateListener { auth ->
                        trySend(auth.currentUser)
                    }
                firebaseAuth.addAuthStateListener(authStateListener)

                awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
            }.map { firebaseUser ->
                firebaseUser?.toSantoroUser()
            }

        override suspend fun signOut() {
            firebaseAuth.signOut()
        }
    }
