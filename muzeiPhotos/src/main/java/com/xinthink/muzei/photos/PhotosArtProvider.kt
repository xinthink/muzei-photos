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

import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import com.xinthink.muzei.photos.worker.R

class PhotosArtProvider : MuzeiArtProvider() {

    companion object {
        private const val TAG = "MZPProvider"

        private const val COMMAND_ID_SETTINGS = 1
        private const val COMMAND_ID_PRUNE = 2
    }

    override fun onLoadRequested(initial: Boolean) {
        PhotosWorker.enqueueLoad(initial)
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
            COMMAND_ID_PRUNE -> setArtwork(emptyList()) // clean existed artworks
        }
    }

    // override fun openArtworkInfo(artwork: Artwork): Boolean {
    //     val uri = artwork.webUri ?: return false
    //     val ctx = context ?: return false
    //     return try {
    //         ctx.startActivity(
    //             Intent(Intent.ACTION_VIEW, uri)
    //                 .addCategory(Intent.CATEGORY_BROWSABLE)
    //                 .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    //         )
    //         true
    //     } catch (e: Throwable) {
    //         Log.w(TAG, "Could not open $uri, artwork=${ContentUris.withAppendedId(contentUri, artwork.id)}", e)
    //         false
    //     }
    // }
}
