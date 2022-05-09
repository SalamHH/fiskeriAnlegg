package no.uio.ifi.team16.stim

import android.util.Log
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class TimeValueFormatter : ValueFormatter() {

    private val dateformat = SimpleDateFormat("HH")
    private val millisecInAnHour = 3600000

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val currentTime = Calendar.getInstance(Locale.getDefault()).time.time
        val valueDate = Date(currentTime + (millisecInAnHour * value).toLong())
        return dateformat.format(valueDate) + ":00"
    }

    // override this for e.g. LineChart or ScatterChart
    override fun getPointLabel(entry: Entry?): String {
        return String.format("%.0f:00", entry?.y)
    }

}