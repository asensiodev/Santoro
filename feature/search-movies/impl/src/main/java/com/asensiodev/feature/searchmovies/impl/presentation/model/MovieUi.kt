package com.asensiodev.feature.searchmovies.impl.presentation.model

import com.asensiodev.feature.searchmovies.impl.domain.model.MovieLibraryStatus

internal data class MovieUi(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double,
    val genreIds: List<Int> = emptyList(),
    val libraryStatus: MovieLibraryStatus? = null,
)
