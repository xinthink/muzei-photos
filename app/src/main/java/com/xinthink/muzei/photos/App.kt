package com.xinthink.muzei.photos

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            // workaround to restrictions on starting activities from the background
            // see https://developer.android.com/guide/components/activities/background-starts
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_NAVIGATION,
                        resources?.getString(R.string.channel_navigation) ?: "Navigation",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = resources?.getString(R.string.channel_navigation_desc)
                            ?: "Navigation to original Google Photos pages"
                        setShowBadge(false)
                        lockscreenVisibility = Notification.VISIBILITY_SECRET
                    })
        }
    }
}
