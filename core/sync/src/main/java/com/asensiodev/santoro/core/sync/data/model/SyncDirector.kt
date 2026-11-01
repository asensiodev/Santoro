package com.asensiodev.santoro.core.sync.data.model

import com.google.gson.annotations.SerializedName

internal data class SyncDirector(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
)
