package no.uio.ifi.team16.stim

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner

abstract class StimFragment : Fragment() {

    // todo sjekk om dette er nødvending
    fun getLifecycleOwner(): LifecycleOwner {
        if (activity != null) {
            return activity as FragmentActivity
        }
        return viewLifecycleOwner
    }
}