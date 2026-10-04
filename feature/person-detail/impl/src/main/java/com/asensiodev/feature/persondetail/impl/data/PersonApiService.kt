package com.asensiodev.feature.persondetail.impl.data

import retrofit2.http.GET
import retrofit2.http.Path

internal interface PersonApiService {
    @GET("person/{person_id}")
    suspend fun person(
        @Path("person_id") id: Int,
    ): PersonApiModel

    @GET("person/{person_id}/movie_credits")
    suspend fun movieCredits(
        @Path("person_id") id: Int,
    ): PersonCreditsApiModel
}
