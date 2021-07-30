package com.andrewkingmarshall.pexels.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.andrewkingmarshall.pexels.R
import com.andrewkingmarshall.pexels.repository.SearchRepository
import com.andrewkingmarshall.pexels.ui.domainmodels.MediaItem
import com.andrewkingmarshall.pexels.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val searchRepository: SearchRepository,
) : ViewModel() {

    val showError = SingleLiveEvent<String>()

    private val currentSearchQuery = MutableLiveData<String>()

    var screenWidth = 0

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

    fun onWidthOfScreenDetermined(screenWidth: Int) {
        this.screenWidth = screenWidth
    }

    private fun calculateDesiredDimenOfMediaPreview(): Int {
        return screenWidth / context.resources.getInteger(R.integer.grid_columns)
    }

    fun onSearchQueryChanged(newSearchQuery: String) {
        currentSearchQuery.value = newSearchQuery
        viewModelScope.launch {
            try {
                if (newSearchQuery.isNotBlank()) {
                    searchRepository.executeSearch(newSearchQuery)
                }
            } catch (e: Exception) {
                showError.value = e.localizedMessage
            }
        }
    }

}