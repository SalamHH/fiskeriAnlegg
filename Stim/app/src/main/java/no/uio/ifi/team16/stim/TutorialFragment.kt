package no.uio.ifi.team16.stim

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

/**
 * Screen slide acivity manager. Manages the view pager.
 */

class TutorialFragment : FragmentActivity() {

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private lateinit var viewPager: ViewPager2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_tutorial)
        window.statusBarColor = getColor(R.color.skyblue)

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.viewpager)

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = TutorialSlidePageAdapter(this)
        viewPager.adapter = pagerAdapter
    }


    override fun onBackPressed() {
        if (viewPager.currentItem == 0 || viewPager.currentItem == 5) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    private inner class TutorialSlidePageAdapter(fa: TutorialFragment) : FragmentStateAdapter(fa) {

        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): Fragment {
            return TutorialSlideFragment(position)
        }
    }
}