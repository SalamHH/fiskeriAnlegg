package no.uio.ifi.team16.stim

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Fragment that displays the tutorial layouts based on position.
 */
class TutorialSlideFragment(private val slide: Int) : StimFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        when (slide) {
            0 -> {
                view?.findViewById<TextView>(R.id.tutorial_title1)
                    ?.setTextAppearance(R.style.tutorial_title)
                return inflater.inflate(R.layout.tutorial_slide, container, true)
            }
            1 -> {
                view?.findViewById<TextView>(R.id.tutorial_title2)
                    ?.setTextAppearance(R.style.tutorial_title)
                return inflater.inflate(R.layout.tutorial_slide2, container, true)
            }
            2 -> {
                view?.findViewById<TextView>(R.id.tutorial_title3)
                    ?.setTextAppearance(R.style.tutorial_title)
                return inflater.inflate(R.layout.tutorial_slide3, container, true)
            }
            3 -> {
                view?.findViewById<TextView>(R.id.tutorial_title4)
                    ?.setTextAppearance(R.style.tutorial_title)
                return inflater.inflate(R.layout.tutorial_slide4, container, true)
            }
            else -> {
                startActivity(Intent(this.context, OpeningScreen::class.java))
                return null
            }
        }
    }
}