package com.andrewkingmarshall.pexels.ui.fragments

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.app.SharedElementCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.andrewkingmarshall.pexels.R
import com.andrewkingmarshall.pexels.databinding.FragmentMediaGridBinding
import com.andrewkingmarshall.pexels.extensions.toast
import com.andrewkingmarshall.pexels.ui.adapter.MediaAdapter
import com.andrewkingmarshall.pexels.ui.domainmodels.MediaItem
import com.andrewkingmarshall.pexels.viewmodels.SearchViewModel
import timber.log.Timber

class MediaGridFragment :
    BaseFragment<FragmentMediaGridBinding>(FragmentMediaGridBinding::inflate) {

    private lateinit var navController: NavController

    private lateinit var viewModel: SearchViewModel

    private val mediaAdapter = MediaAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(SearchViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        prepareTransitions()

        // Avoid a postponeEnterTransition on orientation change, and postpone only of first creation.
        if (savedInstanceState == null) {
            postponeEnterTransition()
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setup(view: View) {
        navController = Navigation.findNavController(view)

        viewModel.showError.observe(viewLifecycleOwner, { it.toast(requireContext()) })

        setUpSearchView()

        setUpRecyclerView()
    }

    private fun setUpSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
            override fun onMediaClicked(imageView: ImageView, mediaItem: MediaItem, position: Int) {
                Timber.d("onMediaClicked: ${mediaItem.urlPreview}")

                viewModel.lastPositionClicked = position

                val extras = FragmentNavigatorExtras(imageView to mediaItem.urlFullScreen)
                val action =
                    MediaGridFragmentDirections.actionMediaGridFragmentToImageDetailFragment(
                        mediaItem
                    )
                navController.navigate(action, extras)
            }
        }

        mediaAdapter.loadListener = object : MediaAdapter.OnLoadListener {
            override fun onLoadCompleted() {
                startPostponedEnterTransition()
            }
        }

        // Update the adapter with new search results
        viewModel.searchResults.observe(viewLifecycleOwner, {
            mediaAdapter.submitList(it)
        })
    }

    /**
     * Prepares the shared element transition to the Image Detail Fragment, as well as the other transitions
     * that affect the flow.
     */
    private fun prepareTransitions() {
        exitTransition = TransitionInflater.from(context)
            .inflateTransition(R.transition.grid_exit_transition)

        setExitSharedElementCallback(
            object : SharedElementCallback() {
                override fun onMapSharedElements(
                    names: List<String>,
                    sharedElements: MutableMap<String, View>
                ) {
                    // Locate the ViewHolder for the clicked position.
                    val selectedViewHolder: RecyclerView.ViewHolder = binding.mediaRecyclerView
                        .findViewHolderForAdapterPosition(viewModel.lastPositionClicked) ?: return

                    // Map the first shared element name to the child ImageView.
                    sharedElements[names[0]] =
                        selectedViewHolder.itemView.findViewById(R.id.imageView)
                }
            })
    }
}