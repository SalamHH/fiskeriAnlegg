package no.uio.ifi.team16.stim

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineDataSet
import javax.inject.Inject

class SparkLineStyle @Inject constructor(private val context: Context) {

    /***
     * stylizes the chart
     */
    // will be moved to its own file using Hilt Dependency I think
    fun styleChart(lineChart: LineChart) = lineChart.apply {
        axisRight.isEnabled = false

        axisLeft.apply {
            isEnabled = false
            axisMinimum = 0f //to avoid clipping from bezier curve
            axisMaximum = 10f //must be overwritten later!
        }

        xAxis.apply {
            //axisMinimum = 0f
            //axisMaximum = 24f
            isGranularityEnabled = true
            granularity = 1f
            setDrawGridLines(false)
            setDrawAxisLine(false)
            position = XAxis.XAxisPosition.BOTTOM
            typeface = ResourcesCompat.getFont(context, R.font.montserrat_bold)
            textSize = 12F
        }

        setTouchEnabled(true)
        isDragEnabled = true
        setScaleEnabled(false)
        setPinchZoom(true)
        isDoubleTapToZoomEnabled = true

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
        setCircleColor(ContextCompat.getColor(context, R.color.black))
        mode = LineDataSet.Mode.CUBIC_BEZIER

        setDrawFilled(true)
        fillDrawable = ContextCompat.getDrawable(context, R.drawable.backgr_spark_line)

    }
}