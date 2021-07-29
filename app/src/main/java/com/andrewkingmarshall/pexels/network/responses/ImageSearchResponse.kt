package com.andrewkingmarshall.pexels.network.responses

import com.andrewkingmarshall.pexels.network.dtos.PexelImageDto
import com.google.gson.annotations.SerializedName

data class ImageSearchResponse(
    val page: Int,
    @SerializedName("per_page") val limitPerPage: Int,
    val photos: List<PexelImageDto> = ArrayList(),
    @SerializedName("total_results") val totalResults: Int,
    @SerializedName("next_page") val nextPage: String,
)
