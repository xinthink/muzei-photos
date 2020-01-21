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

import android.util.Log
import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import com.xinthink.muzei.photos.PhotosWorker.Companion.enqueueLoad
import com.xinthink.muzei.photos.TokenService.Companion.loadToken
import com.xinthink.muzei.photos.worker.BuildConfig
import com.xinthink.muzei.photos.worker.R

class PhotosArtProvider : MuzeiArtProvider() {

    companion object {
        private const val TAG = "MZProvider"

        private const val COMMAND_ID_SETTINGS = 1
        private const val COMMAND_ID_PRUNE = 2
    }

    override fun onCreate(): Boolean {
        super.onCreate()
        if (BuildConfig.DEBUG) Log.d(TAG, "PhotosArtProvider.onCreate, context=${context != null}")
        context?.loadToken()
        return true
    }

    override fun onLoadRequested(initial: Boolean) {
        context?.enqueueLoad(initial)
    }

    override fun getCommands(artwork: Artwork) = context?.run {
        listOfNotNull(
            // UserCommand(
            //     COMMAND_ID_SETTINGS,
            //     getString(R.string.menu_settings)
            // ),
            UserCommand(
                COMMAND_ID_PRUNE,
                getString(R.string.menu_prune)
            )
        )
    } ?: emptyList()

    override fun onCommand(artwork: Artwork, id: Int) {
        when (id) {
            COMMAND_ID_SETTINGS -> Unit
            COMMAND_ID_PRUNE -> {
                // clean existed artworks
                setArtwork(emptyList())
                context?.clearPageTokens()
            }
        }
    }

    // override fun openArtworkInfo(artwork: Artwork): Boolean {
    //     if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) return super.openArtworkInfo(artwork)
    //
    //     // workaround to restrictions on starting activities from the background
    //     // see https://developer.android.com/guide/components/activities/background-starts
    //     val uri = artwork.webUri ?: return false
    //     val ctx = context ?: return false
    //     val pendingIntent = PendingIntent.getActivity(
    //         ctx, 0,
    //         Intent(Intent.ACTION_VIEW, uri)
    //             .addCategory(Intent.CATEGORY_BROWSABLE)
    //             .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    //         PendingIntent.FLAG_UPDATE_CURRENT
    //     )
    //     val notification =
    //         NotificationCompat.Builder(ctx, CHANNEL_NAVIGATION)
    //             .setSmallIcon(R.mipmap.ic_notification)
    //             .setContentTitle(ctx.getString(R.string.push_title_photo))
    //             .setContentText(ctx.getString(R.string.push_text_photo))
    //             .setPriority(NotificationCompat.PRIORITY_HIGH)
    //             .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
    //             .setContentIntent(pendingIntent)
    //             .setAutoCancel(true)
    //             .build()
    //     NotificationManagerCompat.from(ctx).notify(0, notification)
    //     return true
    // }
}
