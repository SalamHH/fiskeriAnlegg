package no.uio.ifi.team16.stim

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat


class ChartValueFormatter : ValueFormatter() {

    private val mFormat = DecimalFormat("0.00")

    override fun getFormattedValue(value: Float): String {
        return mFormat.format(value)
    }

}