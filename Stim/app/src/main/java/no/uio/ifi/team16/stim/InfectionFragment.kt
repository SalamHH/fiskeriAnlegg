package no.uio.ifi.team16.stim

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.transition.TransitionInflater
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import no.uio.ifi.team16.stim.databinding.FragmentInfectionBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import no.uio.ifi.team16.stim.util.Options
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

        binding.LoadingScreen.loadingLayout.visibility = View.VISIBLE

        val animation =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation

        val site = viewModel.getCurrentSite() ?: return binding.root
        binding.sitename.text = site.name

        chartStyle = SparkLineStyle(requireContext())

        viewModel.loadInfectiousPressureTimeSeriesAtSite(site)
        //val contamData = mutableListOf<Entry>()
        //val _lineDataSet = MutableLiveData(LineDataSet(contamData, CHART_LABEL))

        viewModel.getInfectiousPressureTimeSeriesData(site).observe(viewLifecycleOwner) {
            binding.LoadingScreen.loadingLayout.visibility = View.GONE
            it?.getConcentrationsAsGraph()?.also { graph ->
                val infectionData =
                    graph.map { xy -> xy.y }.toTypedArray() //get contamination as separate list
                val weekList = graph.map { xy -> xy.x } //get weeks as separate list
                //CHART
                val linedataset = LineDataSet(
                    graph,
                    CHART_LABEL
                )
                //STATUS
                if (infectionData.isNotEmpty()) {
                    binding.infectionStatusText.text = calculateInfectionStatusText(infectionData)
                    if (calculateInfectionStatusIcon(infectionData) != null) {
                        binding.StatusIcon.setImageDrawable(
                            calculateInfectionStatusIcon(
                                infectionData
                            )
                        )
                    }
                } else {
                    binding.infectionStatusText.text = "Fant ikke smittedata"
                }
                //set max of yaxis to max of loaded dataset
                //THE BEZIER CURVE DOES NOT CONSERVE MIN / MAX OF INTERPOLATED POINTS, SO IT WILL CLIP!!
                //TODO get interpolation(CUBIC BEZIER), and find min max of that, or change to linear(not bezier), or use max+1 min-1
                binding.infectionChart.apply {
                    axisLeft.apply {
                        if (infectionData.isNotEmpty()) {
                            axisMaximum =
                                infectionData.maxOf { v -> v } + 1f //clipping might still occurr
                        }
                    }
                }
                //style linedataset
                chartStyle.styleLineDataSet(linedataset, requireContext())
                binding.infectionChart.data = LineData(linedataset)
                binding.infectionChart.invalidate()

                //TABLE
                binding.tablelayout.removeAllViews()

                for (i in 0..infectionData.lastIndex) {
                    val newRow = TableRow(requireContext())
                    val view = inflater.inflate(R.layout.infection_table_row, container, false)
                    view.findViewById<TextView>(R.id.table_display_week).text =
                        weekList[i].toString()
                    view.findViewById<TextView>(R.id.table_display_float).text =
                        infectionData[i].toString()
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

        chartStyle.styleChart(binding.infectionChart)

        binding.StatusIcon.setOnClickListener {
            val newFragment = StatusDialogFragment()
            newFragment.show(parentFragmentManager, "statusicons")
        }

        return binding.root
    }

    private fun calculateInfectionStatusText(infectiondata: Array<Float>): String {


        if (infectiondata.lastIndex > 1 && infectiondata.average() > Options.infectionExists) {
            //sjekker om det er signifikant økning/miskning på de siste 3 datapunktene
            val lastThree = arrayOf(
                infectiondata[infectiondata.lastIndex - 2],
                infectiondata[infectiondata.lastIndex - 1],
                infectiondata[infectiondata.lastIndex]
            )
            return if (lastThree.average() - infectiondata.average() > Options.increase) {
                "Signifikant økning i smitte"
            } else if (infectiondata.average() - lastThree.average() > Options.decrease) {
                "Signifikant minskning i smitte"
            } else {
                return if (infectiondata.average() > Options.high) {
                    "Høyt smittenivå"
                } else {
                    "Lavt smittenivå"
                }
            }
        } else {
            return "Veldig lav/Ingen smitte"
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

    companion object {
        const val CHART_LABEL = "INFECTION_CHART"
    }

}