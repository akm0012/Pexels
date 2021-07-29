package com.andrewkingmarshall.pexels.viewmodels

import androidx.lifecycle.*
import com.andrewkingmarshall.pexels.database.entities.Image
import com.andrewkingmarshall.pexels.database.entities.SearchQueryWithImages
import com.andrewkingmarshall.pexels.repository.SearchRepository
import com.andrewkingmarshall.pexels.ui.domainmodels.DisplayData
import com.andrewkingmarshall.pexels.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    val showError = SingleLiveEvent<String>()

    private val currentSearchQuery = MutableLiveData<String>()

    val searchResults: LiveData<List<DisplayData>> =
        Transformations.switchMap(currentSearchQuery) { searchQuery ->
            searchRepository.getSearchQueryWithImagesFlow(searchQuery)
                .map {
                    if (it.isNullOrEmpty() || it.first().images.isNullOrEmpty()) {
                        emptyList()
                    } else {
                        it.first().images
                    }
                }.map { imageList ->
                    val displayData = ArrayList<DisplayData>()
                    imageList.forEach {
                        //todo: pick the best URL for the current screen size
                      displayData.add(DisplayData(it.smallUrl, it.largeUrl))
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