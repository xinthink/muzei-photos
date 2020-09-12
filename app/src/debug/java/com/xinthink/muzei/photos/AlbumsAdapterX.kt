package com.xinthink.muzei.photos

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.xinthink.widgets.Alerts.dummyNoButton
import com.xinthink.widgets.Alerts.materialAlert
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.END
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.START
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.TOP
import org.jetbrains.anko.dip
import org.jetbrains.anko.sdk19.listeners.onClick
import org.jetbrains.anko.yesButton

/** Add a force-crash icon in order to verify Crashlytics integration */
fun ConstraintLayout.addCrashIcon() {
    imageViewCompat(R.drawable.ic_bug_report_red_24dp) {
        id = R.id.ic_crash
        onClick {
            context.materialAlert {
                title = "Crash!"
                message = "Would you like to crash the app in order to verify Crashlytics integration?"
                yesButton {
                    throw RuntimeException("Test Crash") // Force a crash
                }
                dummyNoButton()
            }.show()
        }
    }
}

/** Apply constraints to force-crash icon */
fun ConstraintSetBuilder.layoutCrashIcon(icRefresh: View) {
    R.id.ic_crash {
        connect(
            END to START of icRefresh margin icRefresh.dip(2),
            TOP to TOP of icRefresh
        )
    }
}
