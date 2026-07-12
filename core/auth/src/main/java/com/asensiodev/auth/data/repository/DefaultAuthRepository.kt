package com.asensiodev.auth.data.repository

import com.asensiodev.auth.AuthDataSource
import com.asensiodev.auth.domain.repository.AuthRepository
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.library.observability.api.NoOpObservabilityTracker
import com.asensiodev.library.observability.api.ObservabilityTracker
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class DefaultAuthRepository
    @Inject
    constructor(
        private val dataSource: AuthDataSource,
        private val observabilityTracker: ObservabilityTracker = NoOpObservabilityTracker,
    ) : AuthRepository {
        override val currentUser: Flow<SantoroUser?> = dataSource.currentUser

        override suspend fun signInAnonymously(): Result<SantoroUser> =
            dataSource
                .signInAnonymously()
                .onSuccess { user ->
                    observabilityTracker.trackAction(
                        AUTH_SIGN_IN,
                        mapOf(AUTH_PROVIDER to AUTH_PROVIDER_ANONYMOUS),
                    )
                    observabilityTracker.setUser(user.uid, user.isAnonymous)
                }.onFailure { exception ->
                    observabilityTracker.recordError(
                        AUTH_SIGN_IN_FAILED,
                        exception,
                        mapOf(AUTH_PROVIDER to AUTH_PROVIDER_ANONYMOUS),
                    )
                }

        override suspend fun signInWithGoogle(idToken: String): Result<SantoroUser> =
            dataSource
                .signInWithGoogle(idToken)
                .onSuccess { user ->
                    observabilityTracker.trackAction(
                        AUTH_SIGN_IN,
                        mapOf(AUTH_PROVIDER to AUTH_PROVIDER_GOOGLE),
                    )
                    observabilityTracker.setUser(user.uid, user.isAnonymous)
                }.onFailure { exception ->
                    observabilityTracker.recordError(
                        AUTH_SIGN_IN_FAILED,
                        exception,
                        mapOf(AUTH_PROVIDER to AUTH_PROVIDER_GOOGLE),
                    )
                }

        override suspend fun linkWithGoogle(idToken: String): Result<SantoroUser> =
            dataSource
                .linkWithGoogle(idToken)
                .onSuccess { user ->
                    observabilityTracker.trackAction(AUTH_LINK_GOOGLE)
                    observabilityTracker.setUser(user.uid, user.isAnonymous)
                }.onFailure { exception ->
                    observabilityTracker.recordError(AUTH_LINK_GOOGLE_FAILED, exception)
                }

        override suspend fun signOut() {
            dataSource.signOut()
            observabilityTracker.trackAction(AUTH_SIGN_OUT)
            observabilityTracker.clearUser()
        }

        override suspend fun deleteAccount(): Result<Unit> =
            dataSource
                .deleteAccount()
                .onSuccess {
                    observabilityTracker.trackAction(AUTH_DELETE_ACCOUNT)
                    observabilityTracker.clearUser()
                }.onFailure { exception ->
                    observabilityTracker.recordError(AUTH_DELETE_ACCOUNT_FAILED, exception)
                }

        private companion object {
            const val AUTH_PROVIDER = "auth_provider"
            const val AUTH_PROVIDER_ANONYMOUS = "anonymous"
            const val AUTH_PROVIDER_GOOGLE = "google"
            const val AUTH_SIGN_IN = "auth_sign_in"
            const val AUTH_SIGN_IN_FAILED = "auth_sign_in_failed"
            const val AUTH_LINK_GOOGLE = "auth_link_google"
            const val AUTH_LINK_GOOGLE_FAILED = "auth_link_google_failed"
            const val AUTH_SIGN_OUT = "auth_sign_out"
            const val AUTH_DELETE_ACCOUNT = "auth_delete_account"
            const val AUTH_DELETE_ACCOUNT_FAILED = "auth_delete_account_failed"
        }
    }
