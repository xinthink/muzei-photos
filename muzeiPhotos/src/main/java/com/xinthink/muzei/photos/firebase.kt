package com.xinthink.muzei.photos

import androidx.annotation.Size
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/** Reports a firebase event with normalized key/value. */
fun logEvent(@Size(min = 1, max = 40) name: String, vararg params: Pair<String, Any?>) {
    val theParams = mutableListOf<Pair<String, Any?>>()
    for ((k, v) in params) {
        val key = if (k.length > 40) k.substring(0, 40) else k
        val value = if (v is String && v.length > 100) v.substring(0, 100) else v
        theParams.add(key to value)
    }

    Firebase.analytics.logEvent(name, bundleOf(*theParams.toTypedArray()))
}

/** Reports a firebase event with normalized key/value. */
fun FirebaseAnalytics.logEvent(@Size(min = 1, max = 40) name: String, vararg params: Pair<String, Any?>) {
    val theParams = mutableListOf<Pair<String, Any?>>()
    for ((k, v) in params) {
        val key = if (k.length > 40) k.substring(0, 40) else k
        val value = if (v is String && v.length > 100) v.substring(0, 100) else v
        theParams.add(key to value)
    }
    logEvent(name, bundleOf(*theParams.toTypedArray()))
}

/** Set normalized user properties */
fun FirebaseAnalytics.setUserProperties(vararg properties: Pair<String, String>) {
    for ((k, v) in properties) {
        val name = if (k.length > 24) k.substring(0, 24) else k
        val value = if (v.length > 36) v.substring(0, 36) else v
        setUserProperty(name, value)
    }
}
