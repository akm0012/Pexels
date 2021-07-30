package com.andrewkingmarshall.pexels.repository

import timber.log.Timber
import java.lang.IllegalStateException
import javax.inject.Inject

/**
 * This class is designed to keep track of what searches were performed.
 *
 * Note: This will only be kept alive in memory. Once the app is killed this data will be cleared.
 *
 * Ideally, we only want to execute 1 API Call per Search Query / Page pair.
 *
 */
class SearchHistoryCache @Inject constructor() {

    // The Key will be the Search Query and the Value is the highest page we have searched
    private val map = HashMap<String, Int>()

    /**
     * Checks to see if a specific search has been performed.
     *
     * @param searchQuery The search query
     * @param page The page of the search query
     * @return True if this search has already been performed, false if it has not.
     */
    fun hasSearchBeenPerformed(searchQuery: String, page: Int): Boolean {
        return map.containsKey(searchQuery) && map[searchQuery]!! >= page
    }

    /**
     * Lets the cache know this search has been performed.
     *
     * @param searchQuery The search query
     * @param page The page of the search query
     */
    fun recordSuccessfulSearch(searchQuery: String, page: Int) {

        if (map.containsKey(searchQuery) && map[searchQuery]!! > page) {
            Timber.w(IllegalStateException("You are trying to save a page for a search that has " +
                    "already been performed. You should use hasSearchBeenPerformed() to see if that" +
                    "call was necessary. SearchQuery = '$searchQuery' Page = $page"))
        } else {
            map[searchQuery] = page
        }
    }
}