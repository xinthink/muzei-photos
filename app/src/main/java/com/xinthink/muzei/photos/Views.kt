package com.xinthink.muzei.photos

import android.animation.Animator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.ViewManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AnimRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.addListener
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageResource

var View.visible: Boolean
    get() = visibility == View.VISIBLE
    set(value) { visibility = if (value) View.VISIBLE else View.GONE }

var View.visibleKeepSpace: Boolean
    get() = visibility == View.VISIBLE
    set(value) { visibility = if (value) View.VISIBLE else View.INVISIBLE }

fun RecyclerView.ViewHolder.dip(value: Int): Int = itemView.dip(value)
fun RecyclerView.ViewHolder.dip(value: Float): Int = itemView.dip(value)

fun Context.integer(@IntegerRes resId: Int) = resources.getInteger(resId)
fun View.integer(@IntegerRes resId: Int) = resources.getInteger(resId)
fun RecyclerView.ViewHolder.integer(@IntegerRes resId: Int) = itemView.resources.getInteger(resId)
fun Context.long(@IntegerRes resId: Int) = integer(resId).toLong()
fun View.long(@IntegerRes resId: Int) = integer(resId).toLong()
fun RecyclerView.ViewHolder.long(@IntegerRes resId: Int) = integer(resId).toLong()

@ColorInt fun Context.color(@ColorRes resId: Int) = ResourcesCompat.getColor(resources, resId, theme)
@ColorInt fun View.color(@ColorRes resId: Int) = ResourcesCompat.getColor(resources, resId, context.theme)
@ColorInt fun View.color(strColor: String) = Color.parseColor(strColor)
fun View.colorStateList(@ColorRes resId: Int): ColorStateList = ColorStateList.valueOf(color(resId))

fun TextView.ellipsizeEnd() { ellipsize = TextUtils.TruncateAt.END }

/** Compatible way to tint an [ImageView] */
fun ImageView.setTintCompat(
    @ColorRes colorRes: Int = 0,
    @ColorInt color: Int = 0
) {
    val colorVal = if (colorRes != 0) this.color(colorRes) else color
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        imageTintList = ColorStateList.valueOf(colorVal)
    else setColorFilter(colorVal)
}

fun View.anim(@AnimRes animRes: Int): Animation {
    val anim = AnimationUtils.loadAnimation(context, animRes)
    startAnimation(anim)
    return anim
}

inline fun Animation.doOnStart(crossinline action: (animation: Animation?) -> Unit): Animation {
    setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) = action(animation)
        override fun onAnimationEnd(animation: Animation?) = Unit
        override fun onAnimationRepeat(animation: Animation?) = Unit
    })
    return this
}

inline fun Animation.doOnEnd(crossinline action: (animation: Animation?) -> Unit): Animation {
    setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) = Unit
        override fun onAnimationEnd(animation: Animation?) = action(animation)
        override fun onAnimationRepeat(animation: Animation?) = Unit
    })
    return this
}

/**
 * Add an action which will be invoked when the animation has started.
 *
 * @return the [Animator.AnimatorListener] added to the Animator
 * @see Animator.start
 */
inline fun Animator.doOnStart(crossinline action: (animator: Animator) -> Unit): Animator {
    addListener(onStart = action)
    return this
}

/**
 * Add an action which will be invoked when the animation has ended.
 *
 * @return the [Animator.AnimatorListener] added to the Animator
 * @see Animator.end
 */
inline fun Animator.doOnEnd(crossinline action: (animator: Animator) -> Unit): Animator {
    addListener(onEnd = action)
    return this
}

/** Factory method to build [AppCompatImageView], so that we can use vector drawables */
inline fun ViewManager.imageViewCompat(
    @DrawableRes resource: Int,
    @StyleRes theme: Int = 0,
    init: AppCompatImageView.() -> Unit = {}
): AppCompatImageView = ankoView(::AppCompatImageView, theme) {
    imageResource = resource
    init()
}
