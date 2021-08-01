package com.andrewkingmarshall.pexels.viewmodels

import android.content.Context
import androidx.lifecycle.*
import androidx.work.*
import com.andrewkingmarshall.pexels.R
import com.andrewkingmarshall.pexels.network.service.PexelApiService.Companion.PAGE_LIMIT
import com.andrewkingmarshall.pexels.network.service.PexelApiService.Companion.PAGE_START
import com.andrewkingmarshall.pexels.network.service.PexelApiService.Companion.PAGING_THRESHOLD
import com.andrewkingmarshall.pexels.repository.PAGING_TAG
import com.andrewkingmarshall.pexels.repository.SearchRepository
import com.andrewkingmarshall.pexels.ui.domainmodels.MediaItem
import com.andrewkingmarshall.pexels.util.SingleLiveEvent
import com.andrewkingmarshall.pexels.work.CleanUpDatabaseWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val searchRepository: SearchRepository,
) : ViewModel() {

    init {
        scheduleDatabaseCleanUp()
    }

    val showError = SingleLiveEvent<String>()

    private val currentSearchQuery = MutableLiveData<String>()

    var screenWidth = 0

    var lastPositionClicked = 0

    val searchResults: LiveData<List<MediaItem>> =
        Transformations.switchMap(currentSearchQuery) { searchQuery ->
            searchRepository.getSearchQueryWithImagesFlow(searchQuery)
                .map {
                    if (it.isNullOrEmpty() || it.first().images.isNullOrEmpty()) {
                        emptyList()
                    } else {
                        it.first().images
                    }
                }.map {
                    it.sortedBy { image -> image.serverOrder }
                }.map { imageList ->
                    val displayData = ArrayList<MediaItem>()
                    imageList.forEach {
                        //todo: You could do more logic here to better determine which Url to use
                        // based on the screen's width
                        displayData.add(
                            MediaItem(
                                it.imageId,
                                it.mediumUrl,
                                it.largeUrl,
                                it.avgColor,
                                calculateDesiredDimenOfMediaPreview()
                            )
                        )
                    }
                    displayData
                }
                .asLiveData()
        }

    fun onSearchQueryChanged(newSearchQuery: String) {
        currentSearchQuery.value = newSearchQuery
        executeSearch(newSearchQuery)
    }

    /**
     * Called whenever an item is "bound" (or loaded) in the Recycler View. This is where we will
     * determine if we should load the next page.
     *
     * @param position The position that is being bound.
     */
    fun onItemBound(position: Int) {
        Timber.tag(PAGING_TAG).v("onItemBound: $position")

        if ((position + PAGING_THRESHOLD) % PAGE_LIMIT == 0) {
            val nextPage = ((position + PAGING_THRESHOLD) / PAGE_LIMIT) + PAGE_START

            Timber.tag(PAGING_TAG).d("Paging threshold reached. Get the next page: $nextPage")
            Timber.tag(PAGING_TAG).d("Position: $position")

            currentSearchQuery.value?.let {
                executeSearch(it, nextPage)
            }
        }
    }

    private fun executeSearch(newSearchQuery: String, page: Int = PAGE_START) {
        viewModelScope.launch {
            try {
                if (newSearchQuery.isNotBlank()) {
                    searchRepository.executeSearch(newSearchQuery, page)
                }
            } catch (e: Exception) {
                showError.value = e.localizedMessage
            }
        }
    }

    fun onWidthOfScreenDetermined(screenWidth: Int) {
        this.screenWidth = screenWidth
    }

    private fun calculateDesiredDimenOfMediaPreview(): Int {
        return screenWidth / context.resources.getInteger(R.integer.grid_columns)
    }

    /**
     * Schedule a periodic worker that will delete old searches and images once a day.
     */
    private fun scheduleDatabaseCleanUp() {
        Timber.d("Scheduling database clean up work.")

        val cleanUpDbWorkRequest = PeriodicWorkRequestBuilder<CleanUpDatabaseWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context).enqueue(cleanUpDbWorkRequest)
    }

}