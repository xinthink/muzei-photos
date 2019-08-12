package com.xinthink.muzei.photos

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.xinthink.muzei.photos.PhotosService.Companion.fetchPhotosAlbums
import com.xinthink.muzei.photos.TokenInfo.Companion.isValid
import com.xinthink.muzei.photos.TokenService.Companion.loadToken
import kotlinx.coroutines.launch

class AlbumsViewModel: ViewModel() {
    private var mAccount: GoogleSignInAccount? = null
    val account: GoogleSignInAccount? get() = mAccount

    private var mToken: TokenInfo? = null

    private val mAlbums = MutableLiveData<AlbumsResult>()
    val albums: LiveData<AlbumsResult> get() = mAlbums

    private val mLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = mLoading

    @Volatile
    private var albumsPageToken: String? = null

    /** Check if authorized or not */
    fun isUnauthorized(context: Context?): Boolean {
        loadAuthorization(context)
        return account == null || !mToken.isValid
    }

    /** Restore the authorization info */
    fun loadAuthorization(context: Context?) {
        mAccount = GoogleSignIn.getLastSignedInAccount(context)
        mToken = context?.loadToken()
        Log.d(AlbumsFragment.TAG, "restored token from storage: $mToken")
        context?.loadToken()
    }

    /** Fetch Photos albums */
    fun fetchAlbums(context: Context, isIncremental: Boolean = false) {
        if (!isIncremental) albumsPageToken = null // clear pagination when refreshing
        mLoading.postValue(true)
        viewModelScope.launch {
            try {
                val pagination = context.fetchPhotosAlbums(albumsPageToken)
                albumsPageToken = pagination.nextPageToken
                mAlbums.postValue(pagination)
            } catch (e: Throwable) {
                mAlbums.postValue(AlbumsFailure(e))
            } finally {
                mLoading.postValue(false)
            }
        }
    }
}
