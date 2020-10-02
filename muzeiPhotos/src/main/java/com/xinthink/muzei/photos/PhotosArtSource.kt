package com.xinthink.muzei.photos

import android.app.Service
import android.content.Intent

/**
 * This class is kept only to serve as a tombstone to Muzei to know to replace it
 * with [PhotosArtProvider].
 */
class PhotosArtSource : Service() {
    override fun onBind(intent: Intent?) = null
}
