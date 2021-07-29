package com.andrewkingmarshall.pexels.database.dao

import androidx.room.*
import com.andrewkingmarshall.pexels.database.entities.Image
import com.andrewkingmarshall.pexels.database.entities.ImageSearchCrossRef
import com.andrewkingmarshall.pexels.database.entities.SearchQuery
import com.andrewkingmarshall.pexels.database.entities.SearchQueryWithImages

@Dao
interface ImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: Image)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<Image>)

}
