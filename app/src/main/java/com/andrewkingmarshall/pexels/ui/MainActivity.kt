package com.andrewkingmarshall.pexels.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.andrewkingmarshall.pexels.databinding.ActivityMainBinding
import com.andrewkingmarshall.pexels.extensions.screenSizeInPx
import com.andrewkingmarshall.pexels.viewmodels.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)

        viewModel.onWidthOfScreenDetermined(this.screenSizeInPx.x)
    }
}