package com.asensiodev.feature.searchmovies.impl.data.model

import com.google.gson.annotations.SerializedName

internal data class SearchMoviesResponseApiModel(
    @SerializedName("results")
    val results: List<SearchMoviesApiModel>,
    @SerializedName("page")
    val page: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int,
)
