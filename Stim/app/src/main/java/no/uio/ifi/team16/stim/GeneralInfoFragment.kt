package no.uio.ifi.team16.stim

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.transition.AutoTransition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.databinding.FragmentGeneralInfoBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import javax.inject.Inject

class GeneralInfoFragment : Fragment() {

    private lateinit var binding: FragmentGeneralInfoBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private var salinityChartPressed = true
    private var salinityChart = listOf<Entry>()
    private var temperatureChart = listOf<Entry>()
    private lateinit var site: Site

    @Inject
    lateinit var saltChartStyle: SalinityLineStyle

    @Inject
    lateinit var tempChartStyle: TemperatureLineStyle

    /**
     * Fragment for siden i appen som gir info om salt og vann !
     * inneholder grafer og tabeller med info.
     */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGeneralInfoBinding.inflate(inflater, container, false)

        /////////////
        //Animation//
        /////////////

        val animation =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation

        ////////
        //SITE//
        ////////

        site = viewModel.getCurrentSite() ?: return binding.root
        binding.sitename.text = site.name

        /////////////////////
        //INFORMATION BOXES//
        /////////////////////

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
                binding.infoTextExtra.setVisibility(View.GONE)
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

        ////////////////////
        //QUICK INFO GRIDS//
        ////////////////////

        //observer for å få temperatur og saltholdighet i nåtid
        viewModel.getNorKyst800Data().observe(viewLifecycleOwner) {
            it?.apply {
                binding.temperatureTextview.text =
                    getSorroundingTemperature(site.latLong, 0, 0)?.let { temp ->
                        "%4.1f".format(temp) + "°"
                    } ?: "N/A"

                binding.saltTextview.text =
                    getSorroundingSalinity(site.latLong, 0, 0)?.let { salt ->
                        "%4.1f".format(salt)
                    } ?: "N/A"
                binding.Velocitytext.text =
                    getVelocity(site.latLong, 0, 0)?.let { velocity ->
                        "%4.1f".format(velocity)
                    } ?: "N/A"
                val velocity = getVelocityDirectionInXYPlane(site.latLong, 0, 0)
                if (velocity != null) {
                    setVelocityDirection(velocity)
                }
            } ?: run {
                binding.temperatureTextview.text = "N/A"
                binding.saltTextview.text = "N/A"
                binding.Velocitytext.text = "N/A"
            }
        }

        //TOAST IF NO TEMP/SALT
        setOnTemperatureOrSalinityClickListener(binding.temperatureTextview, requireContext())
        setOnTemperatureOrSalinityClickListener(binding.saltTextview, requireContext())
        
        ///////////////////
        //CHART + BUTTONS//
        ///////////////////

        setSalinityChart()
        toggleButtonColors()

        binding.salinitychartButton.setOnClickListener {
            if (!salinityChartPressed) {
                setSalinityChart()
                toggleButtonColors()
            }
        }

        binding.tempchartButton.setOnClickListener {
            if (salinityChartPressed) {
                setTemperatureChart()
                toggleButtonColors()
            }
        }

        //////////
        //TABLES//
        //////////

        setTemperatureTable(inflater, container)
        setSalinityTable(inflater, container)

