package com.andrewkingmarshall.pexels.repository

import com.andrewkingmarshall.pexels.database.dao.ImageDao
import com.andrewkingmarshall.pexels.database.dao.SearchDao
import com.andrewkingmarshall.pexels.database.entities.SearchQueryWithImages
import com.andrewkingmarshall.pexels.network.service.PexelApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val pexelApiService: PexelApiService,
    private val imageDao: ImageDao,
    private val searchDao: SearchDao,
) {

    fun getSearchQueryWithImagesFlow(searchQuery: String) : Flow<List<SearchQueryWithImages>?> {



        return searchDao.getSearchQueryWithImages(searchQuery)
    }

}