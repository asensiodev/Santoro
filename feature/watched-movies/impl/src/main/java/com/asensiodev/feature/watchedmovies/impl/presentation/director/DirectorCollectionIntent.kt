package com.asensiodev.feature.watchedmovies.impl.presentation.director

internal sealed interface DirectorCollectionIntent {
    data class Initialize(
        val personId: Int,
        val name: String,
    ) : DirectorCollectionIntent

    data object RetryFilmography : DirectorCollectionIntent

    data class ChangeShowPending(
        val show: Boolean,
    ) : DirectorCollectionIntent
}
