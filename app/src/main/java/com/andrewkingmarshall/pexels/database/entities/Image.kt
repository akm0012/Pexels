package com.andrewkingmarshall.pexels.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.andrewkingmarshall.pexels.network.dtos.PexelImageDto
import com.andrewkingmarshall.pexels.util.getCurrentTimeInSec

/**
 * Represents a Pexel Photo.
 *
 * @property imageId The id of the photo.
 * @property width The real width of the photo in pixels.
 * @property height The real height of the photo in pixels.
 * @property avgColor The average color of the photo. Useful for a placeholder while the image loads.
 * @property fullSizeUrl The image without any size changes. It will be the same as the [width][Image.width] and [height][Image.height] attributes.
 * @property large2xUrl The image resized W 940px X H 650px DPR 2.
 * @property largeUrl The image resized to W 940px X H 650px DPR 1.
 * @property mediumUrl The image scaled proportionally so that it's new height is 350px.
 * @property smallUrl The image scaled proportionally so that it's new height is 130px.
 * @property tinyUrl The image cropped to W 280px X H 200px.
 * @property dateAdded The date this image was added. Represented in seconds that have elapsed since the Unix epoch.
 */
@Entity
data class Image(
    @PrimaryKey val imageId: Long,
    val width: Int,
    val height: Int,
    val avgColor: String,
    val fullSizeUrl: String,
    val large2xUrl: String,
    val largeUrl: String,
    val mediumUrl: String,
    val smallUrl: String,
    val tinyUrl: String,
    val dateAdded: Long,
    val serverOrder: Int,
) {
    constructor(imageDto: PexelImageDto, serverOrder: Int) : this(
        imageId = imageDto.id,
        width = imageDto.width,
        height = imageDto.height,
        avgColor = imageDto.avg_color,
        fullSizeUrl = imageDto.src.original,
        large2xUrl = imageDto.src.large2x,
        largeUrl = imageDto.src.large,
        mediumUrl = imageDto.src.medium,
        smallUrl = imageDto.src.small,
        tinyUrl = imageDto.src.tiny,
        dateAdded = getCurrentTimeInSec(),
        serverOrder = serverOrder
    )
}

const val IMAGE_PRIMARY_KEY = "imageId"
