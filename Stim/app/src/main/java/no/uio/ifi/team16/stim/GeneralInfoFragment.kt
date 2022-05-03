package no.uio.ifi.team16.stim

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
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
import no.uio.ifi.team16.stim.util.Options
import java.lang.Math.round
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.roundToInt


class GeneralInfoFragment : Fragment() {

    private lateinit var binding: FragmentGeneralInfoBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private var salinityChartPressed = false
    private var salinityChart = listOf<Entry>()
    private var temperatureChart = listOf<Entry>()

    @Inject
    lateinit var saltChartStyle: GeneralLineStyle

    @Inject
    lateinit var tempChartStyle: TemperatureLineStyle

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
            if (!salinityChartPressed) {
                setSalinityChart()
            }
        }

        binding.tempchartButton.setOnClickListener {
            if (salinityChartPressed) {
                setTemperatureChart()
            }
        }

        //TABLE

        viewModel.getNorKyst800AtSiteData(viewModel.getCurrentSite()).observe(viewLifecycleOwner) {
            it?.apply {
                binding.tablelayout.removeAllViews()

                for (i in Options.norKyst800AtSiteTimeRange.first..Options.norKyst800AtSiteTimeRange.last) {
                    if (!getTemperature(0, i, 0, 0).toString().contains("NaN")) {
                        val newRow = TableRow(requireContext())
                        val view = inflater.inflate(R.layout.infection_table_row, container, false)
                        view.findViewById<TextView>(R.id.table_display_week).text =
                            getTime(i).toString()
                        view.findViewById<TextView>(R.id.table_display_float).text =
                            getTemperature(0, i, 0, 0).toString()
                        view.layoutParams = TableRow.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        newRow.addView(view)
                        binding.tablelayout.addView(newRow, 0)
                        newRow.layoutParams = TableLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        Log.d(TAG, "Row added: $i")
                    }
                    binding.tablelayout.requestLayout()
                }
            }
        }
        viewModel.getNorKyst800AtSiteData(viewModel.getCurrentSite()).observe(viewLifecycleOwner) {
            it?.apply {

                binding.Salttablelayout.removeAllViews()

                for (i in Options.norKyst800AtSiteTimeRange.first..Options.norKyst800AtSiteTimeRange.last) {
                    if (!getSalinity(0, i, 0, 0).toString().contains("NaN")) {
                        val newRow = TableRow(requireContext())
                        val view = inflater.inflate(R.layout.infection_table_row, container, false)
                        view.findViewById<TextView>(R.id.table_display_week).text =
                            getTime(i).toString()
                        view.findViewById<TextView>(R.id.table_display_float).text =
                            getSalinity(0, i, 0, 0).toString()
                        view.layoutParams = TableRow.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        newRow.addView(view)
                        binding.Salttablelayout.addView(newRow, 0)
                        newRow.layoutParams = TableLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        Log.d(TAG, "Row added: $i")
                    }
                }
                binding.Salttablelayout.requestLayout()
            }
        }

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

        saltChartStyle = GeneralLineStyle(requireContext())

        viewModel.getNorKyst800AtSiteData(viewModel.getCurrentSite()).observe(viewLifecycleOwner) {
            it?.apply {
                //set chart
                salinityChart = it.getSalinityAtSurfaceAsGraph()
            }

            val salinityData = salinityChart.map { entry ->
                entry.y
            }

            if (salinityChart.isNotEmpty()) {

                val linedatasetSalinity =
                    LineDataSet(salinityChart, CHART_LABEL_SALT)

                binding.salinityChart.apply {
                    axisLeft.apply {
                        axisMaximum =
                            (salinityData.maxOf { v -> v } + 1) //clipping might still occurr
                    }
                }
                //style linedataset
                saltChartStyle.styleLineDataSet(linedatasetSalinity, requireContext())
                binding.salinityChart.data = LineData(linedatasetSalinity)
                binding.salinityChart.notifyDataSetChanged()
                binding.salinityChart.invalidate()

                saltChartStyle.styleChart(binding.salinityChart)

                binding.salinityChartHeader.text = "Graf over saltholdighet"
                salinityChartPressed = true
                toggleButtonColors()
            }
        }
    }

    private fun setTemperatureChart() {
        binding.salinityChart.visibility = View.GONE
        binding.watertempChart.visibility = View.VISIBLE

        tempChartStyle = TemperatureLineStyle(requireContext())

        viewModel.getNorKyst800AtSiteData(viewModel.getCurrentSite()).observe(viewLifecycleOwner) {
            it?.apply {
                temperatureChart = it.getTemperatureAtSurfaceAsGraph()
            }
            //val hourList = arrayOf(1.0, 2.0, 3,0, 4.0, 5.0, 6.0, 7.0, 8.0)
            val temperatureData = temperatureChart.map { entry ->
                entry.y
            }

            if (temperatureChart.isNotEmpty()) {
                val linedataset =
                    LineDataSet(temperatureChart, CHART_LABEL_TEMP)

                binding.watertempChart.apply {
                    axisLeft.apply {
                        axisMaximum =
                            (temperatureData.maxOf { v -> v } + 1).toFloat() //clipping might still occurr
                    }
                }
                //style linedataset

                tempChartStyle.styleLineDataSet(linedataset, requireContext())
                binding.watertempChart.data = LineData(linedataset)
                binding.watertempChart.invalidate()

                tempChartStyle.styleChart(binding.watertempChart)

                binding.salinityChartHeader.text = "Graf over vanntemperatur"
                salinityChartPressed = false
                toggleButtonColors()
            }
        }
    }

    private fun setSalttable() {

    }
}