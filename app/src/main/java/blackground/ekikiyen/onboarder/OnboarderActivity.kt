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

        pages.add(Page.get("Akwaaba", "Take a moment to know what Ekiki yen is", R.drawable.ekikiyen_icon, true))

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