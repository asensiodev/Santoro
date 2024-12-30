package com.asensiodev.feature.searchmovies.impl.data.model

import com.asensiodev.santoro.core.data.model.MovieApiModel
import com.google.gson.annotations.SerializedName

internal data class SearchMoviesResponseApiModel(
    @SerializedName("results")
    val results: List<MovieApiModel>,
    @SerializedName("page")
    val page: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int,
)
