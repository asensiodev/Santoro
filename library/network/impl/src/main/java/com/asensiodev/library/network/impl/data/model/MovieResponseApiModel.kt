package com.asensiodev.library.network.impl.data.model

import com.google.gson.annotations.SerializedName

internal data class MovieResponseApiModel(
    @SerializedName("results")
    val results: List<MovieApiModel>,
    @SerializedName("page")
    val page: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int,
)
