package com.andrewkingmarshall.pexels.repository

import com.andrewkingmarshall.pexels.BuildConfig
import com.andrewkingmarshall.pexels.database.dao.ImageDao
import com.andrewkingmarshall.pexels.database.dao.SearchDao
import com.andrewkingmarshall.pexels.database.entities.Image
import com.andrewkingmarshall.pexels.database.entities.ImageSearchCrossRef
import com.andrewkingmarshall.pexels.database.entities.SearchQuery
import com.andrewkingmarshall.pexels.database.entities.SearchQueryWithImages
import com.andrewkingmarshall.pexels.network.dtos.PexelImageDto
import com.andrewkingmarshall.pexels.network.service.PexelApiService
import com.andrewkingmarshall.pexels.network.service.PexelApiService.Companion.PAGE_LIMIT
import com.andrewkingmarshall.pexels.network.service.PexelApiService.Companion.PAGE_START
import com.andrewkingmarshall.pexels.util.getCurrentTimeInSec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

const val PAGING_TAG = "paging"

@Singleton
class SearchRepository @Inject constructor(
    private val pexelApiService: PexelApiService,
    private val imageDao: ImageDao,
    private val searchDao: SearchDao,
    private val searchHistoryCache: SearchHistoryCache,
) {

    // Used for debugging, Pexel API has been serving duplicates which throws off the paging
    private val sanityCheckListIds = ArrayList<String>()
    private val sanityCheckListUrlPreview = ArrayList<String>()

    fun getSearchQueryWithImagesFlow(searchQuery: String): Flow<List<SearchQueryWithImages>?> {
        return searchDao.getSearchQueryWithImages(searchQuery.lowercase())
    }

    suspend fun executeSearch(_searchQuery: String, page: Int = PAGE_START) {
        val searchQuery = _searchQuery.lowercase()

        Timber.tag(PAGING_TAG).v("Checking cache to see if we've already searched for '$searchQuery' page $page.")
        if (searchHistoryCache.hasSearchBeenPerformed(searchQuery, page)) {
            Timber.tag(PAGING_TAG).i("We have already searched for '$searchQuery' at page: $page. Aborting...")
            return
        }

        Timber.tag(PAGING_TAG).v("Executing search for '$searchQuery' at page: $page")

        try {
            val imageSearchResponse = pexelApiService.searchForImages(searchQuery, page = page)

            val imagesToSave = ArrayList<Image>()
            val imageSearchCrossRefsToSave = ArrayList<ImageSearchCrossRef>()
            val searchToSave = SearchQuery(searchQuery, getCurrentTimeInSec())

            // Todo: delete Images, Glide Data, and Searched from Database once per week
            // Todo: Try to re-call this function if it fails because it is offline
            // Remove akm tags

            // Convert the Dtos into Database objects
            imageSearchResponse.photos.forEachIndexed { index, dto ->

                // Checks the list for duplicated photos (debug only)
                checkForDuplicates(dto, page)

                val serverOrder = ((page - 1) * PAGE_LIMIT) + index
                Timber.d("Server order: $serverOrder")

                val image = Image(dto, page, serverOrder)

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

            Timber.tag(PAGING_TAG).v("Done executing search for $searchQuery at page $page.")

        } catch (cause: Exception) {
            Timber.w(cause, "Unable to execute the search.")
            throw cause
        }
    }

    private fun checkForDuplicates(
        dto: PexelImageDto,
        page: Int
    ) {
        if (BuildConfig.DEBUG) {
            val itemId = "${dto.id}_$page"
            val previewUrl = dto.src.medium

            if (sanityCheckListIds.contains(itemId)) {
                Timber.tag(PAGING_TAG).w("We have already seen that ID: $itemId")
            } else {
                sanityCheckListIds.add(itemId)
            }

            if (sanityCheckListUrlPreview.contains(previewUrl)) {
                Timber.tag(PAGING_TAG).w("We have already seen that url: $previewUrl")
            } else {
                sanityCheckListUrlPreview.add(previewUrl)
            }
        }
    }

}