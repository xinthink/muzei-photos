package com.xinthink.muzei.photos

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
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
import org.jetbrains.anko._FrameLayout
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.cardview.v7.cardView
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
import org.jetbrains.anko.imageView
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk19.listeners.onClick
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.themedButton
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout

/**
 * Adapter for albums list
 */
class AlbumsAdapter(
    private val callback: Callback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mAlbums = mutableListOf<Album>()

    init {
        setHasStableIds(true)
    }

    var selectedAlbum: Album? = null
        set(value) {
            field = value
            notifyItemChanged(0, value)
        }

    /** Update the signed-in account */
    var account: GoogleSignInAccount? = null
        set(value) {
            field = value
            notifyItemChanged(0, value)
        }

    /** Refresh summary section */
    fun refreshSummary(isLoading: Boolean) {
        notifyItemChanged(0, SummaryInfo(isLoading))
    }

    /** Reset the whole albums list */
    fun resetAlbums(albums: List<Album>?) {
        mAlbums.clear()
        if (albums != null) mAlbums.addAll(albums)
        notifyDataSetChanged()
    }

    /** Append albums to the list */
    fun appendAlbums(albums: List<Album>?) {
        val prevSize = mAlbums.size
        if (!albums.isNullOrEmpty()) {
            mAlbums.addAll(albums)
            notifyItemRangeInserted(prevSize + 1, albums.size)
        }
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
        TYPE_ACCOUNT -> SummaryRenderer.create(parent.context, callback)
        else -> AlbumRenderer.create(parent.context, ::onSelectAlbum)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is SummaryRenderer -> holder.render(account, selectedAlbum)
        is AlbumRenderer -> holder.render(mAlbums[position - 1].setSelected(selectedAlbum?.id))
        else -> Unit
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (BuildConfig.DEBUG) Log.v(TAG, "onBindViewHolder position=$position payloads=$payloads")
        super.onBindViewHolder(holder, position, payloads)
    }

    private fun onSelectAlbum(album: Album, position: Int) {
        val index = mAlbums.indexOfFirst { it.id != album.id && it.isSelected }
        if (index > -1) {
            mAlbums[index].isSelected = false
            notifyItemChanged(index + 1, true)
        }

        album.isSelected = true
        notifyItemChanged(position, false)
        callback.onAlbumSelectionChanged(album)
    }

    companion object {
        const val TAG = "MZPAlbApt"
        private const val TYPE_ALBUM = 0
        private const val TYPE_ACCOUNT = 1
    }

    /**
     * Interface definition for interaction with the caller
     */
    interface Callback {
        /** If the access to albums is not yet granted */
        fun isUnauthorized(): Boolean

        /** Request to start the authorization procedure */
        fun onClickAuthorize()

        /** Called when the selected album changed */
        fun onAlbumSelectionChanged(album: Album)

        /** Refresh album list */
        fun refreshAlbums()

        /** Go to the preferences screen */
        fun openPreferences()
    }
}

data class SummaryInfo(
    val isLoading: Boolean
)

const val summaryAlbumSize = 80
const val avatarSize = 48

/**
 * Summary header renderer
 */
private class SummaryRenderer(
    val ui: SummaryUI,
    val callback: AlbumsAdapter.Callback
) : RecyclerView.ViewHolder(ui.rootView) {

    fun render(account: GoogleSignInAccount?, album: Album?) {
        val unAuth = account == null || callback.isUnauthorized()
        ui.authView.visible = unAuth
        ui.summaryView.visible = !unAuth
        if (unAuth) {
            ui.btnAuth.onClick { callback.onClickAuthorize() }
            return
        }

        renderAlbum(album)
        renderAccount(account!!) // account is non-null here
        ui.icRefresh.onClick {
            callback.refreshAlbums()
        }
        ui.icPrefs.onClick {
            callback.openPreferences()
        }
    }

    private fun renderAccount(account: GoogleSignInAccount) {
        val size = dip(avatarSize)
        Picasso.get()
            .load("${account.photoUrl}")
            .placeholder(R.drawable.avatar_placeholder)
            .resize(size, size)
            .centerCrop()
            .transform(CropCircleTransformation())
            .into(ui.imgAvatar)
    }

    private fun renderAlbum(album: Album?) {
        val picUrl = album?.coverPhotoBaseUrl
        if (picUrl != null) {
            Picasso.get()
                .load("${album.coverPhotoBaseUrl}=s${ui.albumSizePx}-p-no") // make it a square and no play button for video thumbnail
                .placeholder(R.drawable.album_cover_placeholder)
                // .resize(ui.albumSizePx, ui.albumSizePx)
                // .centerCrop()
                .into(ui.imgCover)
            ui.imgCover.backgroundColor = Color.TRANSPARENT
            ui.imgTag.setTintCompat(R.color.mask_black_30)
            ui.imgStar.setTintCompat(R.color.golden)
        } else {
            ui.imgCover.backgroundResource = R.drawable.selected_album_placeholder_border
            ui.imgTag.setTintCompat(R.color.accentColor_white50)
            ui.imgStar.setTintCompat(R.color.star_placeholder_color)
        }

        val albumSelected = album?.id?.isNotEmpty() == true
        ui.txtHeader.visible = albumSelected
        ui.txtTitle.visible = albumSelected
        ui.txtSubtitle.visible = albumSelected
        ui.txtMsg.visible = !albumSelected
        if (albumSelected) {
            ui.txtTitle.text = album!!.title // album is non-null here
            ui.txtSubtitle.text = quantityString(R.plurals.items_in_album, album.mediaItemsCount, album.mediaItemsCount)
        } else {
            ui.txtMsg.textResource = R.string.msg_pick_one_album
        }
    }

    companion object {
        /** Instantiate an [SummaryRenderer] */
        fun create(context: Context, callback: AlbumsAdapter.Callback): SummaryRenderer =
            SummaryRenderer(SummaryUI(context), callback)
    }
}

