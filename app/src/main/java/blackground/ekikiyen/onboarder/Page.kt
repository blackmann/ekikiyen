package blackground.ekikiyen.onboarder

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import blackground.ekikiyen.R
import java.io.Serializable

class Page : Fragment() {

    companion object {
        val info = "ek_info"

        fun get(title: String, description: String, image: Int, isLast: Boolean = false): Page {
            val args = Bundle()

            val pageInfo = PageInfo(title, description, image, isLast)
            args.putSerializable(info, pageInfo)

            val fragment = Page()
            fragment.arguments = args

            return fragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pageInfo = arguments[info] as PageInfo

        view.findViewById<TextView>(R.id.tv_onboarder_title).text = pageInfo.title

        view.findViewById<TextView>(R.id.tv_onboarder_description).text = pageInfo.description

        view.findViewById<ImageView>(R.id.iv_onboarder_image)
                .setImageDrawable(context.resources.getDrawable(pageInfo.image))

        if (pageInfo.isLast) {
            val finishButton = view.findViewById<Button>(R.id.finish)
            finishButton.visibility = View.VISIBLE
            finishButton.setOnClickListener { finish() }
        }
    }

    private fun finish() {

    }


    class PageInfo(val title: String, val description: String, val image: Int, val isLast: Boolean) : Serializable
}

