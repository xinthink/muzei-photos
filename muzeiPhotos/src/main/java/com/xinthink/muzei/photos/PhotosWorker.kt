/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xinthink.muzei.photos

import android.content.Context
import android.util.Log
import android.util.Size
import androidx.core.net.toUri
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderClient
import com.google.android.apps.muzei.api.provider.ProviderContract
import com.xinthink.muzei.photos.PhotosService.Companion.albumPhotos
import com.xinthink.muzei.photos.worker.BuildConfig
import com.xinthink.muzei.photos.worker.R
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.OutputStream

/** A background worker to download photos */
class PhotosWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    companion object {
        private const val TAG = "MZPWorker"

        private var mDownloaderHttpClient: OkHttpClient? = null

        /** Schedule a photos-download background job */
        fun Context.enqueueLoad(initial: Boolean) {
            if (BuildConfig.DEBUG) Log.d(TAG, "enqueueLoad initial=$initial")
            val workManager = WorkManager.getInstance(this)
            workManager.enqueue(
                OneTimeWorkRequestBuilder<PhotosWorker>()
                    .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    )
                    .setInputData(Data.Builder()
                        .putBoolean("initial", initial)
                        .build()
                    )
                    .build()
            )
        }
    }

    @Synchronized
    fun getDownloaderHttpClient(): OkHttpClient {
        if (mDownloaderHttpClient == null) {
            mDownloaderHttpClient = OkHttpClient.Builder()
                .cache(applicationContext.createPhotosCache())
                .addLoggingInterceptor()
                .build()
        }
        return mDownloaderHttpClient!!
    }

    private val selectedAlbumId: String? get() = applicationContext.selectedAlbumId
    private val defaultDescription: String by lazy {
        applicationContext.getString(R.string.default_photo_desc)
    }

    override fun doWork(): Result {
        val albumId: String = selectedAlbumId ?: return Result.failure()
        val isInitial = inputData.getBoolean("initial", false)
        val pageToken = if (isInitial) null else loadPageToken(albumId)
        val pagination = try {
            if (BuildConfig.DEBUG) Log.d(
                TAG,
                "fetching mediaItems album=$albumId, pageToken=$pageToken"
            )
            applicationContext.albumPhotos(
                albumId = albumId,
                pageToken = pageToken
            ).also { resp ->
                savePageToken(albumId, resp.nextPageToken)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading Photos response", e)
            return Result.failure()
        }

        if (pagination.mediaItems.isEmpty()) {
            Log.w(TAG, "No photos returned from API.")
            return Result.failure()
        }

        val providerClient = ProviderContract.getProviderClient(
            applicationContext, BuildConfig.PHOTOS_AUTHORITY
        )
        pagination.mediaItems.forEach { item -> addArtwork(providerClient, item) }
        return Result.success()
    }

    private fun addArtwork(providerClient: ProviderClient, mediaItem: MediaItem) {
        if (BuildConfig.DEBUG) Log.d(TAG, "adding MediaItem: $mediaItem")

        try {
            var url = mediaItem.baseUrl
            val size = mediaItem.downloadSize // determines the desired dimensions
            url += if (size.width > 0)
                "=w${size.width}-h${size.height}" // specifies the maximum dimensions
            else "=d" // or download directly

            val req = Request.Builder()
                .url(url)
                .build()
            getDownloaderHttpClient().newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.e(TAG, "download MediaItem failed: ${resp.code()}")
                    return
                }

                val uri = providerClient.addArtwork(mediaItem.toArtwork())
                if (uri != null) {
                    val os = applicationContext.contentResolver.openOutputStream(uri)
                    downloadMedia(resp, os)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "download MediaItem failed", e)
        }
    }

    private fun downloadMedia(resp: Response, os: OutputStream?) {
        if (os == null) return
        val ins = resp.body()?.byteStream() ?: return

        os.use {
            val bytes = ByteArray(512)
            var len = ins.read(bytes)
            while (len != -1) {
                os.write(bytes, 0, len)
                len = ins.read(bytes)
            }
        }
    }

    /** Converts this [MediaItem] into an [Artwork] object. */
    private fun MediaItem.toArtwork(): Artwork = Artwork.Builder()
        .token(id)
        .attribution(mediaMetadata?.formattedCreationTime())
        .title(
            if (description?.isNotEmpty() == true) description else defaultDescription
        )
        .byline(contributorInfo?.displayName)
        .webUri(productUrl.toUri())
        .build()

    /** Determines the maximum download dimension of this [MediaItem]. */
    private val MediaItem.downloadSize: Size
        get() {
            val ratio = aspectRatio
            val h = applicationContext.screenHeigth
            val w = if (ratio != null) h * ratio else 0.0
            return Size(w.toInt(), h)
        }

    private fun loadPageToken(albumId: String): String? = applicationContext.loadPageToken(albumId)

    private fun savePageToken(albumId: String, token: String?) =
        applicationContext.savePageToken(albumId, token)
}
