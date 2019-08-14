package com.xinthink.muzei.photos

import android.animation.ObjectAnimator
import android.content.Context
import android.util.Property
import android.view.Gravity
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
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.BOTTOM
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.END
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.START
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.TOP
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.horizontalPadding
import org.jetbrains.anko.imageView
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk19.listeners.onClick
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding

/**
 * Adapter for albums list
 */
class AlbumsAdapter(
    private var selectedAlbumId: String? = null,
    private val albumSelectionListener: (album: Album) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
        else -> AlbumRenderer.create(parent.context, ::onSelectAlbum)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is AccountRenderer -> holder.render(account)
        is AlbumRenderer -> holder.render(mAlbums[position - 1].setSelected(selectedAlbumId))
        else -> Unit
    }

    // override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
    //     super.onBindViewHolder(holder, position, payloads)
    // }

    private fun onSelectAlbum(album: Album, position: Int) {
        selectedAlbumId = album.id
        val index = mAlbums.indexOfFirst { it.id != album.id && it.isSelected }
        if (index > -1) {
            mAlbums[index].isSelected = false
            notifyItemChanged(index + 1, true)
        }

        album.isSelected = true
        notifyItemChanged(position, false)
        albumSelectionListener(album)
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

        val size = dip(avatarSize)
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
    val coverHolder: View,
    // val coverBg: View,
    val icCheck: View,
    val txtName: TextView,
    val txtCount: TextView,
    val onClick: (album: Album, position: Int) -> Unit
): RecyclerView.ViewHolder(itemView) {
    private val context = itemView.context

    private val paddingProp = object : Property<View, Int>(Int::class.java, "padding") {
        override fun get(v: View?): Int = v?.paddingTop ?: 0

        override fun set(v: View?, value: Int?) {
            v?.padding = value ?: 0
        }
    }

    fun render(album: Album) {
        val imgSize = context.coverImageSize()
        Picasso.get()
            .load("${album.coverPhotoBaseUrl}=w$imgSize-h$imgSize")
            .resize(imgSize, imgSize)
            .centerCrop()
            .transform(RoundedCornersTransformation(dip(6), 0))
            .into(imgCover)
        txtName.text = album.title
        txtCount.text = context.getString(R.string.items_in_album, album.mediaItemsCount)

        itemView.onClick {
            if (!album.isSelected) {
                onClick(album, adapterPosition)
            }
        }

        val bgVisible = coverHolder.background != null
        if (album.isSelected) {
            coverHolder.backgroundResource = R.drawable.album_cover_bg
            ObjectAnimator.ofInt(coverHolder, paddingProp, 0, dip(16))
                .setDuration(long(android.R.integer.config_shortAnimTime))
                .doOnEnd { icCheck.visible = true }
                .start()
        } else if (bgVisible) {
            coverHolder.background = null
            ObjectAnimator.ofInt(coverHolder, paddingProp, dip(16), 0)
                .setDuration(long(android.R.integer.config_shortAnimTime))
                .doOnStart { icCheck.visible = false }
                .start()
        }
    }

    companion object {
        private var coverImageSize = 0

        /** Instantiate an [AlbumRenderer] */
        fun create(
            context: Context,
            onClick: (album: Album, position: Int
        ) -> Unit): AlbumRenderer {
            // lateinit var coverBg: View
            lateinit var coverHolder: View
            lateinit var imgCover: ImageView
            lateinit var icCheck: View
            lateinit var txtName: TextView
            lateinit var txtCount: TextView
            val v = context.UI {
                constraintLayout {
                    bottomPadding = dip(8)

                    // coverBg = view {
                    //     id = R.id.album_cover_bg
                    //     background = null
                    // }.lparams(matchConstraint, matchConstraint)

                    val cover = albumCover()
                    coverHolder = cover.first
                    imgCover = cover.second

                    icCheck = checkIcon()

                    txtName = albumName()

                    txtCount = albumSummary()

                    applyConstraintSet {
                        // coverBg {
                        //     connect(
                        //         START to START of PARENT_ID,
                        //         END to END of PARENT_ID,
                        //         TOP to TOP of PARENT_ID
                        //     )
                        //     dimensionRation = "1"
                        // }
                        coverHolder {
                            // connect(
                            //     START to START of coverBg,
                            //     END to END of coverBg,
                            //     TOP to TOP of coverBg,
                            //     BOTTOM to BOTTOM of coverBg
                            // )
                            connect(
                                START to START of PARENT_ID,
                                END to END of PARENT_ID,
                                TOP to TOP of PARENT_ID
                            )
                            dimensionRation = "1"
                        }
                        // imgCover {
                        //     connect(
                        //         START to START of coverHolder,
                        //         END to END of coverHolder,
                        //         TOP to TOP of coverHolder,
                        //         BOTTOM to BOTTOM of coverHolder
                        //     )
                        // }
                        icCheck {
                            connect(
                                START to START of coverHolder margin dip(4),
                                TOP to TOP of coverHolder margin dip(4)
                            )
                        }
                        txtName {
                            connect(
                                TOP to BOTTOM of coverHolder margin dip(6),
                                START to START of coverHolder,
                                END to END of coverHolder
                            )
                        }
                        txtCount {
                            connect(
                                TOP to BOTTOM of txtName margin dip(4),
                                START to START of coverHolder,
                                END to END of coverHolder
                            )
                        }
                    }
                }
            }.view
            return AlbumRenderer(v, imgCover, coverHolder, icCheck, txtName, txtCount, onClick)
        }

        private fun _ConstraintLayout.albumName(): TextView {
            return textView {
                id = R.id.txt_album_title
                maxLines = 2
                ellipsizeEnd()
                textColorResource = R.color.primaryTextColor
                textSize = 14f
            }.lparams(matchConstraint)
        }

        private fun _ConstraintLayout.albumSummary(): TextView {
            return textView {
                id = R.id.txt_album_items
                maxLines = 1
                ellipsizeEnd()
                textColorResource = R.color.secondaryTextColor
                textSize = 12f
            }.lparams(matchConstraint)
        }

        private fun _ConstraintLayout.albumCover(): Pair<View, ImageView> {
            lateinit var coverHolder: View
            lateinit var imgCover: ImageView
            coverHolder = frameLayout {
                id = R.id.album_cover_holder
                background = null

                imgCover = imageView {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    adjustViewBounds = false
                }.lparams(matchParent, matchParent)
            }.lparams(matchConstraint, matchConstraint)
            return coverHolder to imgCover
        }

        private fun _ConstraintLayout.checkIcon() = frameLayout {
            id = R.id.ic_check
            visible = false
            imageView(R.drawable.circle_white)
                .lparams(dip(20), dip(20)) {
                    gravity = Gravity.CENTER
                }
            imageViewCompat(R.drawable.ic_check_circle_black_24dp) {
                setTintCompat(R.color.primaryDarkColor)
            }.lparams(dip(24), dip(24))
        }
    }

    fun Context.coverImageSize(): Int {
        if (coverImageSize <= 0) coverImageSize = screenWidth / 2
        return coverImageSize
    }
}
