package com.andrewkingmarshall.pexels.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.andrewkingmarshall.pexels.R
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
    }
}