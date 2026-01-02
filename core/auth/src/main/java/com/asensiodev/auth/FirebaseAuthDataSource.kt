package com.asensiodev.auth

import com.asensiodev.auth.data.mapper.toSantoroUser
import com.asensiodev.core.domain.model.SantoroUser
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

internal class FirebaseAuthDataSource
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth,
    ) : AuthDataSource {
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

        override suspend fun signInAnonymously(): Result<SantoroUser> =
            try {
                val authResult = firebaseAuth.signInAnonymously().await()
                val user = authResult.user!!.toSantoroUser()
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }

        override suspend fun signInWithCredential(credential: AuthCredential): Result<SantoroUser> =
            try {
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val user = authResult.user!!.toSantoroUser()
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }

        override suspend fun signOut() {
            firebaseAuth.signOut()
        }
    }
