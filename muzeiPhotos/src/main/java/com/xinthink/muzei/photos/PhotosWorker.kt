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
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import com.xinthink.muzei.photos.PhotosService.Companion.albumPhotos
import com.xinthink.muzei.photos.worker.BuildConfig
import com.xinthink.muzei.photos.worker.R
import org.jetbrains.anko.defaultSharedPreferences
import java.io.IOException

class PhotosWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private const val TAG = "MZPWorker"

        fun enqueueLoad(initial: Boolean) {
            if (BuildConfig.DEBUG) Log.d(TAG, "enqueueLoad initial=$initial")
            val workManager = WorkManager.getInstance()
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

    override fun doWork(): Result {
        val albumId: String = loadSelectedAlbumId() ?: return Result.failure()
        val isInitial = inputData.getBoolean("initial", false)
        val pageToken = if (isInitial) null else loadPageToken(albumId)
        val pagination = try {
            if (BuildConfig.DEBUG) Log.d(TAG, "fetching mediaItems album=$albumId, pageToken=$pageToken")
            applicationContext.albumPhotos(
                albumId = albumId,
                pageSize = 3,
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
        val defaultDesc = applicationContext.getString(R.string.default_photo_desc)
        providerClient.addArtwork(pagination.mediaItems
            // .filter { it.mimeType.startsWith("image") }
            .map {
                if (BuildConfig.DEBUG) Log.d(TAG, "adding MediaItem: $it")
                Artwork().apply {
                    token = it.id
                    attribution = it.mediaMetadata?.formattedCreationTime()
                    title = if (it.description?.isNotEmpty() == true) it.description else defaultDesc
                    byline = it.contributorInfo?.displayName
                    persistentUri = "${it.baseUrl}=d".toUri()
                    webUri = it.productUrl.toUri()
                }
            })
        return Result.success()
    }

    private fun loadSelectedAlbumId(): String? =
        applicationContext.defaultSharedPreferences.getString("selected_album_id", null)

    private fun loadPageToken(albumId: String): String? =
        applicationContext.pageTokenPrefs.getString("photos_page_token_$albumId", null)

    private fun savePageToken(albumId: String, token: String?) =
        applicationContext.pageTokenPrefs.edit {
            putString("photos_page_token_$albumId", token)
        }
}
