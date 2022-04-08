package no.uio.ifi.team16.stim

import androidx.fragment.app.Fragment

abstract class StimFragment : Fragment() {

    companion object {

        /**
         * The current observed municipality number
         */
        var currentMunicipalityNr: String? = null
        var currentSite: String? = null
    }
}