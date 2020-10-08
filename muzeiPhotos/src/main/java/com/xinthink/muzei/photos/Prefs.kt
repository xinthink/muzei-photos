package com.xinthink.muzei.photos

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager

/** The minimal interval between download requests, to reduce 429 errors */
const val minDownloadInterval = 900000L

/** Retrieves the default storage for Preferences */
val Context.defaultSharedPrefs: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

/** Retrieves the storage for photos pagination tokens */
val Context.pageTokenPrefs: SharedPreferences get() =
    getSharedPreferences("album_photos_page_tokens", Context.MODE_PRIVATE)

/** Retrieves value of the dark-theme preference */
val Context.darkThemePref: String? get() = defaultSharedPrefs.getString("prefTheme", null)

/** Update dark mode according to user preferences */
fun Context.updateDarkMode(theme: CharSequence?) {
    if (resources == null) return

    AppCompatDelegate.setDefaultNightMode(
        when (theme) {
            "1" -> AppCompatDelegate.MODE_NIGHT_NO
            "2" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }
    )
}

/** Restore saved token */
fun Context.loadSavedToken(): TokenInfo = defaultSharedPrefs.run {
    TokenInfo(
        accessToken = getString("auth_access_token", "") ?: "",
        refreshToken = getString("auth_refresh_token", "") ?: "",
        expiresIn = getLong("auth_token_expires_in", 0),
        mExpiresAt = getLong("auth_token_expires_at", 0)
    )
}

/** Persist the given token */
fun Context.saveToken(token: TokenInfo): TokenInfo {
    defaultSharedPrefs.edit {
        token.computeExpiresAt() // compute expires before persisting
        putString("auth_access_token", token.accessToken)
        putString("auth_refresh_token", token.refreshToken)
        putLong("auth_token_expires_in", token.expiresIn)
        putLong("auth_token_expires_at", token.expiresAt)
    }
    return token
}

/** Retrieves id of the selected album */
val Context.selectedAlbumId: String?
    get() = defaultSharedPrefs.getString("selected_album_id", null)

/** Restore saved data about selected album */
fun Context.loadSelectedAlbum(): Album? =
    defaultSharedPrefs.run {
        val id = getString("selected_album_id", null) ?: return@run null
        Album(
            id = id,
            title = getString("selected_album_title", "") ?: "",
            coverPhotoBaseUrl = getString("selected_album_coverPhotoBaseUrl", "") ?: "",
            mediaItemsCount = getInt("selected_album_mediaItemsCount", -1)
        )
    }

/** Save data about selected album */
fun Context.saveSelectedAlbum(album: Album): Album {
    defaultSharedPrefs.edit {
        putString("selected_album_id", album.id)
        putString("selected_album_title", album.title)
        putString("selected_album_coverPhotoBaseUrl", album.coverPhotoBaseUrl)
        putInt("selected_album_mediaItemsCount", album.mediaItemsCount)
    }
    // clear page token when album switched
    pageTokenPrefs.edit {
        putString("photos_page_token_${album.id}", null)
    }
    lastDownloadTime = 0L // also reset the timestamp to allow new downloads
    return album
}

fun Context.loadPageToken(albumId: String): String? =
    pageTokenPrefs.getString("photos_page_token_$albumId", null)

fun Context.savePageToken(albumId: String, token: String?) =
    pageTokenPrefs.edit {
        putString("photos_page_token_$albumId", token)
}

/** Clear all saved pagination tokens */
fun Context.clearPageTokens() {
    pageTokenPrefs.edit { clear() }
    lastDownloadTime = 0L // also reset the timestamp to allow new downloads
}

/** Clear saved selected album & pagination tokens */
fun Context.clearAlbumAndPageTokens() {
    defaultSharedPrefs.edit {
        remove("selected_album_id")
        remove("selected_album_title")
        remove("selected_album_coverPhotoBaseUrl")
        remove("selected_album_mediaItemsCount")
    }
    clearPageTokens()
}

/** Timestamp of previous download (accessing Google Photos API) */
var Context.lastDownloadTime
    get() = defaultSharedPrefs.getLong("last_download_ts", 0L)
    set(value) = defaultSharedPrefs.edit { putLong("last_download_ts", maxOf(value, 0L)) }
