package no.uio.ifi.team16.stim

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import no.uio.ifi.team16.stim.databinding.FragmentTutorialSlide1Binding

private lateinit var binding: FragmentTutorialSlide1Binding

/**
 * A simple [Fragment] subclass.
 * Use the [TutorialSlide1Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TutorialSlide1Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }

}