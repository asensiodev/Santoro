package com.asensiodev.santoro.core.data.model

import com.google.gson.annotations.SerializedName

data class CastMemberApiModel(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("character")
    val character: String?,
    @SerializedName("profile_path")
    val profilePath: String?,
)
