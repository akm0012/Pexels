package com.andrewkingmarshall.pexels.network.service

import com.andrewkingmarshall.pexels.network.responses.ImageSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PexelApiServiceInterface {

    @GET("search")
    suspend fun searchForPhotos(
        @Query("query") searchQuery: String,
        @Query("per_page") limitPerPage: Int,
        @Query("page") page: Int,
    ): ImageSearchResponse

}