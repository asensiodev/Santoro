package com.asensiodev.library.observability.impl

import android.os.Bundle
import com.asensiodev.library.observability.api.ObservabilityTracker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseObservabilityTracker
    @Inject
    constructor(
        private val analytics: FirebaseAnalytics,
        private val crashlytics: FirebaseCrashlytics,
    ) : ObservabilityTracker {
        override fun setUser(
            userId: String,
            isAnonymous: Boolean,
        ) {
            val userType = if (isAnonymous) USER_TYPE_ANONYMOUS else USER_TYPE_REGISTERED
            analytics.setUserId(userId)
            analytics.setUserProperty(USER_TYPE, userType)
            crashlytics.setUserId(userId)
            crashlytics.setCustomKey(USER_TYPE, userType)
        }

        override fun clearUser() {
            analytics.setUserId(null)
            analytics.setUserProperty(USER_TYPE, null)
            crashlytics.setUserId(EMPTY_VALUE)
            crashlytics.setCustomKey(USER_TYPE, USER_TYPE_UNAUTHENTICATED)
        }

        override fun trackScreen(screenName: String) {
            val parameters =
                Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                    putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
                }
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, parameters)
        }

        override fun trackAction(
            actionName: String,
            parameters: Map<String, String>,
        ) {
            analytics.logEvent(actionName.toFirebaseName(), parameters.toBundle())
        }

        override fun recordError(
            errorName: String,
            throwable: Throwable,
            parameters: Map<String, String>,
        ) {
            analytics.logEvent(
                ERROR_EVENT,
                (parameters + (ERROR_NAME to errorName)).toBundle(),
            )
            crashlytics.setCustomKey(ERROR_NAME, errorName)
            parameters.forEach { (key, value) ->
                crashlytics.setCustomKey(
                    key.toFirebaseName(),
                    value,
                )
            }
            crashlytics.recordException(throwable)
        }

        private fun Map<String, String>.toBundle(): Bundle =
            Bundle().apply {
                forEach { (key, value) ->
                    putString(
                        key.toFirebaseName(),
                        value.take(MAX_PARAM_VALUE_LENGTH),
                    )
                }
            }

        private fun String.toFirebaseName(): String =
            replace(Regex("[^A-Za-z0-9_]+"), UNDERSCORE)
                .trim('_')
                .take(MAX_PARAM_NAME_LENGTH)
                .ifBlank { FALLBACK_NAME }

        private companion object {
            const val USER_TYPE = "user_type"
            const val USER_TYPE_ANONYMOUS = "anonymous"
            const val USER_TYPE_REGISTERED = "registered"
            const val USER_TYPE_UNAUTHENTICATED = "unauthenticated"
            const val EMPTY_VALUE = ""
            const val ERROR_EVENT = "app_error"
            const val ERROR_NAME = "error_name"
            const val UNDERSCORE = "_"
            const val FALLBACK_NAME = "unknown"
            const val MAX_PARAM_NAME_LENGTH = 40
            const val MAX_PARAM_VALUE_LENGTH = 100
        }
    }
