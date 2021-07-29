package com.andrewkingmarshall.pexels.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.andrewkingmarshall.pexels.database.dao.ImageDao
import com.andrewkingmarshall.pexels.database.dao.SearchDao
import com.andrewkingmarshall.pexels.database.entities.Image
import com.andrewkingmarshall.pexels.database.entities.ImageSearchCrossRef
import com.andrewkingmarshall.pexels.database.entities.SearchQuery
import com.andrewkingmarshall.pexels.databinding.ActivityMainBinding
import com.andrewkingmarshall.pexels.network.interceptors.NetworkException
import com.andrewkingmarshall.pexels.network.service.PexelApiService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var apiService: PexelApiService

    @Inject
    lateinit var imageDao: ImageDao

    @Inject
    lateinit var searchDao: SearchDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {

            lifecycleScope.launch {

                try {
                    val response = apiService.searchForImages("Harry Potter")

                    Timber.d("${response.photos.size}")

                } catch (e: NetworkException) {
                    Timber.d("${e.localizedMessage}")

                }
            }
        }

        // Search for Dogs
        binding.writeSearchQuery1.setOnClickListener {

            val dogImage1 = Image(
                1,
                10,
                15,
                "white",
                "url1",
                "url2",
                "url3",
                "url4",
                "url5",
                "url6",
                System.currentTimeMillis() / 1000,
                System.currentTimeMillis() / 1000,
            )

            val dogImage2 = Image(
                2,
                20,
                25,
                "orange",
                "url1",
                "url2",
                "url3",
                "url4",
                "url5",
                "url6",
                System.currentTimeMillis() / 1000,
                System.currentTimeMillis() / 1000,
            )

            val dogSearchQuery = SearchQuery(
                "dog",
                System.currentTimeMillis() / 1000
            )

            val dogCrossRef = ImageSearchCrossRef(
                dogImage1.imageId,
                dogSearchQuery.searchQuery
            )

            val dogCrossRef2 = ImageSearchCrossRef(
                dogImage2.imageId,
                dogSearchQuery.searchQuery
            )

            lifecycleScope.launch {
                imageDao.insertImage(dogImage1)
                imageDao.insertImage(dogImage2)
                searchDao.insertSearchQuery(dogSearchQuery)
                searchDao.insertImageSearchCrossRef(dogCrossRef)
                searchDao.insertImageSearchCrossRef(dogCrossRef2)

                Timber.i("Done writing dogs to db")
            }

        }

        // Search for Cats
        binding.writeSearchQuery2.setOnClickListener {

            val dogImage1 = Image(
                3,
                100,
                150,
                "white",
                "url1",
                "url2",
                "url3",
                "url4",
                "url5",
                "url6",
                System.currentTimeMillis() / 1000,
                System.currentTimeMillis() / 1000,
            )

            val dogImage2 = Image(
                4,
                200,
                250,
                "orange",
                "url1",
                "url2",
                "url3",
                "url4",
                "url5",
                "url6",
                System.currentTimeMillis() / 1000,
                System.currentTimeMillis() / 1000,
            )

            val dogSearchQuery = SearchQuery(
                "other dog",
                System.currentTimeMillis() / 1000
            )

            val dogCrossRef = ImageSearchCrossRef(
                dogImage1.imageId,
                dogSearchQuery.searchQuery
            )

            val dogCrossRef2 = ImageSearchCrossRef(
                dogImage2.imageId,
                dogSearchQuery.searchQuery
            )

            lifecycleScope.launch {
                imageDao.insertImage(dogImage1)
                imageDao.insertImage(dogImage2)
                searchDao.insertSearchQuery(dogSearchQuery)
                searchDao.insertImageSearchCrossRef(dogCrossRef)
                searchDao.insertImageSearchCrossRef(dogCrossRef2)

                Timber.i("Done writing dogs to db")
            }

        }

        // Get Dogs
        binding.getResults.setOnClickListener {
            lifecycleScope.launch {
                val dogsSearchQueryWithImages = searchDao.getSearchQueryWithImages("dog")

                Timber.d("Bam! $dogsSearchQueryWithImages")
            }
        }

        // Get Other dogs
        binding.getResults2.setOnClickListener {
            lifecycleScope.launch {

                val dogsSearchQueryWithImages = searchDao.getSearchQueryWithImages("other dog")

                Timber.d("Bam! $dogsSearchQueryWithImages")
            }
        }

    }
}