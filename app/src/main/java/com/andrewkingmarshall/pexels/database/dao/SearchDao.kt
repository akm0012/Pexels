package com.andrewkingmarshall.pexels.database.dao

import androidx.room.*
import com.andrewkingmarshall.pexels.database.entities.ImageSearchCrossRef
import com.andrewkingmarshall.pexels.database.entities.SearchQuery
import com.andrewkingmarshall.pexels.database.entities.SearchQueryWithImages
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(searchQuery: SearchQuery)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQueries(searchQueries: List<SearchQuery>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImageSearchCrossRef(searchCrossRef: ImageSearchCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImageSearchCrossRefs(searchCrossRefs: List<ImageSearchCrossRef>)

    @Transaction
    @Query("SELECT * FROM SearchQuery WHERE searchQuery = :searchQuery")
    fun getSearchQueryWithImages(searchQuery: String): Flow<List<SearchQueryWithImages>?>
}