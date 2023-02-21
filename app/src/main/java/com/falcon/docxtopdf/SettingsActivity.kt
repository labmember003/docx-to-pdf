package com.falcon.docxtopdf

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val preferenceContact = preferenceManager.findPreference<Preference>("contact")
            preferenceContact?.setOnPreferenceClickListener {
                composeEmail("Regarding App " + getString(R.string.app_name))
                true
            }
            val preferenceBugReport = preferenceManager.findPreference<Preference>("bug")
            preferenceBugReport?.setOnPreferenceClickListener {
                composeEmail("Bug Report For " + getString(R.string.app_name))
                true
            }
            val preference = preferenceManager.findPreference<Preference>("libraries")
            preference?.setOnPreferenceClickListener {
                startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                true
            }
        }


        private fun composeEmail(subject: String) {
            val a = arrayOf("usarcompanion@gmail.com")
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, a)
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No Mail App Found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}