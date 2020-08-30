package com.xinthink.muzei.photos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.xinthink.muzei.photos.CommandWorker.Companion.enqueueCmd

/**
 * Receives & handles command invoked from the Muzei app.
 */
class CommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "receives command: $intent")
        context.enqueueCmd(intent)
    }
}

private const val TAG = "MZPCmd"
