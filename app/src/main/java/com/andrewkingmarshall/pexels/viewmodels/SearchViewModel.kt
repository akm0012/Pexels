package com.andrewkingmarshall.pexels.viewmodels

import androidx.lifecycle.*
import com.andrewkingmarshall.pexels.repository.SearchRepository
import com.andrewkingmarshall.pexels.ui.domainmodels.MediaItem
import com.andrewkingmarshall.pexels.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    val showError = SingleLiveEvent<String>()

    private val currentSearchQuery = MutableLiveData<String>()

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
                        //todo: pick the best URL for the current screen size
                      displayData.add(MediaItem(it.smallUrl, it.largeUrl, it.avgColor))
                    }
                    displayData
                }
                .asLiveData()
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