package blackground.ekikiyen.about.view

import android.os.Bundle
import android.preference.PreferenceFragment
import blackground.ekikiyen.R

class AboutPreferenceFragment: PreferenceFragment() {

    companion object {
        fun get(): AboutPreferenceFragment {
            return AboutPreferenceFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.about_preference)
    }
}