package com.xinthink.muzei.photos

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xinthink.muzei.photos.TokenInfo.Companion.isValid
import org.jetbrains.anko.UI
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.toast

/**
 * Main screen of the app, displaying albums in the Photos account
 */
class AlbumsFragment : Fragment() {
    private lateinit var viewModel: AlbumsViewModel
    private lateinit var navController: NavController
    private lateinit var albumsAdapter: AlbumsAdapter

    private val account get() = viewModel.account
    private val token get() = viewModel.token

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = NavHostFragment.findNavController(this)
        viewModel = ViewModelProvider(this)[AlbumsViewModel::class.java]
        viewModel.albums.observe(this, onAlbumsResult)

        viewModel.loadAuthorization(context)
        if (account == null) navController.navigate(R.id.action_auth) // sign-in first
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "--- MainFragment started")
        viewModel.loadAuthorization(context)
        render()
        renderAlbums()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = context?.UI {
        recyclerView {
            initRecyclerView(this)
        }
    }?.view

    private fun initRecyclerView(rv: RecyclerView) {
        albumsAdapter = AlbumsAdapter()
        rv.layoutManager = GridLayoutManager(context, 2).apply {
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
                outRect.set(4, 4, 4, 4)
            }
        })
        rv.adapter = albumsAdapter
    }

    private fun render() {
        val account = this.account ?: return renderSignedOut()

        // if signed-in, show account info no matter the token is valid or not
//        txtName.text = account.displayName
        albumsAdapter.account = account

        // TODO different content if token is invalid or expired
//        txtAuthCode.text = token?.authCode
//        txtToken.text = token?.authCode
    }

    private fun renderSignedOut() {
        albumsAdapter.account = null
//        txtName.text = null
//        txtAuthCode.text = null
//        txtToken.text = null
    }

    private fun renderAlbums() {
        if (token.isValid) viewModel.fetchAlbums()
        else albumsAdapter.clearAlbums()
    }

    private val onAlbumsResult = Observer<AlbumsResult?> {
        when (it) {
            is AlbumsPagination -> albumsAdapter.resetAlbums(it.albums)
            is AlbumsFailure -> context?.toast("Fetching albums failed: ${it.error}")
            else -> Unit
        }
    }

    companion object {
        private const val TAG = "MAIN"
    }
}