        return binding.root
    }

    companion object {
        const val CHART_LABEL_SALT = "Saltholdighet"
        const val CHART_LABEL_TEMP = "Temperatur"
    }

    /**
     * Creates the salinity chart from NordKyst800AtSiteData. takes all data from getSalinitAtSurfaceGraph().
     * Style: SalinityLineStyle
     */
    private fun setSalinityChart() {
        binding.salinityChart.visibility = View.VISIBLE
        binding.watertempChart.visibility = View.GONE

        saltChartStyle = SalinityLineStyle(requireContext())

        viewModel.getNorKyst800AtSiteData(site).observe(viewLifecycleOwner) {
            it?.apply {
                //set chart
                salinityChart = it.getSalinityAtSurfaceAsGraph()
            }

            if (salinityChart.isNotEmpty()) {

                val linedatasetSalinity =
                    LineDataSet(salinityChart, CHART_LABEL_SALT)

                binding.salinityChart.apply {
                    xAxis.apply {
                        valueFormatter = TimeValueFormatter()
                        //find hours from 1970 to now
                        val currentHour = Instant.now().epochSecond.toFloat() / 3600
                        addLimitLine(LimitLine(currentHour, "nåtid"))
                        setDrawLimitLinesBehindData(true)
                        moveViewToX(currentHour) //start at current time, showing values after
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
            }
        }
    }

    /**
     * Creates the temperature chart from NordKyst800AtSiteData. takes all data from getTemperatureAtSurfaceGraph().
     * Style: TemperatureLineStyle
     */
    private fun setTemperatureChart() {
        binding.salinityChart.visibility = View.GONE
        binding.watertempChart.visibility = View.VISIBLE

        tempChartStyle = TemperatureLineStyle(requireContext())

        viewModel.getNorKyst800AtSiteData(site).observe(viewLifecycleOwner) {
            it?.apply {
                temperatureChart = it.getTemperatureAtSurfaceAsGraph()
            }

            if (temperatureChart.isNotEmpty()) {
                val linedataset =
                    LineDataSet(temperatureChart, CHART_LABEL_TEMP)

                binding.watertempChart.apply {
                    axisLeft.apply {
                        valueFormatter = TempValueFormatter()
                    }
                    xAxis.apply {
                        valueFormatter = TimeValueFormatter()
                        //find hours from 1970 to now
                        val currentHour = Instant.now().epochSecond.toFloat() / 3600
                        addLimitLine(LimitLine(currentHour, "nåtid"))
                        setDrawLimitLinesBehindData(true)
                        moveViewToX(currentHour) //start at current time, showing values after
                    }
                }
                //style linedataset
                tempChartStyle.styleLineDataSet(linedataset, requireContext())
                binding.watertempChart.data = LineData(linedataset)
                binding.watertempChart.invalidate()

                tempChartStyle.styleChart(binding.watertempChart)

                binding.salinityChartHeader.text = "Graf over vanntemperatur"
                salinityChartPressed = false
            }
        }
    }

    /**
     * Creates the temperature table from NordKyst800AtSiteData. takes all data from getTemperatureAtSurfaceGraph().
     * Uses same layout as the infection table.
     */
    private fun setTemperatureTable(inflater: LayoutInflater, container: ViewGroup?) {
        viewModel.getNorKyst800AtSiteData(site).observe(viewLifecycleOwner) {
            it?.apply {
                binding.tablelayout.removeAllViews()
                val tempgraphdata = getTemperatureAtSurfaceAsGraph()
                for (i in 0..23) {
                    val x = tempgraphdata[i].x
                    val y = tempgraphdata[i].y
                    val newRow = TableRow(requireContext())
                    val view = inflater.inflate(R.layout.infection_table_row, container, false)
                    view.findViewById<TextView>(R.id.table_display_week).text =
                        convertTime(x)
                    if (!y.toString().contains("NaN")) {
                        view.findViewById<TextView>(R.id.table_display_float).text =
                            String.format("%.4f°", y)
                    } else {
                        view.findViewById<TextView>(R.id.table_display_float).text =
                            "Ingen data tilgjengelig"
                    }
                    view.layoutParams = TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    newRow.addView(view)
                    binding.tablelayout.addView(newRow, i)
                    newRow.layoutParams = TableLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    binding.tablelayout.requestLayout()
                }
            }
        }
    }

    /**
     * Creates the salinity table from NordKyst800AtSiteData. takes all data from getSalinityAtSurfaceGraph().
     * Uses same layout as the infection table.
     */

    private fun setSalinityTable(inflater: LayoutInflater, container: ViewGroup?) {
        viewModel.getNorKyst800AtSiteData(site).observe(viewLifecycleOwner) {
            it?.apply {
                val saltgraphdata = getSalinityAtSurfaceAsGraph()
                binding.Salttablelayout.removeAllViews()

                for (i in 0..23) {
                    val x = saltgraphdata[i].x
                    val y = saltgraphdata[i].y
                    val newRow = TableRow(requireContext())
                    val view = inflater.inflate(R.layout.infection_table_row, container, false)
                    view.findViewById<TextView>(R.id.table_display_week).text =
                        convertTime(x)
                    if (!y.toString().contains("NaN")) {
                        view.findViewById<TextView>(R.id.table_display_float).text =
                            y.toString()
                    } else {
                        view.findViewById<TextView>(R.id.table_display_float).text =
                            "Ingen data tilgjengelig"
                    }
                    view.layoutParams = TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    newRow.addView(view)
                    binding.Salttablelayout.addView(newRow, i)
                    newRow.layoutParams = TableLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                binding.Salttablelayout.requestLayout()
            }
        }
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

    /**
     * make toasts explaining certain textvalues of the salinity or temperature textview.
     */
    private fun setOnTemperatureOrSalinityClickListener(textView: TextView, context: Context) {
        textView.setOnClickListener {
            val txt = it as TextView
            if (txt.text == "N/A") {
                Toast.makeText(
                    context,
                    "Ingen data i nærliggende områder",
                    Toast.LENGTH_LONG
                ).show()
            }
            if (txt.text == "---") {
                Toast.makeText(
                    context,
                    "Ingen data lastet inn ennå",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun convertTime(value: Float): String {
        val dateformat = SimpleDateFormat("HH")
        val currentTime = Calendar.getInstance(Locale.getDefault()).time.time
        val valueDate = Date(currentTime + (3600000 * value).toLong())
        return dateformat.format(valueDate) + ":00"
    }

    //TODO - does this work??
    private fun setVelocityDirection(direction: Float) {
        var degrees = (1 + direction / Math.PI) * 180
        binding.VelocityDirectionArrow.rotation = degrees.toFloat()
    }
}