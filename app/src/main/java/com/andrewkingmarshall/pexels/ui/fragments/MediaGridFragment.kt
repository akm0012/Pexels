package com.andrewkingmarshall.pexels.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.andrewkingmarshall.pexels.databinding.FragmentMediaGridBinding
import com.andrewkingmarshall.pexels.extensions.toast
import com.andrewkingmarshall.pexels.ui.adapter.MediaAdapter
import com.andrewkingmarshall.pexels.ui.domainmodels.MediaItem
import com.andrewkingmarshall.pexels.viewmodels.SearchViewModel
import timber.log.Timber

class MediaGridFragment : BaseFragment<FragmentMediaGridBinding>(FragmentMediaGridBinding::inflate) {

    private lateinit var navController: NavController

    private lateinit var viewModel: SearchViewModel

    private val mediaAdapter = MediaAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(SearchViewModel::class.java)
    }

    override fun setup(view: View) {
        navController = Navigation.findNavController(view)

        viewModel.showError.observe(viewLifecycleOwner, { it.toast(requireContext()) })

        setUpSearchView()

        setUpRecyclerView()
    }

    private fun setUpSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Don't care, we will update the results in real time with onQueryTextChange
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.onSearchQueryChanged(it) }
                return true
            }
        })
    }

    private fun setUpRecyclerView() {

        binding.mediaRecyclerView.adapter = mediaAdapter

        mediaAdapter.onBindListener = object : MediaAdapter.OnBindListener {
            override fun onPositionBound(position: Int) {
                viewModel.onItemBound(position)
            }
        }

        mediaAdapter.onMediaClickedListener = object : MediaAdapter.OnMediaClickedListener {
            override fun onMediaClicked(mediaItem: MediaItem) {
                Timber.d("onMediaClicked: ${mediaItem.urlPreview}")
            }
        }

        viewModel.searchResults.observe(viewLifecycleOwner, {
            mediaAdapter.submitList(it)
        })
    }
}