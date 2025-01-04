package com.asensiodev.feature.searchmovies.impl.data.mapper

import com.asensiodev.core.domain.MovieDetail
import com.asensiodev.feature.searchmovies.impl.data.model.SearchMoviesResponseApiModel
import com.asensiodev.santoro.core.data.mapper.toDomain
import com.asensiodev.santoro.core.data.model.MovieApiModel

internal fun SearchMoviesResponseApiModel.toDomain(): List<MovieDetail> =
    results.map { apiModel: MovieApiModel ->
        apiModel.toDomain()
    }
