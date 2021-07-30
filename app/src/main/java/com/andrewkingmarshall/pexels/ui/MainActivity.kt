package com.andrewkingmarshall.pexels.ui

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.andrewkingmarshall.pexels.R
import com.andrewkingmarshall.pexels.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

//    @Inject
//    lateinit var apiService: PexelApiService
//
//    @Inject
//    lateinit var appDatabase: AppDatabase
//
//    @Inject
//    lateinit var imageDao: ImageDao
//
//    @Inject
//    lateinit var searchDao: SearchDao
//
//    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

//        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                Timber.tag("akm").d("onQueryTextSubmit: $query")
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                Timber.tag("akm").d("onQueryTextChange: $newText")
//                newText?.let {viewModel.onSearchQueryChanged(it) }
//                return true
//            }
//
//        })
//
//        viewModel.searchResults.observe(this, { searchResults ->
//            if (searchResults.isEmpty()) {
//                Timber.tag("akm").d("0 results")
//            } else {
//                Timber.tag("akm").d("${searchResults.size} results")
//                searchResults.forEach {
//                    Timber.tag("akm").d("DisplayData: ${it.urlFullScreen}")
//                }
//            }
//        })
    }

//    private fun testDatabaseCode() {
//        binding.button.setOnClickListener {
//
//            lifecycleScope.launch {
//
//                try {
//                    val response = apiService.searchForImages("Harry Potter")
//
//                    Timber.tag("akm").d("${response.photos.size}")
//
//                } catch (e: NetworkException) {
//                    Timber.tag("akm").d("${e.localizedMessage}")
//
//                }
//            }
//        }
//
//        // Search for Dogs
//        binding.writeSearchQuery1.setOnClickListener {
//
//            val dogImage1 = Image(
//                1,
//                10,
//                15,
//                "white",
//                "url1",
//                "url2",
//                "url3",
//                "url4",
//                "url5",
//                "url6",
//                System.currentTimeMillis() / 1000,
//            )
//
//            val dogImage2 = Image(
//                2,
//                20,
//                25,
//                "orange",
//                "url1",
//                "url2",
//                "url3",
//                "url4",
//                "url5",
//                "url6",
//                System.currentTimeMillis() / 1000,
//            )
//
//            val dogSearchQuery = SearchQuery(
//                "dog",
//                System.currentTimeMillis() / 1000
//            )
//
//            val dogCrossRef = ImageSearchCrossRef(
//                dogImage1.imageId,
//                dogSearchQuery.searchQuery
//            )
//
//            val dogCrossRef2 = ImageSearchCrossRef(
//                dogImage2.imageId,
//                dogSearchQuery.searchQuery
//            )
//
//            lifecycleScope.launch {
//                imageDao.insertImage(dogImage1)
//                imageDao.insertImage(dogImage2)
//                searchDao.insertSearchQuery(dogSearchQuery)
//                searchDao.insertImageSearchCrossRef(dogCrossRef)
//                searchDao.insertImageSearchCrossRef(dogCrossRef2)
//
//                Timber.tag("akm").i("Done writing dogs to db")
//            }
//
//        }
//
//        // Search for Cats
//        binding.writeSearchQuery2.setOnClickListener {
//
//            val dogImage1 = Image(
//                3,
//                100,
//                150,
//                "white",
//                "url1",
//                "url2",
//                "url3",
//                "url4",
//                "url5",
//                "url6",
//                System.currentTimeMillis() / 1000,
//            )
//
//            val dogImage2 = Image(
//                4,
//                200,
//                250,
//                "orange",
//                "url1",
//                "url2",
//                "url3",
//                "url4",
//                "url5",
//                "url6",
//                System.currentTimeMillis() / 1000,
//            )
//
//            val dogSearchQuery = SearchQuery(
//                "other dog",
//                System.currentTimeMillis() / 1000
//            )
//
//            val dogCrossRef = ImageSearchCrossRef(
//                dogImage1.imageId,
//                dogSearchQuery.searchQuery
//            )
//
//            val dogCrossRef2 = ImageSearchCrossRef(
//                dogImage2.imageId,
//                dogSearchQuery.searchQuery
//            )
//
//            lifecycleScope.launch {
//                imageDao.insertImage(dogImage1)
//                imageDao.insertImage(dogImage2)
//                searchDao.insertSearchQuery(dogSearchQuery)
//                searchDao.insertImageSearchCrossRef(dogCrossRef)
//                searchDao.insertImageSearchCrossRef(dogCrossRef2)
//
//                Timber.tag("akm").i("Done writing other dogs to db")
//            }
//
//        }
//
//        // Get Dogs
//        binding.getResults.setOnClickListener {
//            lifecycleScope.launch {
//                val dogsSearchQueryWithImages = searchDao.getSearchQueryWithImages("dog")
//
//                Timber.tag("akm").d("Bam! $dogsSearchQueryWithImages")
//            }
//        }
//
//        // Get Other dogs
//        binding.getResults2.setOnClickListener {
//            lifecycleScope.launch {
//
//                val dogsSearchQueryWithImages = searchDao.getSearchQueryWithImages("other dog")
//
//                Timber.tag("akm").d("Bam! $dogsSearchQueryWithImages")
//            }
//        }
//
//        binding.nuke.setOnClickListener {
//            CoroutineScope(Dispatchers.IO).launch {
//                appDatabase.clearAllTables()
//            }
//        }
//    }
}