package no.uio.ifi.team16.stim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import no.uio.ifi.team16.stim.databinding.FragmentSiteInfoBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel

class SiteInfoFragment : StimFragment() {

    private lateinit var binding: FragmentSiteInfoBinding
    private val viewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSiteInfoBinding.inflate(inflater, container, false)

        val site = viewModel.getCurrentSite()

        binding.siteName.text = site.name
        binding.sitelocation.text = site.latLng.toString()

        binding.weatherInfoCard.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_siteInfoFragment_to_weatherFragment)
        }

        binding.saltInfoCard.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_siteInfoFragment_to_saltFragment)
        }

        binding.infectionInfoCard.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_siteInfoFragment_to_infectionFragment)
        }

        return binding.root

    }
}