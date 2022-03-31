package no.uio.ifi.team16.stim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.transition.TransitionInflater
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
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

        viewModel.lineDataSet.observe(viewLifecycleOwner) {
            binding.infectionChart.data = LineData(it)
            binding.infectionChart.invalidate()
        }

        styleChart(binding.infectionChart)

        return binding.root

    }

    fun styleChart(lineChart: LineChart) = lineChart.apply {
        axisRight.isEnabled = false

        axisLeft.apply {
            isEnabled = false
            axisMinimum = 0f
            axisMaximum = 10f
        }

        xAxis.apply {
            axisMinimum = 0f
            axisMaximum = 24f
            isGranularityEnabled = true
            granularity = 4f
            setDrawGridLines(true)
            position = XAxis.XAxisPosition.BOTTOM
        }

        setTouchEnabled(true)
        isDragEnabled = true
        setScaleEnabled(false)
        setPinchZoom(false)

        description = null

        legend.isEnabled = false
    }
}