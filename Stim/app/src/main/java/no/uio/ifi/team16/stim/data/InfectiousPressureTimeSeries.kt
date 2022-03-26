package no.uio.ifi.team16.stim.data

import ucar.ma2.ArrayFloat
import ucar.ma2.Index3D

/**
 * Class representing infectious pressure over a grid at a given time.
 */

data class InfectiousPressureTimeSeries(
    val siteId: Int,
    val concentrations: List<Pair<Int, ArrayFloat>>,
    val concentrationShape: Pair<Int, Int>,
    val dx: Float,
    val dy: Float
) {
    val TAG = "InfectiousPressureTimeSeries"
    var idx: Index3D = Index3D(
        intArrayOf(
            concentrations.size,
            concentrationShape.first,
            concentrationShape.second
        )
    )

    //extend arrayFloat with a getter since theirs is very impractical
    fun ArrayFloat.get(row: Int, column: Int): Float =
        this.getFloat(idx.set(0, row, column))

    override fun toString() =
        "InfectiousPressureTimeSeries:" +
                concentrations.fold("\n", { prev, (date, concentration) ->
                    prev + "${date.toString()}): ${concentration.toString()}\n"
                })
}