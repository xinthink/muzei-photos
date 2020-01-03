package com.xinthink.muzei.photos

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import com.xinthink.muzei.photos.App.Companion.updateDarkMode

/** User Settings */
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        findPreference<Preference>("prefTheme")?.onPreferenceChangeListener =
            OnPreferenceChangeListener { _, newValue ->
                context?.updateDarkMode(newValue as CharSequence?)
                true
            }
    }
}
