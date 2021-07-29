package com.andrewkingmarshall.pexels.database.dao

import androidx.room.*
import com.andrewkingmarshall.pexels.database.entities.ImageSearchCrossRef
import com.andrewkingmarshall.pexels.database.entities.SearchQuery
import com.andrewkingmarshall.pexels.database.entities.SearchQueryWithImages

@Dao
interface SearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(searchQuery: SearchQuery)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImageSearchCrossRef(searchCrossRef: ImageSearchCrossRef)

    @Transaction
    @Query("SELECT * FROM SearchQuery WHERE searchQuery = :searchQuery")
    suspend fun getSearchQueryWithImages(searchQuery: String): List<SearchQueryWithImages>
}