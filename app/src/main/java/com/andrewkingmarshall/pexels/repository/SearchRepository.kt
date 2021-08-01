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
import com.andrewkingmarshall.pexels.work.DB_CLEANUP_TAG
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

const val PAGING_TAG = "paging"

const val DELAY_UNTIL_SEARCH_RETRY_MILLIS = 10 * 1000L // 10 seconds

/**
 * This repository is used to access all the objects that are produced from a Search.
 *
 * @property pexelApiService The API service where our photos come from.
 * @property imageDao The Data Access Object used to save [Images][Image] to our database.
 * @property searchDao The Data Access Object used to save [SearchQueries][SearchQuery]
 * @property searchHistoryCache A cache used to better optimize network performance by reducing the number
 *                              of duplicate network calls being made.
 */
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

    /**
     * Gets a [Flow] of [Images][Image] that match a specific [SearchQuery].
     *
     * @param searchQuery The image search query
     * @return A Flow of Images for a specific search query
     */
    fun getImageFlowForSearchQuery(searchQuery: String): Flow<List<Image>> {
        return searchDao.getSearchQueryWithImages(searchQuery.lowercase()).map {
            // Get the list of Images from the list of SearchQueryWithImages
            if (it.isNullOrEmpty() || it.first().images.isNullOrEmpty()) {
                emptyList()
            } else {
                it.first().images
            }
        }.map {
            // FIXME: There has got to be a way to do this in the database layer
            it.sortedBy { image -> image.serverOrder }
        }
    }

    /**
     * Searches the Pexel API for [Images][Image] that match the supplied query.
     *
     * @param _searchQuery The search query
     * @param page The page number that you want to request from the API
     */
    suspend fun executeSearch(_searchQuery: String, page: Int = PAGE_START) {
        val searchQuery = _searchQuery.lowercase()

        Timber.tag(PAGING_TAG)
            .v("Checking cache to see if we've already searched for '$searchQuery' page $page.")
        if (searchHistoryCache.hasSearchBeenPerformed(searchQuery, page)) {
            Timber.tag(PAGING_TAG)
                .i("We have already searched for '$searchQuery' at page: $page. Aborting...")
            return
        }

        Timber.tag(PAGING_TAG).v("Executing search for '$searchQuery' at page: $page")

        var shouldRetry = false

        do {

            shouldRetry = false

            try {
                val imageSearchResponse = pexelApiService.searchForImages(searchQuery, page = page)

                val imagesToSave = ArrayList<Image>()
                val imageSearchCrossRefsToSave = ArrayList<ImageSearchCrossRef>()
                val searchToSave = SearchQuery(searchQuery, getCurrentTimeInSec())

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

                when (cause) {
                    is UnknownHostException, is SocketTimeoutException -> {
                        Timber.w(cause, "Poor network connectivity, trying again soon...")
                        shouldRetry = true
                        delay(DELAY_UNTIL_SEARCH_RETRY_MILLIS)
                        Timber.d("Trying the search for '$searchQuery' for page $page again...")
                    }

                    else -> {
                        Timber.w(cause, "Unable to execute the search.")
                        throw cause
                    }
                }
            }
        } while (shouldRetry)
    }

    /**
     * Deletes all Searches and Images that were added before the expirationDate.
     *
     * @param expirationDateSec The date in which the Searches and Images expire in seconds.
     */
    suspend fun deleteOldSearches(expirationDateSec: Long) {
        try {
            val expiredSearches = searchDao.getSearchesCreatedBefore(expirationDateSec).first()
            val expiredImages = imageDao.getImagesCreatedBefore(expirationDateSec).first()

            Timber.tag(DB_CLEANUP_TAG).apply {
                d("About to delete ${expiredSearches.count()} Searches.")
                d("About to delete ${expiredImages.count()} Images.")
            }

            searchDao.deleteSearches(expiredSearches)
            imageDao.deleteImages(expiredImages)

            Timber.tag(DB_CLEANUP_TAG).d("Deletion complete.")

        } catch (cause: Exception) {
            Timber.tag(DB_CLEANUP_TAG).w(cause, "Unable to delete old Searches and Images.")
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