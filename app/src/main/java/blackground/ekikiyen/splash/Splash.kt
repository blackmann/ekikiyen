package blackground.ekikiyen.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import blackground.ekikiyen.R
import blackground.ekikiyen.home.HomeActivity
import blackground.ekikiyen.onboarder.OnboarderActivity

class Splash : AppCompatActivity() {

    companion object {
        val onboardShownKey = "ek_onboard_shown"
        val introPrefs = "ek_intro_prefs"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({ startMain() }, 1000)
    }

    private fun startMain() {
        if (!getSharedPreferences(introPrefs, Context.MODE_PRIVATE)
                .getBoolean(onboardShownKey, false)) {
            startActivity(Intent(this, OnboarderActivity::class.java))
        } else {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        finish()
    }
}