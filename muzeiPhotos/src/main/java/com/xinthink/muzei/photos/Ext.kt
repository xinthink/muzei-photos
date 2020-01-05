package com.xinthink.muzei.photos

import android.content.Context

const val CHANNEL_NAVIGATION = "CHANNEL_NAV"

val Context.screenWidth: Int get() = resources.displayMetrics.widthPixels
val Context.screenHeigth: Int get() = resources.displayMetrics.heightPixels
