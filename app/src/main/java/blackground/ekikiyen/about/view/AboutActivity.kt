package blackground.ekikiyen.about.view

import android.app.Activity
import android.os.Bundle
import blackground.ekikiyen.R

class AboutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        fragmentManager?.beginTransaction()
                ?.replace(R.id.preference_container, AboutPreferenceFragment.get())
                ?.commit()
    }
}
