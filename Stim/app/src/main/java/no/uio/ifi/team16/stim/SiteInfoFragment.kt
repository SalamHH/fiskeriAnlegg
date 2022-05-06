package no.uio.ifi.team16.stim

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.WeatherForecast
import no.uio.ifi.team16.stim.databinding.FragmentSiteInfoBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import no.uio.ifi.team16.stim.util.Options


class SiteInfoFragment : StimFragment() {

    private lateinit var binding: FragmentSiteInfoBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var site: Site

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSiteInfoBinding.inflate(inflater, container, false)

        site = viewModel.getCurrentSite() ?: return binding.root

        binding.LoadingScreen.loadingLayout.visibility = View.VISIBLE

        binding.siteName.text = site.name

        viewModel.getWeatherData().observe(viewLifecycleOwner, this::onWeatherLoaded)
        viewModel.loadWeatherAtSite(site)

        binding.tempIdag.text =
            getString(R.string.temperature, site.weatherForecast?.first?.temperature)
        binding.tempImorgen.text =
            getString(R.string.temperature, site.weatherForecast?.second?.temperature)

        //posisjon
        binding.posisjonView.text = "${site.latLong.lat}, ${site.latLong.lng}"
        viewModel.loadNorKyst800AtSite(site)
        viewModel.loadInfectiousPressureTimeSeriesAtSite(site)

        val waterInfofinished = setWaterInfo()
        setInfectionInfo()

        binding.generalInfoBox.setOnClickListener {
            // If the CardView is already expanded, set its visibility
            //  to gone and change the expand less icon to expand more.
            // If the CardView is already expanded, set its visibility
            //  to gone and change the expand less icon to expand more.
            if (binding.relativelayout.visibility == View.VISIBLE) {

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
                binding.relativelayout.visibility = View.VISIBLE
                binding.pil.setImageResource(R.drawable.up_icon)
            }

            //anleggsnummer
            binding.anleggsnrView.text = site.nr.toString()

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
            if (waterInfofinished) {
                view?.findNavController()
                    ?.navigate(R.id.action_siteInfoFragment_to_generalInfoFragment)
            } else {
                val text = "Ikke tilgjenglig"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(context, text, duration)
                toast.show()
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

    private fun onWeatherLoaded(forecast: WeatherForecast?) {
        forecast?.apply {
            binding.vaerIdag.setImageDrawable(first.icon.asDrawable(requireContext()))
            binding.vaerImorgen.setImageDrawable(second.icon.asDrawable(requireContext()))
            binding.tempIdag.text = getString(R.string.temperature, first.temperature)
            binding.tempImorgen.text = getString(R.string.temperature, second.temperature)
        }
    }

    private fun setWaterInfo(): Boolean {
        viewModel.getNorKyst800AtSiteData(site).observe(viewLifecycleOwner) {
            binding.LoadingScreen.loadingLayout.visibility = View.GONE
            it?.apply {
                //forecast data is also available in the norkyst object! (about 66 hours, time indexes hours)
                binding.temp.text = "%4.1f".format(getTemperature()) + "°"
                binding.varsel.text = "%4.1f".format(getSalinity())
            } ?: run {
                binding.temp.text = "N/A"
                binding.varsel.text = "N/A"
            }
        }
        if (binding.temp.text == "N/A" || binding.varsel.text == "N/A") {
            return false
        }
        return true
    }

    private fun setInfectionInfo() {
        viewModel.getInfectiousPressureTimeSeriesData(site).observe(viewLifecycleOwner) {
            it?.getConcentrationsAsGraph()?.apply {
                val infectiondata = map { xy -> xy.y }.toTypedArray()
                binding.fare.setImageDrawable(calculateInfectionStatusIcon(infectiondata))
            } ?: run {
                binding.fare.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.no_data,
                        null
                    )
                )
            }
        }
    }

    private fun calculateInfectionStatusIcon(infectiondata: Array<Float>): Drawable? {

        if (infectiondata.lastIndex > 1 && infectiondata.average() > Options.infectionExists) {
            //sjekker om det er signifikant økning/miskning på de siste 3 datapunktene
            val lastThree = arrayOf(
                infectiondata[infectiondata.lastIndex - 2],
                infectiondata[infectiondata.lastIndex - 1],
                infectiondata[infectiondata.lastIndex]
            )
            return if (lastThree.average() - infectiondata.average() > Options.increase) {
                ResourcesCompat.getDrawable(
                    resources,
                    no.uio.ifi.team16.stim.R.drawable.arrow_up,
                    null
                )
            } else if (infectiondata.average() - lastThree.average() > Options.decrease) {
                ResourcesCompat.getDrawable(
                    resources,
                    no.uio.ifi.team16.stim.R.drawable.arrow_down,
                    null
                )
            } else {
                return if (infectiondata.average() > Options.high) {
                    ResourcesCompat.getDrawable(
                        resources,
                        no.uio.ifi.team16.stim.R.drawable.farevarsel,
                        null
                    )
                } else {
                    ResourcesCompat.getDrawable(
                        resources,
                        no.uio.ifi.team16.stim.R.drawable.no_change,
                        null
                    )
                }
            }
        } else {
            return ResourcesCompat.getDrawable(
                resources,
                no.uio.ifi.team16.stim.R.drawable.checkmark,
                null
            )
        }
    }

}