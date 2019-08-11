package com.xinthink.muzei.photos

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.xinthink.muzei.photos.TokenService.Companion.loadToken
import kotlinx.coroutines.launch

class AlbumsViewModel: ViewModel() {
    private var mAccount: GoogleSignInAccount? = null
    val account: GoogleSignInAccount? get() = mAccount

    private var mToken: TokenInfo? = null
    val token: TokenInfo? get() = mToken
    
    private val mAlbums = MutableLiveData<AlbumsResult>()
    val albums: LiveData<AlbumsResult> get() = mAlbums

    /** Restore the authorization info */
    fun loadAuthorization(context: Context?) {
        mAccount = GoogleSignIn.getLastSignedInAccount(context)
        mToken = context?.loadToken()
    }

    /** Fetch Photos albums */
    fun fetchAlbums() {
        try {
            viewModelScope.launch {
                val pagination = PhotosService.create().albums()
                mAlbums.postValue(pagination)
            }
        } catch (e: Throwable) {
            mAlbums.postValue(AlbumsFailure(e))
        }
    }
}
