package com.asensiodev.auth

import com.asensiodev.auth.data.mapper.toSantoroUser
import com.asensiodev.auth.di.AuthApplicationScope
import com.asensiodev.auth.domain.exception.AccountCollisionException
import com.asensiodev.auth.domain.exception.AuthenticatedUserMismatchException
import com.asensiodev.auth.domain.exception.NoAuthenticatedUserException
import com.asensiodev.auth.domain.model.ExpectedUserSignOutOutcome
import com.asensiodev.core.domain.model.SantoroUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

internal class FirebaseAuthDataSource
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth,
        @AuthApplicationScope applicationScope: CoroutineScope,
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
            }.shareIn(
                scope = applicationScope,
                started = SharingStarted.Eagerly,
                replay = 1,
            )

        override suspend fun signInAnonymously(): Result<SantoroUser> =
            try {
                val authResult = firebaseAuth.signInAnonymously().await()
                val user = authResult.user!!.toSantoroUser()
                Result.success(user)
            } catch (exception: CancellationException) {
                throw exception
            } catch (e: Exception) {
                Result.failure(e)
            }

        override suspend fun signInWithGoogle(idToken: String): Result<SantoroUser> =
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val user = authResult.user!!.toSantoroUser()
                Result.success(user)
            } catch (exception: CancellationException) {
                throw exception
            } catch (e: Exception) {
                Result.failure(e)
            }

        override suspend fun linkWithGoogle(
            expectedUid: String,
            idToken: String,
        ): Result<SantoroUser> =
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val currentUser = firebaseAuth.currentUser ?: throw NoAuthenticatedUserException()
                if (currentUser.uid != expectedUid) {
                    throw AuthenticatedUserMismatchException()
                }
                val authResult = currentUser.linkWithCredential(credential).await()
                val user = authResult.user?.toSantoroUser() ?: throw NoAuthenticatedUserException()
                val activeUser = firebaseAuth.currentUser ?: throw NoAuthenticatedUserException()
                if (user.uid != expectedUid || activeUser.uid != expectedUid) {
                    throw AuthenticatedUserMismatchException()
                }
                Result.success(user)
            } catch (exception: CancellationException) {
                throw exception
            } catch (fce: FirebaseAuthUserCollisionException) {
                Result.failure(
                    AccountCollisionException(
                        "This account is already linked to another user.",
                        fce,
                    ),
                )
            } catch (e: Exception) {
                Result.failure(e)
            }

        override suspend fun reauthenticateWithGoogle(
            expectedUid: String,
            idToken: String,
        ): Result<Unit> =
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val currentUser = firebaseAuth.currentUser ?: throw NoAuthenticatedUserException()
                if (currentUser.uid != expectedUid) {
                    throw AuthenticatedUserMismatchException()
                }
                currentUser.reauthenticate(credential).await()
                Result.success(Unit)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Result.failure(exception)
            }

        override suspend fun signOut(expectedUid: String): ExpectedUserSignOutOutcome =
            when (firebaseAuth.currentUser?.uid) {
                null -> ExpectedUserSignOutOutcome.NoAuthenticatedUser
                expectedUid -> {
                    firebaseAuth.signOut()
                    ExpectedUserSignOutOutcome.SignedOut
                }
                else -> ExpectedUserSignOutOutcome.AuthenticatedUserMismatch
            }

        override suspend fun deleteAccount(expectedUid: String): Result<Unit> =
            try {
                val user = firebaseAuth.currentUser ?: throw NoAuthenticatedUserException()
                if (user.uid != expectedUid) throw AuthenticatedUserMismatchException()
                user.delete().await()
                Result.success(Unit)
            } catch (exception: CancellationException) {
                throw exception
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
