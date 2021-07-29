package com.andrewkingmarshall.pexels.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents something that was searched for.
 *
 * @property searchQuery The text that was searched for.
 * @property lastDateSearched The date this search was performed. Represented in seconds that have elapsed since the Unix epoch.
 */
@Entity
data class SearchQuery(
    @PrimaryKey val searchQuery: String,
    val lastDateSearched: Long,
)

const val SEARCH_QUERY_PRIMARY_KEY = "searchQuery"
