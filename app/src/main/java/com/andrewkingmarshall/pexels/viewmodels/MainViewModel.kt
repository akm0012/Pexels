package com.andrewkingmarshall.pexels.viewmodels

import androidx.lifecycle.*
import com.andrewkingmarshall.pexels.database.entities.Image
import com.andrewkingmarshall.pexels.database.entities.SearchQueryWithImages
import com.andrewkingmarshall.pexels.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    private val currentSearchQuery = MutableLiveData<String>()

    val searchResults: LiveData<List<Image>> =
        Transformations.switchMap(currentSearchQuery) { searchQuery ->
            searchRepository.getSearchQueryWithImagesFlow(searchQuery)
                .map {
                    if (it.isNullOrEmpty() || it.first().images.isNullOrEmpty()) {
                        emptyList()
                    } else {
                        it.first().images
                    }
                }
                .asLiveData()
        }

    fun onSearchQueryChanged(newSearchQuery: String) {
        currentSearchQuery.value = newSearchQuery
    }

}