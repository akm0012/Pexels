package com.andrewkingmarshall.pexels.network.service

import com.andrewkingmarshall.pexels.network.responses.ImageSearchResponse
import retrofit2.Retrofit
import javax.inject.Inject

class PexelApiService @Inject constructor(retrofit: Retrofit) {

    private val apiServiceInterface by lazy {
        retrofit.create(PexelApiServiceInterface::class.java)
    }

    /**
     * Searches for Pexel Photos based on some query.
     *
     * @param searchQuery The search query for what kind of photos you want to see.
     * @param limitPerPage How many results you want per page.
     * @param page What page of results do you want.
     * @return A list of photos along with some metadata. See [ImageSearchResponse]
     */
    suspend fun searchForImages(
        searchQuery: String,
        limitPerPage: Int = PAGE_LIMIT,
        page: Int = PAGE_START,
    ): ImageSearchResponse {
        return apiServiceInterface.searchForPhotos(searchQuery, limitPerPage, page)
    }

    companion object {
        const val PAGE_LIMIT = 30;
        const val PAGE_START = 1;
        const val PAGING_THRESHOLD = 3;
    }
}