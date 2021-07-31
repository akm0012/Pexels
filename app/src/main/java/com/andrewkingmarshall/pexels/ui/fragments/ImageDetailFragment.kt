package com.andrewkingmarshall.pexels.ui.fragments

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import androidx.navigation.fragment.navArgs
import com.andrewkingmarshall.pexels.R
import com.andrewkingmarshall.pexels.databinding.FragmentImageDetailBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class ImageDetailFragment :
    BaseFragment<FragmentImageDetailBinding>(FragmentImageDetailBinding::inflate) {

    private val args: ImageDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        prepareSharedElementTransition()

        // Avoid a postponeEnterTransition on orientation change, and postpone only of first creation.
        if (savedInstanceState == null) {
            postponeEnterTransition()
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setup(view: View) {

        // Set the background to the average color
        args.mediaItem.averageColor?.let { colorAsHex ->
            binding.background.setBackgroundColor(Color.parseColor(colorAsHex))
        }

        // Load the full size image
        val imageUrl = args.mediaItem.urlFullScreen
        binding.fullScreenImageView.apply {
            transitionName = imageUrl
            Glide.with(this)
                .load(imageUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        startPostponedEnterTransition()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        startPostponedEnterTransition()
                        return false
                    }
                })
                .into(this)
        }
    }

    /**
     * Prepares the shared element transition from and back to the grid fragment.
     */
    private fun prepareSharedElementTransition() {
        val transition = TransitionInflater.from(context)
            .inflateTransition(R.transition.image_shared_element_transition)

        sharedElementEnterTransition = transition

        // A similar mapping is set at the MediaGridFragment with a setExitSharedElementCallback.
        setEnterSharedElementCallback(
            object : SharedElementCallback() {
                override fun onMapSharedElements(
                    names: List<String>,
                    sharedElements: MutableMap<String, View>
                ) {
                    // Map the first shared element name to the child ImageView.
                    sharedElements[names[0]] = binding.fullScreenImageView
                }
            })
    }
}