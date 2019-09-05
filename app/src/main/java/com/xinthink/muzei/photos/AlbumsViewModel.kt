package com.xinthink.muzei.photos

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.xinthink.muzei.photos.PhotosService.Companion.fetchPhotosAlbums
import com.xinthink.muzei.photos.TokenInfo.Companion.isValid
import com.xinthink.muzei.photos.TokenService.Companion.exchangeAccessToken
import com.xinthink.muzei.photos.TokenService.Companion.loadToken
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.toast

private const val TAG = "MZPAlbVM"

/**
 * ViewModel for Albums screen
 */
class AlbumsViewModel : ViewModel() {
    private var mAccount = MutableLiveData<GoogleSignInAccount>()
    val account: LiveData<GoogleSignInAccount> get() = mAccount

    private var mToken = MutableLiveData<TokenInfo>()
    val token: LiveData<TokenInfo> get() = mToken

    private val mAlbums = MutableLiveData<AlbumsResult>()
    val albums: LiveData<AlbumsResult> get() = mAlbums

    private val mSelectedAlbum = MutableLiveData<Album>()
    val selectedAlbum: LiveData<Album> get() = mSelectedAlbum

    private val mLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = mLoading

    @Volatile
    private var albumsPageToken: String? = null

    /** If we don't have a valid nextPageToken */
    private val hasNoPageToken: Boolean get() = albumsPageToken?.isNotEmpty() != true

    /** Check if authorized or not */
    val isUnauthorized: Boolean get() = account.value == null || !mToken.value.isValid

    fun restoreSavedData(context: Context) {
        loadAuthorization(context)
        loadSelectedAlbumInfo(context)
    }

    /** Restore authorization info */
    fun loadAuthorization(context: Context?) {
        mAccount.value = GoogleSignIn.getLastSignedInAccount(context)
        mToken.value = context?.loadToken()
        if (BuildConfig.DEBUG) Log.d(TAG, "restored token from storage: ${mToken.value}")
    }

    /** Restore saved data about selected album */
    fun loadSelectedAlbumInfo(context: Context) {
        mSelectedAlbum.value = context.defaultSharedPreferences.run {
            val id = getString("selected_album_id", null) ?: return@run null

            Album(
                id = id,
                title = getString("selected_album_title", "") ?: "",
                coverPhotoBaseUrl = getString("selected_album_coverPhotoBaseUrl", "") ?: "",
                mediaItemsCount = getInt("selected_album_mediaItemsCount", -1)
            )
        }
    }

    /** Save data about selected album */
    fun saveSelectedAlbumInfo(context: Context, album: Album) {
        context.defaultSharedPreferences.edit {
            putString("selected_album_id", album.id)
            putString("selected_album_title", album.title)
            putString("selected_album_coverPhotoBaseUrl", album.coverPhotoBaseUrl)
            putInt("selected_album_mediaItemsCount", album.mediaItemsCount)
        }
        context.pageTokenPrefs.edit {
            putString("photos_page_token_${album.id}", null)
        }
        mSelectedAlbum.value = album
    }

    fun handleSignInResult(context: Context, resultCode: Int, completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                context.exchangeAccessToken(account) // Signed in successfully, fetch access token now
                mAccount.value = account
            } else if (resultCode != Activity.RESULT_CANCELED) {
                context.toast("Google Sign-In failed")
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "signInResult:failed code=${e.statusCode}", e)
            if (resultCode != Activity.RESULT_CANCELED) {
                context.toast("Google Sign-In failed: ${e.message}")
            }
        }
    }

    /** fetch access token */
    private fun Context.exchangeAccessToken(account: GoogleSignInAccount) {
        mLoading.postValue(true)
        viewModelScope.launch {
            try {
                val tokenInfo = exchangeAccessToken(account.serverAuthCode ?: "")
                mToken.postValue(tokenInfo)
                if (BuildConfig.DEBUG) Log.d(TAG, "token fetched: $tokenInfo")
            } catch (e: Throwable) {
                toast("Authorization failed: ${e.message}")
                Log.e(TAG, "fetchAccessToken failed", e)
            } finally {
                mLoading.postValue(false)
            }
        }
    }

    /** Fetch Photos albums */
    fun fetchAlbums(
        context: Context,
        isIncremental: Boolean = false,
        force: Boolean = false
    ) {
        if (!force && (mLoading.value == true || (isIncremental && hasNoPageToken))) return

        if (!isIncremental) albumsPageToken = null // clear pagination when refreshing
        mLoading.value = true
        viewModelScope.launch {
            try {
                val pagination = context.fetchPhotosAlbums(pageToken = albumsPageToken)
                albumsPageToken = pagination.nextPageToken
                pagination.isIncremental = isIncremental
                mAlbums.postValue(pagination)
            } catch (e: Throwable) {
                mAlbums.postValue(AlbumsFailure(e))
            } finally {
                mLoading.postValue(false)
            }
        }
    }
}
