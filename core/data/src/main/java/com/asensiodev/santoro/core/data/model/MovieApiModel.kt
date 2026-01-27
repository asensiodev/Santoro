package com.asensiodev.santoro.core.data.model

import com.google.gson.annotations.SerializedName

data class MovieApiModel(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("overview")
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("popularity")
    val popularity: Double?,
    @SerializedName("vote_average")
    val voteAverage: Double?,
    @SerializedName("vote_count")
    val voteCount: Int?,
    @SerializedName("genres")
    val genres: List<GenreApiModel>?,
    @SerializedName("production_countries")
    val productionCountries: List<ProductionCountryApiModel>?,
    @SerializedName("runtime")
    val runtime: Int?,
    @SerializedName("credits")
    val credits: CreditsApiModel?,
)
