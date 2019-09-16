package com.xinthink.muzei.photos

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crashlytics.android.Crashlytics
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.xinthink.muzei.photos.worker.BuildConfig
import com.xinthink.widgets.LinearRecyclerOnScrollListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.horizontalPadding
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.toast

/**
 * Main screen of the app, displaying albums in the Photos account
 */
@ExperimentalCoroutinesApi
class AlbumsActivity : AppCompatActivity(), AlbumsAdapter.Callback, CoroutineScope by MainScope() {
    private lateinit var viewModel: AlbumsViewModel
    private lateinit var signInClient: GoogleSignInClient
    private lateinit var albumsAdapter: AlbumsAdapter
    private lateinit var loadingIndicator: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        frameLayout {
            recyclerView {
                horizontalPadding = dip(7)
                initRecyclerView(this)
            }.lparams(matchParent, matchParent)

            loadingIndicator = horizontalProgressBar {
                isIndeterminate = true
                visible = false
            }.lparams(matchParent) {
                topMargin = dip(-7)
                gravity = Gravity.TOP
            }
        }

        initGoogleSignIn()
        viewModel = ViewModelProvider(this)[AlbumsViewModel::class.java]
        observeViewModel()
        viewModel.restoreSavedData(this)
    }

    private fun initGoogleSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(BuildConfig.AUTH_CLIENT_ID, true)
            .requestScopes(Scope(BuildConfig.AUTH_SCOPE_PHOTOS))
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        signInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            viewModel.handleSignInResult(this, resultCode, task)
        }
    }

    override fun onAlbumSelectionChanged(album: Album) = viewModel.saveSelectedAlbumInfo(this, album)

    override fun onClickAuthorize() = startActivityForResult(signInClient.signInIntent, RC_SIGN_IN)

    override fun isUnauthorized(): Boolean = viewModel.isUnauthorized

    override fun refreshAlbums() = onTokenUpdated.onChanged(null)

    private fun initRecyclerView(rv: RecyclerView) {
        albumsAdapter = AlbumsAdapter(this)
        val lm = GridLayoutManager(this, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) = when (position) {
                    0 -> 2
                    else -> 1
                }
            }
        }
        rv.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val dip: (Int) -> Int = view::dip
                val count = albumsAdapter.itemCount - 1
                val position = parent.getChildAdapterPosition(view)
                val atBottom = position >= count - 2
                outRect.set(dip(7), dip(7), dip(7), if (atBottom) dip(28) else dip(14))
            }
        })
        rv.layoutManager = lm
        rv.adapter = albumsAdapter

        rv.addOnScrollListener(LinearRecyclerOnScrollListener(lm, ::loadMore))
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this, Observer {
            loadingIndicator.visible = it
            albumsAdapter.refreshSummary(it)
        })
        viewModel.account.observe(this, onAccountUpdated)
        viewModel.selectedAlbum.observe(this, onSelectedAlbumUpdated)
        viewModel.token.observe(this, onTokenUpdated)
        viewModel.albums.observe(this, onAlbumsResult)
    }

    private val onAccountUpdated = Observer<GoogleSignInAccount?> {
        albumsAdapter.account = it
    }

    private val onSelectedAlbumUpdated = Observer<Album?> {
        albumsAdapter.selectedAlbum = it
        if (it != null) setResult(RESULT_OK)
    }

    private val onTokenUpdated = Observer<TokenInfo?> {
        if (isUnauthorized()) albumsAdapter.clearAlbums()
        else viewModel.fetchAlbums(this, force = true)
    }

    private val onAlbumsResult = Observer<AlbumsResult?> {
        try {
            when (it) {
                is AlbumsPagination -> {
                    val handler = if (it.isIncremental) albumsAdapter::appendAlbums
                    else albumsAdapter::resetAlbums
                    handler(it.albums)
                    updateSelectedAlbumInfo(it)
                }
                is AlbumsFailure -> toast("Fetching albums failed: ${it.error}")
                else -> Unit
            }
        } catch (e: Exception) {
            // FIXME crashes workaround: https://is.gd/VomkBY https://is.gd/3OWuSZ
            Crashlytics.logException(e)
        }
    }

    private fun loadMore() {
        if (!isUnauthorized()) loadingIndicator.post {
            // run following code outside the scroll callback
            viewModel.fetchAlbums(this, isIncremental = true)
        }
    }

    /** update data of current selected album */
    private fun updateSelectedAlbumInfo(it: AlbumsPagination) {
        val selected = viewModel.selectedAlbum.value
        if (selected != null) {
            val a = it.albums.find { a -> a.id == selected.id }
            if (a?.isSummaryUpdated(selected) == true) viewModel.saveSelectedAlbumInfo(this, a)
        }
    }

    companion object {
        private const val TAG = "MZPAlb"
        private const val RC_SIGN_IN = 1
    }
}
