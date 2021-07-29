package com.andrewkingmarshall.pexels.database.entities

import androidx.room.Entity
import androidx.room.Index

/**
 * A associative entity between an [Image] and a [SearchQuery].
 *
 * @property imageId The Id of the Image.
 * @property searchQuery The search query. (Primary key of the [SearchQuery])
 */
@Entity(
    primaryKeys = [IMAGE_PRIMARY_KEY, SEARCH_QUERY_PRIMARY_KEY],
    indices = [Index(value = [SEARCH_QUERY_PRIMARY_KEY])]
)
data class ImageSearchCrossRef(
    val imageId: Long,
    val searchQuery: String,
)
