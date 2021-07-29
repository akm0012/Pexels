package com.andrewkingmarshall.pexels.database.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class SearchQueryWithImages(
    @Embedded val searchQuery: SearchQuery,

    @Relation(
        parentColumn = SEARCH_QUERY_PRIMARY_KEY,
        entityColumn = IMAGE_PRIMARY_KEY,
        associateBy = Junction(ImageSearchCrossRef::class)
    )
    val images: List<Image>?
)
