package com.andrewkingmarshall.pexels.ui.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.andrewkingmarshall.pexels.databinding.MediaItemBinding
import com.andrewkingmarshall.pexels.ui.domainmodels.MediaItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class MediaAdapter :
    ListAdapter<MediaItem, MediaAdapter.ViewHolder>(MediaItemDiffCallback()) {

    /**
     * An interface used to listen for when an item is clicked.
     */
    interface OnMediaClickedListener {
        fun onMediaClicked(imageView: ImageView, mediaItem: MediaItem, position: Int)
    }

    var onMediaClickedListener: OnMediaClickedListener? = null

    /**
     * An interface used to listen to when an item is being bound.
     */
    interface OnBindListener {
        fun onPositionBound(position: Int)
    }

    var onBindListener: OnBindListener? = null

    interface OnLoadListener {
        fun onLoadCompleted()
    }

    var loadListener: OnLoadListener? = null

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
        holder.bind(getItem(position), position, onMediaClickedListener, loadListener)
    }

    class ViewHolder(
        private val binding: MediaItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            mediaItem: MediaItem, position: Int,
            mediaClickedListener: OnMediaClickedListener? = null,
            loadListener: OnLoadListener? = null
        ) {

            val imageView = binding.imageView

            // Set the ImageView to the exact size we want. As determined from the screen's current width
            imageView.apply {
                layoutParams.height = mediaItem.desiredDimen
                layoutParams.width = mediaItem.desiredDimen
                requestLayout()
            }

            // Set the transition name for cool animations
            imageView.transitionName = mediaItem.urlFullScreen

            // Set the background to the average color while the image loads
            mediaItem.averageColor?.let { colorAsHex ->
                imageView.setBackgroundColor(Color.parseColor(colorAsHex))
            }

            // Load the Image
            Glide.with(itemView)
                .load(mediaItem.urlPreview)
                .override(mediaItem.desiredDimen)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE) // Cache the decoded version of the image
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        loadListener?.onLoadCompleted()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        loadListener?.onLoadCompleted()
                        return false
                    }

                })
                .into(imageView)

            // Listen for clicks
            mediaClickedListener?.let { listener ->
                imageView.setOnClickListener {
                    listener.onMediaClicked(
                        imageView,
                        mediaItem,
                        position
                    )
                }
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
