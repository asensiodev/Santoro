package com.asensiodev.feature.persondetail.impl.data

import com.google.gson.annotations.SerializedName

internal data class PersonApiModel(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("biography") val biography: String?,
    @SerializedName("profile_path") val profilePath: String?,
    @SerializedName("birthday") val birthday: String?,
    @SerializedName("deathday") val deathday: String?,
    @SerializedName("place_of_birth") val birthplace: String?,
    @SerializedName("known_for_department") val knownForDepartment: String? = null,
)

internal data class PersonCreditsApiModel(
    @SerializedName("cast") val cast: List<PersonCreditApiModel>?,
    @SerializedName("crew") val crew: List<PersonCreditApiModel>?,
)

internal data class PersonCreditApiModel(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("character") val character: String?,
    @SerializedName("job") val job: String?,
)
