package com.andrewkingmarshall.pexels.network.dtos

/**
 * Represents a Pexel Photo.
 *
 * [Official Docs](https://www.pexels.com/api/documentation/#photos-overview)
 *
 * @property id The id of the photo.
 * @property width The real width of the photo in pixels.
 * @property height The real height of the photo in pixels.
 * @property url The Pexels URL where the photo is located.
 * @property photographer The name of the photographer who took the photo.
 * @property photographer_url The URL of the photographer's Pexels profile.
 * @property photographer_id The id of the photographer.
 * @property avg_color The average color of the photo. Useful for a placeholder while the image loads.
 * @property src An assortment of different image sizes that can be used to display this Photo.
 */
data class PexelImageDto(
    val id: Long,
    val width: Int,
    val height: Int,
    val url: String,
    val photographer: String,
    val photographer_url: String,
    val photographer_id: Long,
    val avg_color: String,
    val src: PexelImageSrcDto
)

/**
 * An assortment of different image sizes that can be used to display a [Pexel Photo][PexelImageDto].
 *
 * @property original The image without any size changes. It will be the same as the [width][PexelImageDto.width] and [height][PexelImageDto.height] attributes.
 * @property large2x The image resized W 940px X H 650px DPR 2.
 * @property large The image resized to W 940px X H 650px DPR 1.
 * @property medium The image scaled proportionally so that it's new height is 350px.
 * @property small The image scaled proportionally so that it's new height is 130px.
 * @property portrait The image cropped to W 800px X H 1200px.
 * @property landscape The image cropped to W 1200px X H 627px.
 * @property tiny The image cropped to W 280px X H 200px.
 */
data class PexelImageSrcDto(
    val original: String,
    val large2x: String,
    val large: String,
    val medium: String,
    val small: String,
    val portrait: String,
    val landscape: String,
    val tiny: String,
)
