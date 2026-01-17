package com.asensiodev.auth.helper

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.asensiodev.santoro.core.stringresources.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSignInHelper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val credentialManager = CredentialManager.create(context)

        suspend fun signIn(activityContext: Context): Result<String> {
            val googleIdOption =
                GetGoogleIdOption
                    .Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(true)
                    .build()

            val request =
                GetCredentialRequest
                    .Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

            return try {
                val result = credentialManager.getCredential(activityContext, request)
                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(
                        result.credential.data,
                    )
                Result.success(googleIdTokenCredential.idToken)
            } catch (e: GetCredentialException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
