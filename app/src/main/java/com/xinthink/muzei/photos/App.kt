package com.xinthink.muzei.photos

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        updateDarkMode(defaultSharedPrefs.getString("prefTheme", null))
    }

    companion object {
        val Context.defaultSharedPrefs: SharedPreferences
            get() = PreferenceManager.getDefaultSharedPreferences(this)

        /** Update dark mode according to user preferences */
        fun Context.updateDarkMode(theme: CharSequence?) {
            if (resources == null) return

            AppCompatDelegate.setDefaultNightMode(
                when (theme) {
                    "1" -> AppCompatDelegate.MODE_NIGHT_NO
                    "2" -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                }
            )
        }
    }
}
