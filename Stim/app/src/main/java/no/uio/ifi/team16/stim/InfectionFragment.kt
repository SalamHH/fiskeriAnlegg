package no.uio.ifi.team16.stim

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.transition.TransitionInflater
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import no.uio.ifi.team16.stim.databinding.FragmentInfectionBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import javax.inject.Inject

class InfectionFragment : StimFragment() {

    private val TAG = "INFECTIONFRAGMENT"

    private lateinit var binding: FragmentInfectionBinding
    private val viewModel: MainActivityViewModel by activityViewModels()

    @Inject
    lateinit var chartStyle: SparkLineStyle


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "oncreate")
        binding = FragmentInfectionBinding.inflate(inflater, container, false)

        val animation =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation

        val site = viewModel.getCurrentSite()
        Log.d(TAG, "site is $site")

        chartStyle = SparkLineStyle(requireContext())

        viewModel.loadInfectiousPressureTimeSeriesAtSite(site)
        //val contamData = mutableListOf<Entry>()
        //val _lineDataSet = MutableLiveData(LineDataSet(contamData, CHART_LABEL))

        viewModel.getInfectiousPressureTimeSeriesData().observe(viewLifecycleOwner) {
            Log.d(TAG, "TIMESERIES CHANGED! site id: " + site.id)
            Log.d(TAG, "TO: " + it[site.id].toString())
            //TODO: handle site not loaded(ie null) - we get nullpointerexception if we go back, and try to load inf from another site,
            //since site seems to update, but loading the concentrations does not? the get fails since there is no id for this site in the map
            it[site.id]?.getAllConcentrationsUnzipped()?.also { (weekList, infectionData) ->
                val linedataset = LineDataSet(
                    weekList.zip(infectionData)                 // list med par av x og y
                        .map { (x, y) -> Entry(x.toFloat(), y) }, //list med Entry(x,y)
                    CHART_LABEL
                )
                //set max of yaxis to max of loaded dataset
                //THE BEZIER CURVE DOES NOT CONSERVE MIN / MAX OF INTERPOLATED POINTS, SO IT WILL CLIP!!
                //TODO get interpolation(CUBIC BEZIER), and find min max of that, or change to linear(not bezier), or use max+1 min-1
                binding.infectionChart.apply {
                    axisLeft.apply {
                        axisMaximum =
                            infectionData.maxOf { v -> v } + 1f //clipping might still occurr
                    }
                }
                //style linedataset
                chartStyle.styleLineDataSet(linedataset, requireContext())
                binding.infectionChart.data = LineData(linedataset)
                binding.infectionChart.invalidate()
            }
        }

        chartStyle.styleChart(binding.infectionChart)

        return binding.root
    }

    companion object {
        const val CHART_LABEL = "INFECTION_CHART"
    }

}