/**
 * UI of the Summary header
 */
private class SummaryUI(context: Context) {
    val albumSizePx = context.dip(summaryAlbumSize)
    val avatarSizePx = context.dip(avatarSize)

    val rootView: View = context.UI {
        frameLayout {
            val auth = unauthorizedView()
            authView = auth.first
            btnAuth = auth.second

            summaryView = summaryView()
        }
    }.view

    lateinit var authView: View
    lateinit var btnAuth: View
    lateinit var summaryView: View
    lateinit var albumWrapper: View
    lateinit var imgCover: ImageView
    lateinit var imgAvatar: ImageView
    lateinit var imgTag: ImageView
    lateinit var imgStar: ImageView
    lateinit var txtHeader: TextView
    lateinit var txtTitle: TextView
    lateinit var txtSubtitle: TextView
    lateinit var txtMsg: TextView
    lateinit var icRefresh: View
    lateinit var icPrefs: View

    private fun _FrameLayout.unauthorizedView(): Pair<View, View> {
        lateinit var btnAuth: TextView
        val container = verticalLayout {
            gravity = Gravity.CENTER_HORIZONTAL
            padding = dip(20)
            topPadding = dip(80)

            textView(R.string.msg_authorization_request) {
                setLineSpacing(0f, 1.2f)
                textColorResource = R.color.secondaryTextColor
                textSize = 16f
            }.lparams {
                gravity = Gravity.CENTER
            }

            btnAuth = themedButton(text = R.string.btn_authorize, theme = R.style.PrimaryFlatButton)
                .lparams {
                    topMargin = dip(5)
                }
        }.lparams(width = matchParent)
        return container to btnAuth
    }

    private fun _FrameLayout.summaryView() = constraintLayout {
        visible = false

        // album cover
        albumWrapper = cardView {
            id = R.id.album_cover_holder
            cardElevation = 0f
            radius = dip(6).toFloat()
            setCardBackgroundColor(Color.TRANSPARENT)

            frameLayout {
                imgCover = imageView {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    adjustViewBounds = false
                }.lparams(albumSizePx, albumSizePx)

                imgTag = imageViewCompat(R.drawable.triangle_accent_36dp)
                    .lparams(dip(40), dip(40)) {
                        gravity = Gravity.TOP or Gravity.END
                    }

                imgStar = imageViewCompat(R.drawable.ic_star_black_24dp)
                    .lparams(dip(20), dip(20)) {
                        gravity = Gravity.TOP or Gravity.END
                        margin = dip(2)
                    }
            }
        }

        // texts
        txtHeader = header()
        txtTitle = title()
        txtSubtitle = subtitle()
        txtMsg = message()

        // account
        imgAvatar = imageView {
            id = R.id.img_avatar
            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = false
        }.lparams(avatarSizePx, avatarSizePx)

        // refresh icon
        icRefresh = imageViewCompat(R.drawable.ic_refresh_black_24dp) {
            id = R.id.ic_refresh
            setTintCompat(R.color.sys_icon_color)
        }

        // preferences icon
        icPrefs = imageViewCompat(R.drawable.ic_settings_black_24dp) {
            id = R.id.ic_prefs
            setTintCompat(R.color.sys_icon_color)
        }

        // add a force-crash icon if debugging
        addCrashIcon()

        applyConstraintSet {
            imgAvatar {
                connect(
                    END to END of PARENT_ID,
                    BOTTOM to BOTTOM of PARENT_ID
                )
            }
            albumWrapper {
                connect(
                    START to START of PARENT_ID,
                    TOP to TOP of PARENT_ID margin dip(26)
                )
                dimensionRation = "1"
            }
            txtHeader {
                connect(
                    TOP to TOP of albumWrapper,
                    START to END of albumWrapper margin dip(10),
                    END to START of imgAvatar margin dip(10),
                    BOTTOM to TOP of txtTitle
                )
            }
            txtTitle {
                connect(
                    START to START of txtHeader,
                    END to END of txtHeader,
                    TOP to BOTTOM of txtHeader margin dip(12),
                    BOTTOM to TOP of txtSubtitle
                )
            }
            txtSubtitle {
                connect(
                    START to START of txtHeader,
                    END to END of txtHeader,
                    TOP to BOTTOM of txtTitle
                )
            }
            txtMsg {
                connect(
                    TOP to TOP of albumWrapper,
                    START to END of albumWrapper margin dip(10),
                    END to START of imgAvatar margin dip(10)
                )
            }
            icRefresh {
                connect(
                    END to START of icPrefs margin icRefresh.dip(2),
                    TOP to TOP of icPrefs
                )
            }
            icPrefs {
                connect(
                    END to END of PARENT_ID,
                    TOP to TOP of PARENT_ID
                )
            }
            layoutCrashIcon(icRefresh)
        }
    }.lparams(width = matchParent)

