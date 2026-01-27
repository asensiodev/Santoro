package com.asensiodev.santoro.core.data.model

import com.google.gson.annotations.SerializedName

data class CreditsApiModel(
    @SerializedName("cast")
    val cast: List<CastMemberApiModel>?,
)
