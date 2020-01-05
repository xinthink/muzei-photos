package com.xinthink.muzei.photos

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import org.jetbrains.anko.intentFor

/** User Settings */
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        findPreference<Preference>("prefTheme")?.onPreferenceChangeListener =
            OnPreferenceChangeListener { _, newValue ->
                context?.updateDarkMode(newValue as CharSequence?)
                true
            }
        findPreference<Preference>("prefSwitchAccount")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                onSwitchAccount()
                true
            }
    }

    /** Switch to another Google account */
    private fun onSwitchAccount() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.dlg_title_switch_account)
            .setMessage(R.string.dlg_msg_switch_account)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                activity?.finish()
                @Suppress("EXPERIMENTAL_API_USAGE")
                ctx.intentFor<AlbumsActivity>("switchAccount" to true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .also { startActivity(it) }
            }
            .show()
    }
}
