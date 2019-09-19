package com.xinthink.widgets

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.jetbrains.anko.AlertBuilder
import org.jetbrains.anko.AlertBuilderFactory
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.internals.AnkoInternals
import org.jetbrains.anko.internals.AnkoInternals.NO_GETTER
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.yesButton
import kotlin.DeprecationLevel.ERROR

/** Extensions to [Anko AlertBuilder][AlertBuilder] */
object Alerts {
    /** Factory method reference to create a [MaterialAlertBuilder] */
    val Material: AlertBuilderFactory<AlertDialog> = ::MaterialAlertBuilder

    /** Create a [MaterialAlertBuilder] */
    inline fun Context.materialAlert(
        crossinline init: AlertBuilder<AlertDialog>.() -> Unit
    ): AlertBuilder<AlertDialog> {
        val builder = Material(this)
        builder.init()
        return builder
    }

    /** Create a [MaterialAlertBuilder] */
    inline fun Fragment.materialAlert(
        crossinline init: AlertBuilder<AlertDialog>.() -> Unit
    ): AlertBuilder<AlertDialog> = activity!!.materialAlert(init)

    @Suppress("UNUSED_PARAMETER")
    fun dummyDialogHandler(d: DialogInterface) = Unit

    fun AlertBuilder<*>.dummyOkButton() = okButton(::dummyDialogHandler)

    fun AlertBuilder<*>.dummyCancelButton() = cancelButton(::dummyDialogHandler)

    fun AlertBuilder<*>.dummyYesButton() = yesButton(::dummyDialogHandler)

    fun AlertBuilder<*>.dummyNoButton() = noButton(::dummyDialogHandler)

    fun AlertBuilder<*>.dummyPositiveButton(text: String) =
        positiveButton(text, ::dummyDialogHandler)

    fun AlertBuilder<*>.dummyPositiveButton(@StringRes textResource: Int) =
        positiveButton(textResource, ::dummyDialogHandler)

    fun AlertBuilder<*>.dummyNegativeButton(text: String) =
        negativeButton(text, ::dummyDialogHandler)

    fun AlertBuilder<*>.dummyNegativeButton(@StringRes textResource: Int) =
        negativeButton(textResource, ::dummyDialogHandler)

    fun AlertBuilder<*>.dummyNeutralPressed(text: String) =
        neutralPressed(text, ::dummyDialogHandler)

    fun AlertBuilder<*>.dummyNeutralPressed(@StringRes textResource: Int) =
        neutralPressed(textResource, ::dummyDialogHandler)
}

/** Material alert dialog factory for Anko */
class MaterialAlertBuilder(override val ctx: Context) : AlertBuilder<AlertDialog> {
    private val builder = MaterialAlertDialogBuilder(ctx)

    override var title: CharSequence
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) { builder.setTitle(value) }

    override var titleResource: Int
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) { builder.setTitle(value) }

    override var message: CharSequence
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) { builder.setMessage(value) }

    override var messageResource: Int
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) { builder.setMessage(value) }

    override var icon: Drawable
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) { builder.setIcon(value) }

    override var iconResource: Int
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) { builder.setIcon(value) }

    override var customTitle: View
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) { builder.setCustomTitle(value) }

    override var customView: View
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) { builder.setView(value) }

    override var isCancelable: Boolean
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) { builder.setCancelable(value) }

    override fun onCancelled(handler: (DialogInterface) -> Unit) {
        builder.setOnCancelListener(handler)
    }

    override fun onKeyPressed(handler: (dialog: DialogInterface, keyCode: Int, e: KeyEvent) -> Boolean) {
        builder.setOnKeyListener(handler)
    }

    override fun positiveButton(buttonText: String, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setPositiveButton(buttonText) { dialog, _ -> onClicked(dialog) }
    }

    override fun positiveButton(buttonTextResource: Int, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setPositiveButton(buttonTextResource) { dialog, _ -> onClicked(dialog) }
    }

    override fun negativeButton(buttonText: String, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setNegativeButton(buttonText) { dialog, _ -> onClicked(dialog) }
    }

    override fun negativeButton(buttonTextResource: Int, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setNegativeButton(buttonTextResource) { dialog, _ -> onClicked(dialog) }
    }

    override fun neutralPressed(buttonText: String, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setNeutralButton(buttonText) { dialog, _ -> onClicked(dialog) }
    }

    override fun neutralPressed(buttonTextResource: Int, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setNeutralButton(buttonTextResource) { dialog, _ -> onClicked(dialog) }
    }

    override fun items(items: List<CharSequence>, onItemSelected: (dialog: DialogInterface, index: Int) -> Unit) {
        builder.setItems(Array(items.size) { i -> items[i].toString() }) { dialog, which ->
            onItemSelected(dialog, which)
        }
    }

    override fun <T> items(items: List<T>, onItemSelected: (dialog: DialogInterface, item: T, index: Int) -> Unit) {
        builder.setItems(Array(items.size) { i -> items[i].toString() }) { dialog, which ->
            onItemSelected(dialog, items[which], which)
        }
    }

    override fun build(): AlertDialog = builder.create()

    override fun show(): AlertDialog = builder.show()
}
