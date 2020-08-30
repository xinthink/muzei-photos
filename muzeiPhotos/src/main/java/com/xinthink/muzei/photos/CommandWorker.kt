package com.xinthink.muzei.photos

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.xinthink.muzei.photos.worker.BuildConfig

/** A worker handling Muzei commands. */
class CommandWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    companion object {
        private const val TAG = "MZPCmd"

        /** Schedule a Muzei command handling job */
        fun Context.enqueueCmd(intent: Intent) {
            Log.d(TAG, "enqueueCmd intent=$intent")
            val workManager = WorkManager.getInstance(this)
            workManager.enqueue(
                OneTimeWorkRequestBuilder<CommandWorker>()
                    .setInputData(
                        Data.Builder()
                            .putInt(EXTRA_COMMAND_ID, intent.getIntExtra(EXTRA_COMMAND_ID, 0))
                            .build()
                    )
                    .build()
            )
        }
    }

    override fun doWork(): Result {
        val cmd = inputData.getInt(EXTRA_COMMAND_ID, 0)
        return try {
            if (cmd == COMMAND_ID_PRUNE) {
                prunePhotos()
            }
            Result.success()
        } catch (e: Throwable) {
            Log.e(TAG, "failed to handle command: id=$cmd", e)
            Result.failure()
        }
    }

    private fun prunePhotos() {
        val contentUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(BuildConfig.PHOTOS_AUTHORITY)
            .build()
        applicationContext.contentResolver.query(contentUri, null, null, null, null)
            ?.use { c ->
                Log.d(TAG, "found ${c.count} photos")
            }
        Log.d(TAG, "deleting all downloaded photos")
        applicationContext.contentResolver.delete(contentUri, null, null)
        applicationContext.contentResolver.query(contentUri, null, null, null, null)
            ?.use { c ->
                Log.d(TAG, "found ${c.count} photos")
            }
        applicationContext.clearPageTokens()
    }
}
