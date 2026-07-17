package com.asensiodev.auth.helper

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.asensiodev.santoro.core.stringresources.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSignInHelper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val credentialManager = CredentialManager.create(context)

        private val serverClientId: String
            get() = context.getString(R.string.default_web_client_id)

        suspend fun signIn(activityContext: Context): Result<String> {
            val result = signInWithGoogleIdOption(activityContext)
            if (result.exceptionOrNull() is NoCredentialException) {
                Log.w(TAG, "No credentials found, falling back to SignInWithGoogleOption")
                return signInWithGoogleOption(activityContext)
            }
            return result
        }

        private suspend fun signInWithGoogleIdOption(activityContext: Context): Result<String> {
            val googleIdOption =
                GetGoogleIdOption
                    .Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()

            val request =
                GetCredentialRequest
                    .Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

            return getCredential(activityContext, request)
        }

        private suspend fun signInWithGoogleOption(activityContext: Context): Result<String> {
            val signInOption =
                GetSignInWithGoogleOption
                    .Builder(serverClientId)
                    .build()

            val request =
                GetCredentialRequest
                    .Builder()
                    .addCredentialOption(signInOption)
                    .build()

            return getCredential(activityContext, request)
        }

        private suspend fun getCredential(
            activityContext: Context,
            request: GetCredentialRequest,
        ): Result<String> =
            try {
                val result = credentialManager.getCredential(activityContext, request)
                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(
                        result.credential.data,
                    )
                Result.success(googleIdTokenCredential.idToken)
            } catch (exception: CancellationException) {
                throw exception
            } catch (e: GetCredentialException) {
                Log.e(TAG, "GetCredentialException: ${e.type} - ${e.message}", e)
                Result.failure(e)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error: ${e.message}", e)
                Result.failure(e)
            }

        private companion object {
            const val TAG = "GoogleSignInHelper"
        }
    }
