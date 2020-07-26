package com.xinthink.muzei.photos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.apps.muzei.api.provider.ProviderContract
import com.xinthink.muzei.photos.worker.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PhotosPruneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Run the prune on the IO dispatcher so that we
        // don't block the main thread
        val result = goAsync()
        GlobalScope.launch(Dispatchers.IO) {
            val providerClient = ProviderContract.getProviderClient(
                context, BuildConfig.PHOTOS_AUTHORITY
            )
            providerClient.setArtwork(emptyList())
            result.finish()
            context.clearPageTokens()
        }
    }
}
