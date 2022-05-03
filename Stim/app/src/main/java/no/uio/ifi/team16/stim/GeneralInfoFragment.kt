package no.uio.ifi.team16.stim

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import no.uio.ifi.team16.stim.data.StaticMapImageLoader
import no.uio.ifi.team16.stim.databinding.FragmentGeneralInfoBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import java.lang.Math.round
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.roundToInt


class GeneralInfoFragment : Fragment() {

    private lateinit var binding: FragmentGeneralInfoBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private var salinityChartPressed = false

    @Inject
    lateinit var chartStyle: GeneralLineStyle

    /**
     * Fragment for siden i appen som gir info om salt og vann
     */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGeneralInfoBinding.inflate(inflater, container, false)

        val site = viewModel.getCurrentSite() ?: return binding.root

        //SITE
        binding.sitename.text = site.name

        //Vanntemperatur og saltholdighet
        viewModel.loadNorKyst800AtSite(site)

        viewModel.getNorKyst800AtSiteData(site).observe(viewLifecycleOwner) {
            it?.apply {
                //forecast data is also available in the norkyst object! (about 66 hours, time indexes hours)
                binding.temperatureTextview.text = "%4.1f".format(getTemperature()) + "°"
                binding.saltTextview.text = "%4.1f".format(getSalinity())
            } ?: run {
                binding.temperatureTextview.text = "N/A"
                binding.saltTextview.text = "N/A"
            }
        }
        //CHART

        setSalinityChart()

        //buttons
        binding.salinitychartButton.setOnClickListener {
            if (salinityChartPressed != true) {
                setSalinityChart()
            }
        }

        binding.tempchartButton.setOnClickListener {
            if (salinityChartPressed) {
                setTemperatureChart()
            }
        }

        //posisjon
        binding.posisjonView.text = "${site.latLong.lat}, ${site.latLong.lng}"

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

        return binding.root
    }

    companion object {
        const val CHART_LABEL_SALT = "Saltholdighet"
        const val CHART_LABEL_TEMP = "Temperatur"
    }

    private fun toggleButtonColors() {
        if (salinityChartPressed) {
            binding.salinitychartButton.setBackgroundColor(
                resources.getColor(
                    R.color.darkest_skyblue,
                    null
                )
            )
            binding.tempchartButton.setBackgroundColor(
                resources.getColor(
                    R.color.dark_skyblue,
                    null
                )
            )
        } else {
            binding.salinitychartButton.setBackgroundColor(
                resources.getColor(
                    R.color.dark_skyblue,
                    null
                )
            )
            binding.tempchartButton.setBackgroundColor(
                resources.getColor(
                    R.color.darkest_skyblue,
                    null
                )
            )
        }
    }

    private fun setSalinityChart() {
        binding.salinityChart.visibility = View.VISIBLE
        binding.watertempChart.visibility = View.GONE

        chartStyle = GeneralLineStyle(requireContext())
        var salinityChart = listOf<Entry>()

        viewModel.getNorKyst800AtSiteData(viewModel.getCurrentSite()).observe(viewLifecycleOwner) {
            it?.apply {
                //set chart
                salinityChart = it.getSalinityAtSurfaceAsGraph().map { entry -> //round each y entry
                    Entry(entry.x, entry.y)
                }
            }
            Log.d("wtfman", salinityChart[0].toString())
            val testmFormat = DecimalFormat("0.00")
            Log.d("Doesiteven?", testmFormat.format(salinityChart[0].x))
            //val hourList = arrayOf(1.0, 2.0, 3,0, 4.0, 5.0, 6.0, 7.0, 8.0)
            val salinityData = salinityChart.map { entry ->
                entry.y
            }

            if (salinityChart.isNotEmpty()) {

                val linedatasetSalinity =
                    LineDataSet(salinityChart, CHART_LABEL_SALT)

                linedatasetSalinity.valueFormatter = LargeValueFormatter()

                binding.salinityChart.apply {
                    axisLeft.apply {
                        axisMaximum =
                            (salinityData.maxOf { v -> v } + 1) //clipping might still occurr
                        valueFormatter = LargeValueFormatter()
                    }
                }
                //style linedataset
                chartStyle.styleLineDataSet(linedatasetSalinity, requireContext())
                binding.salinityChart.data = LineData(linedatasetSalinity)
                binding.salinityChart.invalidate()

                chartStyle.styleChart(binding.salinityChart)

                binding.salinityChartHeader.text = "Graf over saltholdighet"
                salinityChartPressed = true
                toggleButtonColors()
            }
        }
    }

    private fun setTemperatureChart() {
        binding.salinityChart.visibility = View.GONE
        binding.watertempChart.visibility = View.VISIBLE

        chartStyle = GeneralLineStyle(requireContext())
        var temperatureChart = listOf<Entry>()

        viewModel.getNorKyst800AtSiteData(viewModel.getCurrentSite()).observe(viewLifecycleOwner) {
            it?.apply {
                //set chart
                temperatureChart = it.getSalinityAtSurfaceAsGraph()
            }
            //val hourList = arrayOf(1.0, 2.0, 3,0, 4.0, 5.0, 6.0, 7.0, 8.0)
            val temperatureData = temperatureChart.map { entry ->
                entry.y
            }

            if (temperatureChart.isNotEmpty()) {
                val linedataset =
                    LineDataSet(temperatureChart, CHART_LABEL_TEMP)

                linedataset.valueFormatter = ChartValueFormatter()

                binding.watertempChart.apply {
                    axisLeft.apply {
                        axisMaximum =
                            (temperatureData.maxOf { v -> v } + 1).toFloat() //clipping might still occurr
                        valueFormatter = ChartValueFormatter()
                    }
                    axisRight.apply {
                        valueFormatter = ChartValueFormatter()
                    }
                }
                //style linedataset

                chartStyle.styleLineDataSet(linedataset, requireContext())
                binding.watertempChart.data = LineData(linedataset)
                binding.watertempChart.invalidate()
                chartStyle.styleChart(binding.watertempChart)

                binding.salinityChartHeader.text = "Graf over vanntemperatur"
                salinityChartPressed = false
                toggleButtonColors()
            }
        }
    }
}