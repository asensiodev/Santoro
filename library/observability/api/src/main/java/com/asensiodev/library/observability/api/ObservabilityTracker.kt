package com.asensiodev.library.observability.api

interface ObservabilityTracker {
    fun setUser(isAnonymous: Boolean)

    fun clearUser()

    fun trackScreen(screenName: String)

    fun trackAction(
        actionName: String,
        parameters: Map<String, String> = emptyMap(),
    )

    fun recordError(
        errorName: String,
        throwable: Throwable,
        parameters: Map<String, String> = emptyMap(),
    )
}

object NoOpObservabilityTracker : ObservabilityTracker {
    override fun setUser(isAnonymous: Boolean) = Unit

    override fun clearUser() = Unit

    override fun trackScreen(screenName: String) = Unit

    override fun trackAction(
        actionName: String,
        parameters: Map<String, String>,
    ) = Unit

    override fun recordError(
        errorName: String,
        throwable: Throwable,
        parameters: Map<String, String>,
    ) = Unit
}
