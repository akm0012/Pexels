package com.andrewkingmarshall.pexels.inject

import android.content.Context
import androidx.room.Room
import com.andrewkingmarshall.pexels.database.AppDatabase
import com.andrewkingmarshall.pexels.database.dao.ImageDao
import com.andrewkingmarshall.pexels.database.dao.SearchDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "Pexel-Database"
        ).build()
    }

    @Provides
    fun provideMediaDao(db: AppDatabase): ImageDao {
        return db.imageDao()
    }

    @Provides
    fun provideSearchDao(db: AppDatabase): SearchDao {
        return db.searchDao()
    }

}