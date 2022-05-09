package no.uio.ifi.team16.stim

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineDataSet
import javax.inject.Inject


/**
 * Class defining how a chart should be styled in the Spark Line Style
 */
class SalinityLineStyle @Inject constructor(private val context: Context) {

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
        }

        xAxis.apply {
            setDrawGridLinesBehindData(true)
            isGranularityEnabled = true
            granularity = 1f
            setDrawGridLines(true)
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

        animateY(1500, Easing.EaseInOutCubic) // animates the chart line
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