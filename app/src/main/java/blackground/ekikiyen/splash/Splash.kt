package blackground.ekikiyen.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import blackground.ekikiyen.R
import blackground.ekikiyen.home.HomeActivity

class Splash: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler() .postDelayed({ startMain()}, 1350)
    }

    private fun startMain() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}