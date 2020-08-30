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

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.RemoteActionCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import com.xinthink.muzei.photos.PhotosWorker.Companion.enqueueLoad
import com.xinthink.muzei.photos.TokenService.Companion.loadToken
import com.xinthink.muzei.photos.worker.BuildConfig
import com.xinthink.muzei.photos.worker.R

class PhotosArtProvider : MuzeiArtProvider() {

    companion object {
        private const val TAG = "MZProvider"
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

    /* kept for backward compatibility with Muzei 3.3 */
    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun getCommands(artwork: Artwork) = context?.run {
        listOfNotNull(
            com.google.android.apps.muzei.api.UserCommand(
                COMMAND_ID_PRUNE,
                getString(R.string.menu_prune)
            )
        )
    } ?: emptyList()

    /* kept for backward compatibility with Muzei 3.3 */
    @Suppress("OverridingDeprecatedMember")
    override fun onCommand(artwork: Artwork, id: Int) {
        when (id) {
            COMMAND_ID_PRUNE -> {
                // clean existed artworks
                setArtwork(emptyList())
                context?.clearPageTokens()
            }
        }
    }

    /* Used on Muzei 3.4+ */
    override fun getCommandActions(artwork: Artwork): List<RemoteActionCompat> {
        val ctx = context ?: return emptyList()
        return listOf(
            RemoteActionCompat(
                IconCompat.createWithResource(ctx, android.R.drawable.ic_menu_delete),
                ctx.getString(R.string.menu_prune),
                "",
                PendingIntent.getBroadcast(
                    ctx, 0,
                    Intent(ctx, CommandReceiver::class.java).putExtra(
                        EXTRA_COMMAND_ID,
                        COMMAND_ID_PRUNE
                    ),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            ).apply {
                setShouldShowIcon(false)
            }
        )
    }
}

/** Command id of deleting all downloaded photos */
const val COMMAND_ID_PRUNE = 2

const val EXTRA_COMMAND_ID = "mzp.command_id"
