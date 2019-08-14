package com.xinthink.muzei.photos

import android.content.Context
import android.content.SharedPreferences

val Context.screenWidth: Int get() = resources.displayMetrics.widthPixels
val Context.screenHeigth: Int get() = resources.displayMetrics.heightPixels

val Context.pageTokenPrefs: SharedPreferences get() =
    getSharedPreferences("album_photos_page_tokens", Context.MODE_PRIVATE)
