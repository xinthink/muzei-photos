package com.xinthink.muzei.photos

import android.content.Context
import android.util.Log
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
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
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

        /** Checks API access interval to avoid 429 errors */
        private fun Context.isDownloadAllowed(isInitial: Boolean) =
            isInitial || (System.currentTimeMillis() - lastDownloadTime) > minDownloadInterval

        /** Schedule a photos-download background job */
        fun Context.enqueueLoad(initial: Boolean) {
            if (BuildConfig.DEBUG) Log.d(TAG, "enqueueLoad initial=$initial")
            logEvent("enqueue_photo_dl", "initial" to "$initial")
            FirebaseCrashlytics.getInstance().setCustomKey("last_download_ts", lastDownloadTime)

            if (!isDownloadAllowed(initial)) {
                if (BuildConfig.DEBUG) Log.d(TAG, "skip download request within minimal interval")
                return
            }

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

        if (!applicationContext.isDownloadAllowed(isInitial)) {
            if (BuildConfig.DEBUG) Log.d(TAG, "skip download work within minimal interval")
            return Result.failure()
        }

        logEvent(
            "get_photos",
            "album" to albumId,
            "initial" to "$isInitial",
            "pageToken" to (pageToken ?: "")
        )

        applicationContext.lastDownloadTime = System.currentTimeMillis()
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
            logEvent("err_get_photos", "message" to "$e")
            FirebaseCrashlytics.getInstance().recordException(e)
            return Result.failure()
        }

        if (pagination.mediaItems.isNotEmpty()) {
            // download photos
            val providerClient = ProviderContract.getProviderClient(
                applicationContext, BuildConfig.PHOTOS_AUTHORITY
            )
            pagination.mediaItems.forEach { item -> addArtwork(providerClient, item) }
        } else {
            Log.w(TAG, "No photos returned from API.")
        }

        return Result.success()
    }

    private fun addArtwork(providerClient: ProviderClient, mediaItem: MediaItem) {
        if (BuildConfig.DEBUG) Log.d(TAG, "adding MediaItem: $mediaItem")

        try {
            if (providerClient.isMediaDownloaded(mediaItem)) {
                if (BuildConfig.DEBUG) Log.d(TAG, "skip downloaded MediaItem: $mediaItem")
                return
            }

            val (w, h) = mediaItem.downloadSize // determines the desired dimensions
            val spec = if (w > 0) "w$w-h$h" else "d" // specifies desired dimensions or download directly
            logEvent(
                "dl_photo",
                FirebaseAnalytics.Param.ITEM_ID to mediaItem.id,
                "spec" to spec
            )
            FirebaseCrashlytics.getInstance().setCustomKey("media_item_spec", spec)

            val req = Request.Builder()
                .url("${mediaItem.baseUrl}=$spec")
                .build()
            getDownloaderHttpClient().newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.e(TAG, "download MediaItem failed: ${resp.code()}")
                    return
                }

                val uri = providerClient.addArtwork(mediaItem.toArtwork())
                if (uri != null) {
                    val os = applicationContext.contentResolver.openOutputStream(uri)
                    resp.downloadMedia(os)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "download MediaItem failed", e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun ProviderClient.isMediaDownloaded(mediaItem: MediaItem): Boolean {
        val artwork = applicationContext.contentResolver.query(
            contentUri,
            null,
            "${ProviderContract.Artwork.TOKEN} = ?",
            arrayOf(mediaItem.id),
            null
        ).use {
            if (it?.moveToFirst() == true) Artwork.fromCursor(it) else null
        }
        return artwork?.data?.exists() ?: false
    }

    private fun Response.downloadMedia(os: OutputStream?) {
        if (os == null) return
        val ins = body()?.byteStream() ?: return

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
    private fun MediaItem.toArtwork(): Artwork = Artwork(
        token = id,
        attribution = mediaMetadata?.formattedCreationTime(),
        title = if (description?.isNotEmpty() == true) description else defaultDescription,
        byline = contributorInfo?.displayName,
        webUri = productUrl.toUri()
    )

    /** Determines the maximum download dimension of this [MediaItem]. */
    private val MediaItem.downloadSize: Pair<Int, Int>
        get() {
            val ratio = aspectRatio
            val h = applicationContext.screenHeight
            val w = if (ratio != null) h * ratio else 0.0
            return Pair(w.toInt(), h)
        }

    private fun loadPageToken(albumId: String): String? = applicationContext.loadPageToken(albumId)

    private fun savePageToken(albumId: String, token: String?) =
        applicationContext.savePageToken(albumId, token)
}
