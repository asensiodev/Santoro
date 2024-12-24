package com.asensiodev.library.network.impl.data.mapper

import com.asensiodev.library.network.impl.data.model.MovieResponseApiModel

internal fun MovieResponseApiModel.toDomain() = results.map { it.toDomain() }
