package com.andrewkingmarshall.pexels.repository

import com.andrewkingmarshall.pexels.database.dao.ImageDao
import com.andrewkingmarshall.pexels.database.dao.SearchDao
import com.andrewkingmarshall.pexels.database.entities.Image
import com.andrewkingmarshall.pexels.database.entities.ImageSearchCrossRef
import com.andrewkingmarshall.pexels.database.entities.SearchQuery
import com.andrewkingmarshall.pexels.database.entities.SearchQueryWithImages
import com.andrewkingmarshall.pexels.network.service.PexelApiService
import com.andrewkingmarshall.pexels.network.service.PexelApiService.Companion.PAGE_START
import com.andrewkingmarshall.pexels.util.getCurrentTimeInSec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val pexelApiService: PexelApiService,
    private val imageDao: ImageDao,
    private val searchDao: SearchDao,
    private val searchHistoryCache: SearchHistoryCache,
) {

    fun getSearchQueryWithImagesFlow(searchQuery: String): Flow<List<SearchQueryWithImages>?> {
        return searchDao.getSearchQueryWithImages(searchQuery.lowercase())
    }

    suspend fun executeSearch(_searchQuery: String, page: Int = PAGE_START) {
        val searchQuery = _searchQuery.lowercase()

        Timber.v("Checking cache to see if we've already performed this search.")
        if (searchHistoryCache.hasSearchBeenPerformed(searchQuery, page)) {
            Timber.i("We are already searched for '$searchQuery' page: $page. Aborting...")
            return
        }

        Timber.v("Executing search for '$searchQuery' at page: $page")

        try {
            val imageSearchResponse = pexelApiService.searchForImages(searchQuery, page = page)

            val imagesToSave = ArrayList<Image>()
            val imageSearchCrossRefsToSave = ArrayList<ImageSearchCrossRef>()
            val searchToSave = SearchQuery(searchQuery, getCurrentTimeInSec())

            // Todo: delete Images, Glide Data, and Searched from Database once per week

            // Convert the Dtos into Database objects
            imageSearchResponse.photos.forEach { dto ->
                val image = Image(dto)

                imagesToSave.add(image)

                imageSearchCrossRefsToSave.add(
                    ImageSearchCrossRef(
                        image.imageId,
                        searchToSave.searchQuery
                    )
                )
            }

            // Save everything to the database
            imageDao.insertImages(imagesToSave)
            searchDao.insertSearchQuery(searchToSave)
            searchDao.insertImageSearchCrossRefs(imageSearchCrossRefsToSave)

            // Record this search so we don't make it again
            searchHistoryCache.recordSuccessfulSearch(searchQuery, page)

            Timber.v("Done executing search for $searchQuery")

        } catch (cause: Exception) {
            Timber.w(cause, "Unable to execute the search.")
            throw cause
        }
    }

}