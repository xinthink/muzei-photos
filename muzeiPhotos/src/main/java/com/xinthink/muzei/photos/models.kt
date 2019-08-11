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

    // for local storage only
    val authCode: String = ""
) {
    companion object {
        /** Whether this token is valid (is authorized) TODO check refresh_token & expires_in */
        val TokenInfo?.isValid: Boolean get() =
            this != null && authCode.isNotEmpty() && accessToken.isNotEmpty()
    }
}

data class Album(
    val id: String,
    val title: String,

    /** The url points to the album in Google Photos that can be a opened by the user. */
    val productUrl: String,

    val coverPhotoBaseUrl: String,
    val coverPhotoMediaItemId: String,
    val mediaItemsCount: Int
)

sealed class AlbumsResult

data class AlbumsPagination(
    val albums: List<Album>,
    val nextPageToken: String
) : AlbumsResult()

data class AlbumsFailure(val error: Throwable) : AlbumsResult()
