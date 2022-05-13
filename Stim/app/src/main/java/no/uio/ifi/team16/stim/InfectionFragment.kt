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
import androidx.transition.AutoTransition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.databinding.FragmentInfectionBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import no.uio.ifi.team16.stim.util.Options
import java.time.ZonedDateTime
import javax.inject.Inject


class InfectionFragment : StimFragment() {

    private val TAG = "INFECTIONFRAGMENT"

    private lateinit var binding: FragmentInfectionBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var site: Site

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

        ////////
        //SITE//
        ////////

        site = viewModel.getCurrentSite() ?: return binding.root
        binding.sitename.text = site.name

        chartStyle = SparkLineStyle(requireContext())

        viewModel.loadInfectiousPressureTimeSeriesAtSite(site)

        binding.InformationCard.setOnClickListener {
            // If the CardView is already expanded, set its visibility
            //  to gone and change the expand less icon to expand more.
            // If the CardView is already expanded, set its visibility
            //  to gone and change the expand less icon to expand more.
            if (binding.infoTextExtra.visibility == View.VISIBLE) {

                // The transition of the hiddenView is carried out
                //  by the TransitionManager class.
                // Here we use an object of the AutoTransition
                // Class to create a default transition.
                TransitionManager.beginDelayedTransition(
                    binding.InformationCard,
                    AutoTransition()
                )
                binding.infoTextExtra.visibility = View.GONE
                binding.pil.setImageResource(R.drawable.down_darkblue)
            } else {
                TransitionManager.beginDelayedTransition(
                    binding.InformationCard,
                    AutoTransition()
                )
                binding.infoTextExtra.visibility = View.VISIBLE
                binding.pil.setImageResource(R.drawable.up_darkblue)
            }
        }

        viewModel.getInfectiousPressureTimeSeriesData(site).observe(viewLifecycleOwner) {
            it?.getConcentrationsAsGraph()?.also { graph ->
                val infectionData =
                    graph.map { xy -> xy.y }.toTypedArray() //get contamination as separate list
                val weekList = graph.map { xy -> xy.x } //get weeks as separate list

                //CREATE TABLE
                createTable(infectionData, weekList, inflater, container)

                //CHART
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
            }
        }
        chartStyle.styleChart(binding.infectionChart)

        binding.StatusIcon.setOnClickListener {
            val newFragment = StatusDialogFragment()
            newFragment.show(parentFragmentManager, "statusicons")
        }

        viewModel.getBarentsWatchData(site).observe(viewLifecycleOwner) {
            if (it?.listPD?.isNotEmpty() == true) {
                binding.pdIcon.setImageDrawable(ResourcesCompat.getDrawable(
                    resources,
                    no.uio.ifi.team16.stim.R.drawable.pd_bad,
                    null
                ))
                if (it.listPD["pd_ukjent_mistankedato"] != "null") {
                    val time = ZonedDateTime.parse(it.listPD["pd_sav3_mistankedato"])
                    binding.mistankeUkjentDato.text = format(time)
                }
                if (it.listPD["pd_sav2_mistankedato"] != "null") {
                    val time = ZonedDateTime.parse(it.listPD["pd_sav3_mistankedato"])
                    binding.mistankeSav2Dato.text = format(time)
                }
                if (it.listPD["pd_sav3_mistankedato"] != "null") {
                    val time = ZonedDateTime.parse(it.listPD["pd_sav3_mistankedato"])
                    binding.mistankeSav3Dato.text = format(time)
                }
                if (it.listPD["pd_ukjent_paavistdato"] != "null") {
                    val time = ZonedDateTime.parse(it.listPD["pd_ukjent_paavistdato"])
                    binding.paavistUkjent.text = format(time)
                }
                if (it.listPD["pd_sav2_paavistdato"] != "null") {
                    val time = ZonedDateTime.parse(it.listPD["pd_sav2_paavistdato"])
                    binding.paavistSav2Dato.text = format(time)
                }
                if (it.listPD["pd_sav3_paavistdato"] != "null") {
                    val time = ZonedDateTime.parse(it.listPD["pd_sav3_paavistdato"])
                    binding.paavistSav3Dato.text = format(time)
                }
            }

            if (it?.listILA?.isNotEmpty() == true) {
                binding.ilaIcon.setImageDrawable(ResourcesCompat.getDrawable(
                    resources,
                    no.uio.ifi.team16.stim.R.drawable.ila_bad,
                    null
                ))
                if (it.listILA["mistankedato"] != "null") {
                    val time = ZonedDateTime.parse(it.listILA["mistankedato"])
                    binding.mistankeDato.text = format(time)
                }
                if (it.listILA["paavistdato"] != "null") {
                    val time = ZonedDateTime.parse(it.listILA["paavistdato"])
                    binding.paavistDato.text = format(time)
                }
            }
        }

        return binding.root
    }

    private fun createTable(infectionData: Array<Float>, weekList: List<Float>, inflater: LayoutInflater, container: ViewGroup?) {
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



    fun format(time: ZonedDateTime?): String? {
        val day = time?.dayOfMonth.toString()
        val month = time?.month.toString()
        val year = time?.year.toString()
        return "$day $month $year"
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
                    R.drawable.arrow_up,
                    null
                )
            } else if (infectiondata.average() - lastThree.average() > Options.decrease) {
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.arrow_down,
                    null
                )
            } else {
                return if (infectiondata.average() > Options.high) {
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.farevarsel,
                        null
                    )
                } else {
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.no_change,
                        null
                    )
                }
            }
        } else {
            return ResourcesCompat.getDrawable(
                resources,
                R.drawable.checkmark,
                null
            )
        }
    }

    companion object {
        const val CHART_LABEL = "INFECTION_CHART"
    }

}