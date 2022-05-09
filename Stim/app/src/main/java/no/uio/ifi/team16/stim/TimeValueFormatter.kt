package no.uio.ifi.team16.stim

import android.util.Log
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TimeValueFormatter : ValueFormatter() {

    private val formatter = DateTimeFormatter.ofPattern("HH")

    //Format("HH")
    private val secInAnHour = 60 * 60

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val valueDate =
            Instant.ofEpochSecond(secInAnHour * value.toLong()).atZone(ZoneId.systemDefault())
        Log.d("TMFORMATTER", valueDate.format(formatter))
        return valueDate.format(formatter) + ":00"
    }

    // override this for e.g. LineChart or ScatterChart
    override fun getPointLabel(entry: Entry?): String {
        return String.format("%.0f:00", entry?.y)
    }

}