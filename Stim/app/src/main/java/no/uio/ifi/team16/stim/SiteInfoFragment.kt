package no.uio.ifi.team16.stim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import no.uio.ifi.team16.stim.databinding.FragmentSiteInfoBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel

class SiteInfoFragment : StimFragment() {

    private lateinit var binding: FragmentSiteInfoBinding
    private val viewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSiteInfoBinding.inflate(inflater, container, false)

        val site = viewModel.getCurrentSite()

        binding.siteName.text = site.name
        binding.sitelocation.text = site.latLong.toString()

        binding.weatherInfoCard.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_siteInfoFragment_to_weatherFragment)
        }

        binding.saltInfoCard.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_siteInfoFragment_to_saltFragment)
        }
        //Vanntemperatur og saltholdighet

        viewModel.loadNorKyst800()

        viewModel.getNorKyst800Data().observe(viewLifecycleOwner) {
            if (it != null) {
                binding.temperatureTextview.text = "${it.getTemperature(site.latLong).toString()}°"
                binding.saltTextview.text = it.getSalinity(site.latLong).toString()
            }
        }

        binding.infectionInfoCard.setOnClickListener {
            val extras = FragmentNavigatorExtras(binding.infectionIcon to "image_big")
            view?.findNavController()?.navigate(
                R.id.action_siteInfoFragment_to_infectionFragment,
                null,
                null,
                extras
            )
        }

        return binding.root

    }
}