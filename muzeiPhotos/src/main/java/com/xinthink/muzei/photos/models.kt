package com.xinthink.muzei.photos

import com.squareup.moshi.Json

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
    val isExpired: Boolean get() = accessToken.isEmpty() || refreshToken.isEmpty() ||
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
    val title: String,

    /** The url points to the album in Google Photos that can be a opened by the user. */
    val productUrl: String,

    val coverPhotoBaseUrl: String,
    val coverPhotoMediaItemId: String,
    val mediaItemsCount: Int,

    /** Whether the album is selected, for local use only */
    var isSelected: Boolean = false
) {
    fun setSelected(id: String?): Album {
        isSelected = this.id == id
        return this
    }
}

sealed class AlbumsResult

data class AlbumsPagination(
    val albums: List<Album>,
    val nextPageToken: String
) : AlbumsResult()

data class AlbumsFailure(val error: Throwable) : AlbumsResult()
