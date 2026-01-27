package com.asensiodev.santoro.core.data.model

import com.google.gson.annotations.SerializedName

data class CrewMemberApiModel(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("job")
    val job: String?,
)
