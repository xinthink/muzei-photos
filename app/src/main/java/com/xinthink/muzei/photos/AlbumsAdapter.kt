package com.xinthink.muzei.photos

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import org.jetbrains.anko.UI
import org.jetbrains.anko.dip
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.space
import org.jetbrains.anko.textView

/**
 * Adapter for albums list
 */
class AlbumsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mAlbums = mutableListOf<Album>()

    init {
        setHasStableIds(true)
    }

    /** Update the signed-in account */
    var account: GoogleSignInAccount? = null
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    /** Reset the whole albums list */
    fun resetAlbums(albums: List<Album>?) {
        mAlbums.clear()
        if (albums != null) mAlbums.addAll(albums)
        notifyDataSetChanged()
    }

    /** Remove all the albums */
    fun clearAlbums() {
        val size = mAlbums.size
        mAlbums.clear()
        notifyItemRangeRemoved(1, size)
    }

    override fun getItemCount() = mAlbums.size + 1

    override fun getItemViewType(position: Int) = when (position) {
        0 -> TYPE_ACCOUNT
        else -> TYPE_ALBUM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_ACCOUNT -> AccountRenderer.create(parent.context)
        else -> AlbumRenderer.create(parent.context)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is AccountRenderer -> holder.render(account)
        is AlbumRenderer -> holder.render(mAlbums[position - 1])
        else -> Unit
    }

    companion object {
        private const val TYPE_ALBUM = 0
        private const val TYPE_ACCOUNT = 1
    }
}

/**
 * Account (summary) renderer
 */
private class AccountRenderer(
    itemView: View,
    val txtName: TextView
): RecyclerView.ViewHolder(itemView) {

    fun render(account: GoogleSignInAccount?) {
        txtName.text = account?.displayName
    }

    companion object {
        /** Instantiate an [AccountRenderer] */
        fun create(context: Context): AccountRenderer {
            lateinit var txtName: TextView
            val v = context.UI {
                linearLayout {
                    textView("Name:")
                    space().lparams(width = dip(12))
                    txtName = textView()
                }
            }.view
            return AccountRenderer(v, txtName)
        }
    }
}

/**
 * Album (thumbnail) renderer
 */
private class AlbumRenderer(
    itemView: View,
    val txtName: TextView
): RecyclerView.ViewHolder(itemView) {

    fun render(album: Album) {
        txtName.text = album.title
    }

    companion object {
        /** Instantiate an [AlbumRenderer] */
        fun create(context: Context): AlbumRenderer {
            lateinit var txtName: TextView
            val v = context.UI {
                linearLayout {
                    textView("Name:")
                    space().lparams(width = dip(12))
                    txtName = textView()
                }
            }.view
            return AlbumRenderer(v, txtName)
        }
    }
}
