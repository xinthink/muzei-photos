package com.xinthink.muzei.photos

import android.annotation.SuppressLint
import com.squareup.moshi.Json
import java.text.DateFormat
import java.text.SimpleDateFormat

/** Google Photos authorization info */
data class TokenInfo(
    @field:Json(name = "access_token")
    val accessToken: String = "",

    @field:Json(name = "refresh_token")
    val refreshToken: String = "",

    @field:Json(name = "expires_in")
    val expiresIn: Long = 0,

    @field:Json(name = "token_type")
    val tokenType: String = "",

    /** Timestamp when the access token expires, a derived property */
    private var mExpiresAt: Long = 0
) {
    /** Whether the token is expired (at least 10 seconds until it expires) */
    val isExpired: Boolean
        get() = accessToken.isEmpty() || refreshToken.isEmpty() ||
            (expiresAt - System.currentTimeMillis()) < 10000L

    /** Timestamp when the access token expires */
    val expiresAt: Long get() = mExpiresAt

    /** Compute and update [expiresAt] */
    fun computeExpiresAt() {
        mExpiresAt = System.currentTimeMillis() + expiresIn * 1000L
    }

    companion object {
        /** Whether the access token is valid (is authorized) */
        val TokenInfo?.isValid: Boolean
            get() = this != null && accessToken.isNotEmpty() && refreshToken.isNotEmpty()
    }
}

data class Album(
    val id: String,
    val title: String = "",

    /** The url points to the album in Google Photos that can be a opened by the user. */
    val productUrl: String = "",

    val coverPhotoBaseUrl: String = "",
    val coverPhotoMediaItemId: String = "",
    val mediaItemsCount: Int = -1,

    /** Whether the album is selected, for local use only */
    var isSelected: Boolean = false
) {
    fun setSelected(id: String?): Album {
        isSelected = this.id == id
        return this
    }

    fun isSummaryUpdated(a: Album): Boolean = title != a.title ||
        coverPhotoBaseUrl != a.coverPhotoBaseUrl || mediaItemsCount != a.mediaItemsCount
}

sealed class AlbumsResult

data class AlbumsPagination(
    val albums: List<Album>,
    val nextPageToken: String
) : AlbumsResult()

data class AlbumsFailure(val error: Throwable) : AlbumsResult()

/**
 * Searching result in a user's Google Photos library.
 */
data class MediaItemsPagination(
    /** List of media items that match the search parameters. */
    val mediaItems: List<MediaItem>,

    /** Use this token to get the next set of media items. Its presence is the only reliable indicator of more media items being available in the next request. */
    val nextPageToken: String
)

/**
 * Representation of a media item (such as a photo or video) in Google Photos.
 */
data class MediaItem(
    val id: String,

    /** Description of the media item. This is shown to the user in the item's info section in the Google Photos app. */
    val description: String?,

    /** The url points to the album in Google Photos that can be a opened by the user. */
    val productUrl: String,

    /**
     * A URL to the media item's bytes.
     * This shouldn't be used as is. Parameters should be appended to this URL before use.
     * See the developer documentation for a complete list of supported parameters.
     * For example, '=w2048-h1024' will set the dimensions of a media item of type photo to have a width of 2048 px and height of 1024 px.
     */
    val baseUrl: String,

    /** MIME type of the media item. For example, image/jpeg. */
    val mimeType: String,

    /** Metadata related to the media item, such as, height, width, or creation time. */
    val mediaMetadata: MediaMetadata,

    /** Information about the user who created this media item. */
    val contributorInfo: Contributor?,

    /**
     * Filename of the media item. This is shown to the user in the item's info section in the Google Photos app.
     */
    val filename: String
)

/**
 * Metadata for a media item.
 */
data class MediaMetadata(
    /**
     * Time when the media item was first created (not when it was uploaded to Google Photos).
     * A timestamp in RFC3339 UTC "Zulu" format, accurate to nanoseconds. Example: "2014-10-02T15:01:23.045123456Z".
     */
    val creationTime: String,
    val width: String,
    val height: String
) {
    fun formattedCreationTime(): String {
        return try {
            val parser = creationTimeParser.get() ?: return ""
            val fmt = creationTimeFmt.get() ?: return ""
            fmt.format(parser.parse(creationTime))
        } catch (e: Throwable) {
            e.printStackTrace()
            ""
        }
    }

    companion object {
        private val creationTimeParser = object : ThreadLocal<DateFormat>() {
            @SuppressLint("SimpleDateFormat")
            override fun initialValue(): DateFormat? =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        }
        private val creationTimeFmt = object : ThreadLocal<DateFormat>() {
            override fun initialValue(): DateFormat? =
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        }
    }
}

/**
 * Information about the user who added the media item. Note that this information is included only if the media item is within a shared album created by your app and you have the sharing scope.
 */
data class Contributor(
    val profilePictureBaseUrl: String,
    val displayName: String
)
