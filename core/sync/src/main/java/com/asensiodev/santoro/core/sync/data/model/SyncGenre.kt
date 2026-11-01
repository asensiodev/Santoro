package com.asensiodev.santoro.core.sync.data.model

import com.google.gson.annotations.SerializedName

internal data class SyncGenre(
    @SerializedName(value = "id", alternate = ["a"]) val id: Int?,
    @SerializedName(value = "name", alternate = ["b"]) val name: String?,
)
