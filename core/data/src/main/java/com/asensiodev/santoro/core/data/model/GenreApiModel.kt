package com.asensiodev.santoro.core.data.model

import com.google.gson.annotations.SerializedName

data class GenreApiModel(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
)
