package com.asensiodev.feature.searchmovies.impl.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal interface SearchMoviesEffects {
    val feedback: Flow<SearchMoviesEffect>
    val navigation: Flow<SearchMoviesNavigationEffect>
}

internal class MutableSearchMoviesEffects : SearchMoviesEffects {
    private val _feedback = MutableSharedFlow<SearchMoviesEffect>(extraBufferCapacity = 1)
    override val feedback: Flow<SearchMoviesEffect> = _feedback.asSharedFlow()

    private val _navigation =
        MutableSharedFlow<SearchMoviesNavigationEffect>(
            extraBufferCapacity = 1,
        )
    override val navigation: Flow<SearchMoviesNavigationEffect> = _navigation.asSharedFlow()

    suspend fun emitFeedback(effect: SearchMoviesEffect) {
        _feedback.emit(effect)
    }

    suspend fun emitNavigation(effect: SearchMoviesNavigationEffect) {
        _navigation.emit(effect)
    }
}
