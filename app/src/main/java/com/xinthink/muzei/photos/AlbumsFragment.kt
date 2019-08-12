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
import org.jetbrains.anko.UI
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalPadding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = NavHostFragment.findNavController(this)
        viewModel = ViewModelProvider(this)[AlbumsViewModel::class.java]
        viewModel.albums.observe(this, onAlbumsResult)

        if (viewModel.isUnauthorized(context)) navController.navigate(R.id.action_auth) // sign-in first
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "AlbumsFragment started, check authorization again")
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
            horizontalPadding = dip(7)
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
                val dip: (Int) -> Int = view::dip
                val count = albumsAdapter.itemCount - 1
                val position = parent.getChildAdapterPosition(view)
                val atBottom = position >= count - 2
                outRect.set(dip(7), dip(7), dip(7), if (atBottom) dip(28) else dip(14))
            }
        })
        rv.adapter = albumsAdapter
    }

    private fun render() {
        val account = this.account ?: return renderSignedOut()

        // if signed-in, show account summary
        albumsAdapter.account = account

        // TODO different content if token is invalid or expired
    }

    private fun renderSignedOut() {
        albumsAdapter.account = null
    }

    private fun renderAlbums() {
        val ctx = context
        if (ctx == null || viewModel.isUnauthorized(ctx)) albumsAdapter.clearAlbums()
        else viewModel.fetchAlbums(ctx)
    }

    private val onAlbumsResult = Observer<AlbumsResult?> {
        when (it) {
            is AlbumsPagination -> albumsAdapter.resetAlbums(it.albums)
            is AlbumsFailure -> context?.toast("Fetching albums failed: ${it.error}")
            else -> Unit
        }
    }

    companion object {
        const val TAG = "ALBUMS"
    }
}
