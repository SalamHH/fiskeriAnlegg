package no.uio.ifi.team16.stim

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
import androidx.transition.AutoTransition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import no.uio.ifi.team16.stim.data.BarentsWatchAtSite
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.databinding.FragmentInfectionBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import no.uio.ifi.team16.stim.util.InfectionStatusCalculator
import java.time.ZonedDateTime
import javax.inject.Inject


class InfectionFragment : StimFragment() {

    private val TAG = "INFECTIONFRAGMENT"

    private lateinit var binding: FragmentInfectionBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var site: Site

    @Inject
    lateinit var chartStyle: InfectionLineStyle

    /**
     * Fragment that displays infection of salmonlouse using a graph and a table.
     * Also contains information on infection of ILA and PD.
     */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "oncreate")
        binding = FragmentInfectionBinding.inflate(inflater, container, false)

        //ANIMATION

        val animation =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation

        //SITE

        site = viewModel.getCurrentSite() ?: return binding.root
        binding.sitename.text = site.name

        chartStyle = InfectionLineStyle()

        //EXPANDABLE INFORMATION CARD

        binding.InformationCard.setOnClickListener {
            if (binding.infoTextExtra.visibility == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(
                    binding.InformationCard,
                    AutoTransition()
                )
                binding.infoTextExtra.visibility = View.GONE
                binding.arrow.setImageResource(R.drawable.down_darkblue)
            } else {
                TransitionManager.beginDelayedTransition(
                    binding.InformationCard,
                    AutoTransition()
                )
                binding.infoTextExtra.visibility = View.VISIBLE
                binding.arrow.setImageResource(R.drawable.up_darkblue)
            }
        }

        viewModel.getInfectiousPressureTimeSeriesData(site).observe(viewLifecycleOwner) {
            it?.let { inf ->
                //write current infectioninfo to texgtview
                binding.infectionValue.text =
                    getString(R.string.currentInfection_format).format(inf.getCurrentConcentration())

                //make a graph when historical data is ready
                inf.observeConcentrationsGraph(viewLifecycleOwner) { graph ->
                    val infectionData =
                        graph.map { xy -> xy.y }.toTypedArray() //get contamination as separate list
                    val weekList = graph.map { xy -> xy.x } //get weeks as separate list

                    //TABLE
                    createInfectionTable(infectionData, weekList, inflater, container)

                    //CHART
                    createInfectionChart(graph, infectionData)

                    //STATUS
                    val statuscalculator = InfectionStatusCalculator(resources)
                    if (infectionData.isNotEmpty()) {
                        binding.infectionStatusText.text =
                            statuscalculator.calculateInfectionStatusText(infectionData)
                        if (statuscalculator.calculateInfectionStatusIcon(infectionData) != null) {
                            binding.StatusIcon.setImageDrawable(
                                statuscalculator.calculateInfectionStatusIcon(
                                    infectionData
                                )
                            )
                            binding.StatusIcon.contentDescription =
                                statuscalculator.calculateInfectionStatusText(infectionData)
                        }
                    } else {
                        binding.infectionStatusText.text =
                            resources.getText(R.string.no_infectiouspressure_found)
                    }
                }
            }
        }

        binding.StatusIcon.setOnClickListener {
            val newFragment = InfectiousIconStatusDialogFragment()
            newFragment.show(parentFragmentManager, "statusicons")
        }

        calculateBarentsWatchInfection()

        return binding.root
    }

    private fun createInfectionChart(graph: List<Entry>, infectionData: Array<Float>) {
        val linedataset = LineDataSet(
            graph,
            CHART_LABEL
        )
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
        chartStyle.styleLineDataSet(linedataset, requireContext())
        binding.infectionChart.data = LineData(linedataset)
        binding.infectionChart.invalidate()
        chartStyle.styleChart(binding.infectionChart)
    }

    private fun createInfectionTable(
        infectionData: Array<Float>,
        weekList: List<Float>,
        inflater: LayoutInflater,
        container: ViewGroup?
    ) {
        for (i in infectionData.indices) {
            val newRow = TableRow(requireContext())
            val view = inflater.inflate(R.layout.infection_table_row, container, false)
            view.findViewById<TextView>(R.id.table_display_week).text =
                String.format("%.0f", weekList[i])
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
    }

    private fun calculateBarentsWatchInfection() {
        viewModel.getBarentsWatchData(site).observe(viewLifecycleOwner) {
            if (it?.listPD?.isNotEmpty() == true) {
                binding.pdIcon.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.pd_bad,
                        null
                    )
                )
                binding.suspicionUnknowndate.text =
                    checkIfDataExistAndFormat("pd_ukjent_mistankedato", it)
                        ?: resources.getText(R.string.no_suspicion)
                binding.suspicionSav2Date.text =
                    checkIfDataExistAndFormat("pd_sav2_mistankedato", it)
                        ?: resources.getText(R.string.no_suspicion)
                binding.suspicionSav3Date.text =
                    checkIfDataExistAndFormat("pd_sav3_mistankedato", it)
                        ?: resources.getText(R.string.no_suspicion)
                binding.provenUnknown.text = checkIfDataExistAndFormat("pd_ukjent_paavistdato", it)
                    ?: resources.getText(R.string.not_proven)
                binding.provenSav2Date.text = checkIfDataExistAndFormat("pd_sav2_paavistdato", it)
                    ?: resources.getText(R.string.not_proven)
                binding.provenSav3Date.text = checkIfDataExistAndFormat("pd_sav3_paavistdato", it)
                    ?: resources.getText(R.string.not_proven)
            }

            if (it?.listILA?.isNotEmpty() == true) {
                binding.ilaIcon.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ila_bad,
                        null
                    )
                )
                binding.mistankeDato.text = checkIfDataExistAndFormat("mistankedato", it)
                binding.paavistDato.text = checkIfDataExistAndFormat("paavistdato", it)
            }
        }
    }

    private fun format(time: ZonedDateTime?): String {
        val day = time?.dayOfMonth.toString()
        val month = time?.month.toString()
        val year = time?.year.toString()
        return "$day $month $year"
    }


    private fun checkIfDataExistAndFormat(
        input: String,
        barrentsdata: BarentsWatchAtSite
    ): String? {
        if (barrentsdata.listPD[input] != "null") {
            val time = ZonedDateTime.parse(barrentsdata.listPD[input])
            return format(time)
        }
        return null
    }

    companion object {
        const val CHART_LABEL = "INFECTION_CHART"
    }

}