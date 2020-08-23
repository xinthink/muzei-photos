package com.xinthink.muzei.photos

import android.content.Context
import com.xinthink.muzei.photos.worker.BuildConfig
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import java.io.File

const val CHANNEL_NAVIGATION = "CHANNEL_NAV"
const val PHOTOS_CACHE_SIZE = 256L * 1024 * 1024

val Context.screenWidth: Int get() = resources.displayMetrics.widthPixels
val Context.screenHeigth: Int get() = resources.displayMetrics.heightPixels

/** Get cache settings for photos downloader */
fun Context.createPhotosCache(): Cache =
    Cache(File(cacheDir, "downloader"), PHOTOS_CACHE_SIZE)

/** Create a logging interceptor for OkHttp client */
fun OkHttpClient.Builder.addLoggingInterceptor(): OkHttpClient.Builder {
    val interceptor = HttpLoggingInterceptor()
        .setLevel(if (BuildConfig.DEBUG) Level.BODY else Level.NONE)
    return addInterceptor(interceptor)
        // .addNetworkInterceptor(interceptor)
}

/** Determines the aspect ration (width / height) of this [MediaItem]. */
val MediaItem.aspectRatio: Double?
    get() {
        val metadata = mediaMetadata ?: return null
        return java.lang.Double.parseDouble(metadata.width) / java.lang.Double.parseDouble(metadata.height)
    }
