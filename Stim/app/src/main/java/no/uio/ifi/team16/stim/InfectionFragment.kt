package no.uio.ifi.team16.stim

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.transition.TransitionInflater
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import no.uio.ifi.team16.stim.databinding.FragmentInfectionBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel

class InfectionFragment : StimFragment() {

    private lateinit var binding: FragmentInfectionBinding
    private val viewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentInfectionBinding.inflate(inflater, container, false)

        val animation = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation

        val site =  viewModel.getCurrentSite()
        val site = viewModel.getCurrentSite()

        //last inn InfectiousPressureTimeSeries for dette objektet
        viewModel.loadInfectiousPressureTimeSeriesAtSite(site)

        val contamData = mutableListOf<Entry>()
        val _lineDataSet = MutableLiveData(LineDataSet(contamData, CHART_LABEL))

        viewModel.getInfectiousPressureTimeSeriesData().observe(viewLifecycleOwner) {
            println("THE TIMESERIES OBSERVED ARE \n" + it[site.id].toString())
            val (weekList, infectionData) = it[site.id]!!.getAllConcentrationsUnzipped()

            _lineDataSet.value = LineDataSet(
                weekList.zip(infectionData)                 // list med par av x og y
                    .map { (x,y) -> Entry(x.toFloat(),y) }, //list med Entry(x,y)
                CHART_LABEL
            )

            viewModel.setLineDataSet(_lineDataSet)
        }

        viewModel.getLineDataSet().observe(viewLifecycleOwner) {
            styleLineDataSet(it, requireContext())
            binding.infectionChart.data = LineData(it)
            binding.infectionChart.invalidate()
        }

        styleChart(binding.infectionChart)

        //viewModel.loadSiteContamination()

        return binding.root

    }

    companion object {
        const val CHART_LABEL = "INFECTION_CHART"
    }

    /***
     * stylizes the chart
     */
    // will be moved to its own file using Hilt Dependency I think
    fun styleChart(lineChart: LineChart) = lineChart.apply {
        axisRight.isEnabled = false

        axisLeft.apply {
            isEnabled = false
            axisMinimum = 0f
            axisMaximum = 10f
        }

        xAxis.apply {
            //axisMinimum = 0f
            //axisMaximum = 24f
            isGranularityEnabled = true
            granularity = 4f
            setDrawGridLines(false)
            setDrawAxisLine(false)
            position = XAxis.XAxisPosition.BOTTOM
        }

        setTouchEnabled(true)
        isDragEnabled = true
        setScaleEnabled(false)
        setPinchZoom(false)

        description = null

        legend.isEnabled = false
    }

    fun styleLineDataSet(lineDataSet: LineDataSet, context: Context) = lineDataSet.apply{
        color = ContextCompat.getColor(context, R.color.white)
        valueTextColor = ContextCompat.getColor(context, R.color.black)
        setDrawValues(false)
        lineWidth = 3f
        isHighlightEnabled = true
        setDrawHighlightIndicators(false)
        setDrawCircles(true)
        mode = LineDataSet.Mode.CUBIC_BEZIER

        setDrawFilled(true)
        fillDrawable = ContextCompat.getDrawable(context, R.drawable.backgr_spark_line)

    }
}