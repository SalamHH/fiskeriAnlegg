package no.uio.ifi.team16.stim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import no.uio.ifi.team16.stim.data.WeatherForecast
import no.uio.ifi.team16.stim.databinding.FragmentSiteInfoBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel


class SiteInfoFragment : StimFragment() {

    private lateinit var binding: FragmentSiteInfoBinding
    private val viewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSiteInfoBinding.inflate(inflater, container, false)

        val site = viewModel.getCurrentSite() ?: return binding.root

        binding.siteName.text = site.name

        viewModel.getWeatherData().observe(viewLifecycleOwner, this::onWeatherLoaded)
        viewModel.loadWeatherAtSite(site)

        binding.tempIdag.text = getString(R.string.temperature, site.weatherForecast?.first?.temperature)
        binding.tempImorgen.text = getString(R.string.temperature, site.weatherForecast?.second?.temperature)

        //posisjon
        binding.posisjonView.text = "${site.latLong.lat}, ${site.latLong.lng}"

        binding.generalInfoBox.setOnClickListener {
            // If the CardView is already expanded, set its visibility
            //  to gone and change the expand less icon to expand more.
            // If the CardView is already expanded, set its visibility
            //  to gone and change the expand less icon to expand more.
            if (binding.relativelayout.getVisibility() === View.VISIBLE) {

                // The transition of the hiddenView is carried out
                //  by the TransitionManager class.
                // Here we use an object of the AutoTransition
                // Class to create a default transition.
                TransitionManager.beginDelayedTransition(
                    binding.generalInfoBox,
                    AutoTransition()
                )
                binding.relativelayout.setVisibility(View.GONE)
                binding.pil.setImageResource(R.drawable.down_icon)
            } else {
                TransitionManager.beginDelayedTransition(
                    binding.generalInfoBox,
                    AutoTransition()
                )
                binding.relativelayout.setVisibility(View.VISIBLE)
                binding.pil.setImageResource(R.drawable.up_icon)
            }

            //anleggsnummer
            binding.anleggsnrView.text = site.id.toString()

            //plassering
            binding.plasseringView.text = site.placementType ?: "-----"

            //kapasitet
            binding.kapasitetView.text = site.capacity.toString()

            //vanntype
            binding.vannTypeView.text = site.waterType ?: "-----"

            //kommune
            binding.prodOmraadeView.text = site.placement?.municipalityName ?: "-----"
        }


        binding.weatherInfoCard.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_siteInfoFragment_to_weatherFragment)
        }

        binding.waterInfoCard.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_siteInfoFragment_to_generalInfoFragment)
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
    private fun onWeatherLoaded(forecast: WeatherForecast?) {
        forecast?.apply {
            binding.vaerIdag.setImageDrawable(first.icon.asDrawable(requireContext()))
            binding.vaerImorgen.setImageDrawable(second.icon.asDrawable(requireContext()))
            binding.tempIdag.text = getString(R.string.temperature, first.temperature)
            binding.tempImorgen.text = getString(R.string.temperature, second.temperature)
        }
    }

}