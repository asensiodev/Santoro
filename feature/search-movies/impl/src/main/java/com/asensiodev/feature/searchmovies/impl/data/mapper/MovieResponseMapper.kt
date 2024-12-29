package com.asensiodev.feature.searchmovies.impl.data.mapper

import com.asensiodev.feature.searchmovies.impl.data.model.SearchMoviesResponseApiModel

internal fun SearchMoviesResponseApiModel.toDomain() =
    results.map {
        it.toDomain()
    }
