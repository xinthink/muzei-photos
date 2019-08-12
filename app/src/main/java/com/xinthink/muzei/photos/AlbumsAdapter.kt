package com.xinthink.muzei.photos

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import org.jetbrains.anko.UI
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.BOTTOM
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.END
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.START
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.TOP
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalPadding
import org.jetbrains.anko.imageView
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding

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

    override fun getItemId(position: Int) = when (position) {
        0 -> 0
        else -> mAlbums[position - 1].id.hashCode()
    }.toLong()

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
    val imgAvatar: ImageView
): RecyclerView.ViewHolder(itemView) {

    fun render(account: GoogleSignInAccount?) {
        if (account == null) return

        val size = itemView.dip(avatarSize)
        Picasso.get()
            .load("${account.photoUrl}")
            .resize(size, size)
            .centerCrop()
            .transform(CropCircleTransformation())
            .into(imgAvatar)
    }

    companion object {
        private const val avatarSize = 48

        /** Instantiate an [AccountRenderer] */
        fun create(context: Context): AccountRenderer {
            lateinit var imgAvatar: ImageView
            val v = context.UI {
                constraintLayout {
                    topPadding = dip(24)
                    bottomPadding = dip(12)
                    horizontalPadding = dip(20)

                    imgAvatar = imageView {
                        id = R.id.img_avatar
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        adjustViewBounds = false
                    }.lparams(dip(avatarSize), dip(avatarSize))

                    applyConstraintSet {
                        imgAvatar {
                            connect(
                                START to START of PARENT_ID,
                                END to END of PARENT_ID,
                                TOP to TOP of PARENT_ID,
                                BOTTOM to BOTTOM of PARENT_ID
                            )
                        }
                    }
                }
            }.view
            return AccountRenderer(v, imgAvatar)
        }
    }
}

/**
 * Album (thumbnail) renderer
 */
private class AlbumRenderer(
    itemView: View,
    val imgCover: ImageView,
    val txtName: TextView,
    val txtCount: TextView
): RecyclerView.ViewHolder(itemView) {
    private val context = itemView.context

    fun render(album: Album) {
        val size = context.coverImageSize()
        Picasso.get()
            .load("${album.coverPhotoBaseUrl}=w$size-h$size")
            .resize(size, size)
            .centerCrop()
            .transform(RoundedCornersTransformation(itemView.dip(6), 0))
            .into(imgCover)
        txtName.text = album.title
        txtCount.text = context.getString(R.string.items_in_album, album.mediaItemsCount)
    }

    companion object {
        private var coverImageSize = 0

        /** Instantiate an [AlbumRenderer] */
        fun create(context: Context): AlbumRenderer {
            lateinit var imgCover: ImageView
            lateinit var txtName: TextView
            lateinit var txtCount: TextView
            val v = context.UI {
                constraintLayout {
                    bottomPadding = dip(8)

                    imgCover = imageView {
                        id = R.id.img_album_cover
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        adjustViewBounds = false
                    }.lparams(matchConstraint, matchConstraint)

                    txtName = textView {
                        id = R.id.txt_album_title
                        maxLines = 2
                        ellipsize = TextUtils.TruncateAt.END
                        textColorResource = R.color.primaryTextColor
                        textSize = 14f
                    }.lparams(matchConstraint)

                    txtCount = textView {
                        id = R.id.txt_album_items
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                        textColorResource = R.color.primaryTextColor
                        textSize = 12f
                    }.lparams(matchConstraint)

                    applyConstraintSet {
                        imgCover {
                            connect(
                                START to START of PARENT_ID,
                                END to END of PARENT_ID,
                                TOP to TOP of PARENT_ID
                            )
                            dimensionRation = "1"
                        }
                        txtName {
                            connect(
                                TOP to BOTTOM of imgCover margin dip(6),
                                START to START of imgCover,
                                END to END of imgCover
                            )
                        }
                        txtCount {
                            connect(
                                TOP to BOTTOM of txtName margin dip(4),
                                START to START of imgCover,
                                END to END of imgCover
                            )
                        }
                    }
                }
            }.view
            return AlbumRenderer(v, imgCover, txtName, txtCount)
        }
    }

    fun Context.coverImageSize(): Int {
        if (coverImageSize <= 0) {
            coverImageSize = resources.displayMetrics.widthPixels / 2
        }
        return coverImageSize
    }
}
