@file:Suppress("UNUSED_PARAMETER")

package com.xinthink.muzei.photos

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder

/** Add a force-crash icon in order to verify Crashlytics integration, no effect in release builds */
fun ConstraintLayout.addCrashIcon() = Unit

/** Apply constraints to force-crash icon, no effect in release builds */
fun ConstraintSetBuilder.layoutCrashIcon(icRefresh: View) = Unit