    private fun _ConstraintLayout.header() = textView(R.string.header_currently_set) {
        id = R.id.txt_summary_header
        // visible = false
        textColorResource = R.color.secondaryTextColor
        textSize = 14f
        setTypeface(typeface, Typeface.BOLD)
    }.lparams(0)

    private fun _ConstraintLayout.title() = textView {
        id = R.id.txt_summary_title
        visible = false
        textColorResource = R.color.primaryTextColor
        textSize = 16f
        maxLines = 2
        setLineSpacing(0f, 1.2f)
        ellipsizeEnd()
    }.lparams(0)

    private fun _ConstraintLayout.subtitle() = textView {
        id = R.id.txt_summary_subtitle
        visible = false
        textColorResource = R.color.hintTextColor
        textSize = 14f
    }.lparams(0)

    private fun _ConstraintLayout.message() = textView {
        id = R.id.txt_summary_msg
        visible = false
        textColorResource = R.color.secondaryTextColor
        textSize = 16f
        setLineSpacing(0f, 1.2f)
    }.lparams(0)
}

/**
 * Album (thumbnail) renderer
 */
private class AlbumRenderer(
    val ui: AlbumUI,
    val onClick: (album: Album, position: Int) -> Unit
) : RecyclerView.ViewHolder(ui.rootView) {
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
            .load("${album.coverPhotoBaseUrl}=s$imgSize-p-no") // make it a square and no play button for video thumbnail
            .placeholder(R.drawable.album_cover_placeholder)
            // .resize(imgSize, imgSize)
            // .centerCrop()
            .transform(RoundedCornersTransformation(dip(6), 0))
            .into(ui.imgCover)
        ui.txtName.text = album.title
        ui.txtCount.text = quantityString(R.plurals.items_in_album, album.mediaItemsCount, album.mediaItemsCount)

        itemView.onClick {
            if (!album.isSelected) {
                onClick(album, adapterPosition)
            }
        }

        val bgVisible = ui.coverHolder.background != null
        if (album.isSelected) {
            ui.coverHolder.backgroundResource = R.drawable.album_cover_bg
            ObjectAnimator.ofInt(ui.coverHolder, paddingProp, 0, dip(16))
                .setDuration(long(android.R.integer.config_shortAnimTime))
                .doOnEnd { ui.icCheck.visible = true }
                .start()
        } else if (bgVisible) {
            ui.coverHolder.background = null
            ObjectAnimator.ofInt(ui.coverHolder, paddingProp, dip(16), 0)
                .setDuration(long(android.R.integer.config_shortAnimTime))
                .doOnStart { ui.icCheck.visible = false }
                .start()
        }
    }

    companion object {
        private var coverImageSize = 0

        fun Context.coverImageSize(): Int {
            if (coverImageSize <= 0) coverImageSize = screenWidth / 2
            return coverImageSize
        }

        /** Instantiate an [AlbumRenderer] */
        fun create(
            context: Context,
            onClick: (album: Album, position: Int) -> Unit
        ): AlbumRenderer = AlbumRenderer(AlbumUI(context), onClick)
    }
}

/**
 * UI of Album thumbnail
 */
private class AlbumUI(context: Context) {
    val rootView: View = context.UI {
        constraintLayout {
            bottomPadding = dip(8)

            val cover = albumCover()
            coverHolder = cover.first
            imgCover = cover.second

            icCheck = checkIcon()

            txtName = albumName()

            txtCount = albumSummary()

            applyConstraintSet {
                coverHolder {
                    connect(
                        START to START of PARENT_ID,
                        END to END of PARENT_ID,
                        TOP to TOP of PARENT_ID
                    )
                    dimensionRation = "1"
                }
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

    lateinit var coverHolder: View
    lateinit var imgCover: ImageView
    lateinit var icCheck: View
    lateinit var txtName: TextView
    lateinit var txtCount: TextView

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
        imageView(R.drawable.circle_white) {
            setTintCompat(R.color.check_icon)
        }.lparams(dip(20), dip(20)) {
            gravity = Gravity.CENTER
        }
        imageViewCompat(R.drawable.ic_check_circle_black_24dp) {
            setTintCompat(R.color.check_icon_bg)
        }.lparams(dip(24), dip(24))
    }
}
