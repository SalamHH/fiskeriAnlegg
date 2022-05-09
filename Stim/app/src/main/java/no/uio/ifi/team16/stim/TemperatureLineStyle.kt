package no.uio.ifi.team16.stim

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import javax.inject.Inject

/**
 * Class defining how a chart should be styled in the Spark Line Style
 */
class TemperatureLineStyle @Inject constructor(private val context: Context) {

    /***
     * stylizes the chart
     */
    fun styleChart(lineChart: LineChart) = lineChart.apply {
        axisRight.isEnabled = false

        axisLeft.apply {
            isEnabled = true
            setDrawGridLines(false)
            setDrawAxisLine(false)
            isGranularityEnabled = true
            granularity = 0.5f
            //axisMinimum = 0f //to avoid clipping from bezier curve
            //axisMaximum = 20f //must be overwritten later!
        }

        xAxis.apply {
            //axisMinimum = 0f
            //axisMaximum = 100f
            isGranularityEnabled = true
            granularity = 1f
            setDrawGridLines(false)
            setDrawAxisLine(false)
            position = XAxis.XAxisPosition.BOTTOM
            typeface = ResourcesCompat.getFont(context, R.font.montserrat_bold)
            textSize = 12F
            setVisibleXRange(6f, 24f) //at least 6 but at most 24 points shown at any time
            moveViewToX(0f) //start at current time, showing values after
        }

        // following section defines how the user may interact with the chart
        setScaleEnabled(true) // enables the user to zoom in and out
        setTouchEnabled(true)
        isDragEnabled = true
        setPinchZoom(true)
        isDoubleTapToZoomEnabled = true

        description = null // description is not necessary

        legend.isEnabled = false // legend is not necessary

        animateY(1000, Easing.Linear) // animates the chart line

        this.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            /**
             * Called when a value has been selected inside the chart.
             *
             * @param e The selected Entry.
             * @param h The corresponding highlight object that contains information
             * about the highlighted position
             */
            override fun onValueSelected(e: Entry, h: Highlight) {

            }

            /**
             * Called when nothing has been selected or an "un-select" has been made.
             */
            override fun onNothingSelected() {

            }
        })
    }

    /***
     * stylizes the chart line
     */
    fun styleLineDataSet(lineDataSet: LineDataSet, context: Context) = lineDataSet.apply {
        color = ContextCompat.getColor(context, R.color.chartlinecolor)
        valueTextColor = ContextCompat.getColor(context, R.color.black)
        setDrawValues(false)
        lineWidth = 3f
        isHighlightEnabled = true
        setDrawHighlightIndicators(false)
        setDrawCircles(true)
        setCircleColor(ContextCompat.getColor(context, R.color.chartlinecolor))
        mode = LineDataSet.Mode.CUBIC_BEZIER

        setDrawFilled(true)
        fillDrawable = ContextCompat.getDrawable(context, R.drawable.backgr_spark_line)

    }
}