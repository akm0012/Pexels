package com.andrewkingmarshall.pexels.viewmodels

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.andrewkingmarshall.pexels.R
import com.andrewkingmarshall.pexels.network.service.PexelApiService.Companion.PAGE_LIMIT
import com.andrewkingmarshall.pexels.network.service.PexelApiService.Companion.PAGE_START
import com.andrewkingmarshall.pexels.network.service.PexelApiService.Companion.PAGING_THRESHOLD
import com.andrewkingmarshall.pexels.repository.SearchRepository
import com.andrewkingmarshall.pexels.ui.domainmodels.MediaItem
import com.andrewkingmarshall.pexels.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val searchRepository: SearchRepository,
) : ViewModel() {

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
                }.map { imageList ->
                    val displayData = ArrayList<MediaItem>()
                    imageList.forEach {
                        //todo: You could do more logic here to better determine which Url to use
                        // based on the screen's width
                        displayData.add(
                            MediaItem(
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
        Timber.v("onItemBound: $position")

        if ((position + PAGING_THRESHOLD) % PAGE_LIMIT == 0) {
            val nextPage = ((position + PAGING_THRESHOLD) / PAGE_LIMIT) + PAGE_START

            Timber.d("Paging threshold reached. Get the next page: $nextPage")

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


}