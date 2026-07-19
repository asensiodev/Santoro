package com.asensiodev.santoro.core.data.model

import com.google.gson.annotations.SerializedName

data class CastMemberApiModel(
    @SerializedName("id")
    val id: Int,
    @SerializedName("credit_id")
    val creditId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("character")
    val character: String?,
    @SerializedName("profile_path")
    val profilePath: String?,
)
