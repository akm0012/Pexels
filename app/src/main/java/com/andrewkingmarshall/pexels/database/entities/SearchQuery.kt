package com.andrewkingmarshall.pexels.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andrewkingmarshall.pexels.util.getCurrentTimeInSec

/**
 * Represents something that was searched for.
 *
 * @property searchQuery The text that was searched for.
 * @property dateSearched The date this search was performed. Represented in seconds that have elapsed since the Unix epoch.
 */
@Entity
data class SearchQuery(
    @PrimaryKey val searchQuery: String,
    val dateSearched: Long = getCurrentTimeInSec(),
)

const val SEARCH_QUERY_PRIMARY_KEY = "searchQuery"
