package no.uio.ifi.team16.stim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import no.uio.ifi.team16.stim.databinding.FragmentStartPageBinding

/**
MIDLERTIDIG FRAGMENT!!
Dette er et fragment som viser valg mellom MAP og RECYCLERVIEW
 */
class StartPageFragment : StimFragment() {

    private lateinit var binding: FragmentStartPageBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStartPageBinding.inflate(inflater, container, false)

        /*
        binding.mapButton.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_startPageFragment_to_mapFragment)
        }

        binding.recyclerViewBtn.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_startPageFragment_to_favoriteSitesFragment)
        }

         */



        return binding.root
    }
}