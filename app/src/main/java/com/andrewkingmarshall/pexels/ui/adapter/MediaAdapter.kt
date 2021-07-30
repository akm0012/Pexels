package com.andrewkingmarshall.pexels.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.andrewkingmarshall.pexels.R
import com.andrewkingmarshall.pexels.databinding.MediaItemBinding
import com.andrewkingmarshall.pexels.ui.domainmodels.MediaItem
import com.andrewkingmarshall.pexels.util.getCurrentTimeInSec
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class MediaAdapter :
    ListAdapter<MediaItem, MediaAdapter.ViewHolder>(MediaItemDiffCallback()) {

    /**
     * An interface used to listen for when an item is clicked.
     */
    interface OnMediaClickedListener {
        fun onMediaClicked(mediaItem: MediaItem)
    }

    var onMediaClickedListener: OnMediaClickedListener? = null

    /**
     * An interface used to listen to when an item is being bound.
     */
    interface OnBindListener {
        fun onPositionBound(position: Int)
    }

    var onBindListener: OnBindListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            MediaItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onBindListener?.onPositionBound(position)
        holder.bind(getItem(position), onMediaClickedListener)
    }

    class ViewHolder(
        private val binding: MediaItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mediaItem: MediaItem, mediaClickedListener: OnMediaClickedListener? = null) {

            // Set the ImageView to the exact size we want. As determined from the screen's current width
            binding.imageView.layoutParams.height = mediaItem.desiredDimen
            binding.imageView.layoutParams.width = mediaItem.desiredDimen
            binding.imageView.requestLayout()

            // Set the background to the average color while the image loads
            mediaItem.averageColor?.let { colorAsHex ->
                binding.imageView.setBackgroundColor(Color.parseColor(colorAsHex))
            }

            // Load the Image
            Glide.with(itemView)
                .load(mediaItem.urlPreview)
                .override(mediaItem.desiredDimen)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE) // Cache the decoded version of the image
                .into(binding.imageView)

            // Listen for clicks
            mediaClickedListener?.let { listener ->
                binding.imageView.setOnClickListener { listener.onMediaClicked(mediaItem) }
            }
        }
    }
}

private class MediaItemDiffCallback : DiffUtil.ItemCallback<MediaItem>() {

    override fun areItemsTheSame(
        oldItem: MediaItem,
        newItem: MediaItem
    ): Boolean {
        return oldItem.urlPreview == newItem.urlPreview
    }

    override fun areContentsTheSame(
        oldItem: MediaItem,
        newItem: MediaItem
    ): Boolean {
        return oldItem == newItem
    }
}
