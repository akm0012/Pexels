package com.andrewkingmarshall.pexels.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.andrewkingmarshall.pexels.database.dao.ImageDao
import com.andrewkingmarshall.pexels.database.dao.SearchDao
import com.andrewkingmarshall.pexels.database.entities.Image
import com.andrewkingmarshall.pexels.database.entities.ImageSearchCrossRef
import com.andrewkingmarshall.pexels.database.entities.SearchQuery

@Database(
    entities = [
        Image::class,
        SearchQuery::class,
        ImageSearchCrossRef::class,
    ], version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
    abstract fun searchDao(): SearchDao
}
