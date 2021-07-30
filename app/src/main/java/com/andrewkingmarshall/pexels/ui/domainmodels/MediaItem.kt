package com.andrewkingmarshall.pexels.ui.domainmodels

/**
 * A Domain Model that represents a single media item.
 *
 * @property urlPreview The Url to use when loading a preview image.
 * @property urlFullScreen The Url used when loading a full screen image.
 * @property averageColor The average color of the image. Useful when loading. I.E. "#977B6C"
 */
data class MediaItem(
    val urlPreview: String,
    val urlFullScreen: String,
    val averageColor: String?,
)
