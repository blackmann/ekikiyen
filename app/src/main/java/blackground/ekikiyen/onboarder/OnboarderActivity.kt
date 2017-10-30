package blackground.ekikiyen.onboarder

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import blackground.ekikiyen.R
import me.relex.circleindicator.CircleIndicator


class OnboarderActivity : AppCompatActivity() {

    private val pages = ArrayList<Page>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard)

        pages.add(Page.get("Akwaaba",
                "Ekiki Yen is about sharing Vodafone Ekiki Me recharge numbers." +
                        " You can also load Ekiki Me for yourself. Swipe left to know how.",
                R.drawable.ekikiyen_icon))

        pages.add(Page.get("Load your credit",
                "Rather load your credit from the app using the dialer button, without including *134*.",
                R.drawable.share_02))

        pages.add(Page.get("Scan Credit", "You can also choose to scan the card rather than typing. Use the icon above to access the scanner",
                R.drawable.ekikiyen_scanner))

        pages.add(Page.get("Share",
                "After loading the credit come back into the app and share it without redialing the code again. Use the icon above to share.",
                R.drawable.ekikiyen_share))

        pages.add(Page.get("Load Ekiki Me",
                "Just tap an Ekiki Me code to load it. Simple. No dialing.",
                R.drawable.done_02, true))

        val pager = findViewById<ViewPager>(R.id.onboarder_pages)

        pager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return pages[position]
            }

            override fun getCount(): Int {
                return pages.size
            }

        }

        findViewById<CircleIndicator>(R.id.indicator)
                .setViewPager(pager)
    }


}