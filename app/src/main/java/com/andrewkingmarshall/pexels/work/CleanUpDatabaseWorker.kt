package com.andrewkingmarshall.pexels.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.andrewkingmarshall.pexels.repository.SearchRepository
import com.andrewkingmarshall.pexels.util.getCurrentTimeInSec
import com.andrewkingmarshall.pexels.util.getSecondsInXDays
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.lang.Exception

// Delete all searches that happened at least this many days ago
const val SEARCH_EXPIRATION_DAYS = 5

const val DB_CLEANUP_TAG = "DB Cleanup"

/**
 * This Worker will periodically clean up our database by deleting images / searches that have not
 * been performed in a while.
 *
 */
@HiltWorker
class CleanUpDatabaseWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val searchRepository: SearchRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag(DB_CLEANUP_TAG).d("Starting to clean up the data base.")

        try {

            val expirationTime = getCurrentTimeInSec() - getSecondsInXDays(SEARCH_EXPIRATION_DAYS)

            searchRepository.deleteOldSearches(expirationTime)

        } catch (cause: Exception) {
            Timber.tag(DB_CLEANUP_TAG).w(cause, "Database clean up failed.")
            return Result.failure()
        }

        Timber.tag(DB_CLEANUP_TAG).d("Deleted all searches and images that occurred $SEARCH_EXPIRATION_DAYS days ago.")
        return Result.success()
    }

}