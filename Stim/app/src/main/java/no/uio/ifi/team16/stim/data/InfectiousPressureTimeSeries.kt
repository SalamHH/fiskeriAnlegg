package no.uio.ifi.team16.stim.data

import com.github.mikephil.charting.data.Entry
import no.uio.ifi.team16.stim.util.FloatArray2D
import no.uio.ifi.team16.stim.util.FloatArray3D

/**
 * Class representing infectious pressure over a grid at a given time.
 *
 * note the aggregation-function. Since infectiousPressuretimeseries is not just a single grid cell over time,
 * but several neighbors, the aggregate function makes all the cells at a single moment in time into
 * a single datapoint.
 *
 * In most cases the most appropriate type of aggregation might be mean, max or sum.
 * currently using sum
 */
data class InfectiousPressureTimeSeries(
    val siteId: Int,
    val concentrations: FloatArray3D,        //array of 2D arrays of concentration around site
    val weeks: Array<Int>,                  //array of week numbers
    val concentrationShape: Pair<Int, Int>,  //shape of each 2D array. Can be inferred from concentrations, but unsafe
    val dx: Float,                           //separation between points in x-direction
    val dy: Float                            //separation between points in y-direction, usually dx
) {
    val TAG = "InfectiousPressureTimeSeries"

    //how concentrations at a given time are aggregated to a single float
    val aggregation: (FloatArray2D) -> Float = { arr -> meanAggregation(arr) }

    init {
        //infer y-x shape
        val shape: Pair<Int, Int> = Pair(
            concentrations.firstOrNull()?.size
                ?: -1, //if null, there are no data entries, and shape cannot be inferred!
            concentrations.firstOrNull()?.firstOrNull()?.size ?: 0
        )
    }

    /////////////////
    // AGGREGATORS //
    /////////////////
    /**
     * return max value of a 2D array
     */
    private fun maxAggregation(array: FloatArray2D): Float =
        array.maxOf { concentrationRow ->
            concentrationRow.maxOf { concentration ->
                concentration
            }
        }

    /**
     * return mean value of a 2D array
     */
    private fun meanAggregation(array: FloatArray2D): Float =
        array.fold(0f) { sum, concentrationRow ->
            sum + concentrationRow.fold(0f) { rowSum, concentration ->
                rowSum + concentration
            } / concentrationRow.size
        } / array.size

    /**
     * return sum of a 2D array
     */
    private fun sumAggregation(array: FloatArray2D): Float =
        array.fold(0f) { sum, concentrationRow ->
            sum + concentrationRow.fold(0f) { rowSum, concentration ->
                rowSum + concentration
            }
        }

    /////////////////////////
    // CONVENIENCE-GETTERS //
    /////////////////////////
    /**
     * get concentration(aggregated) at a given index, 0 being the most recent
     *
     * nullable to return null when out of bounds.
     */
    fun getConcentration(index: Int): Float? =
        concentrations.getOrNull(index)?.let { arr ->
            aggregation(arr)
        }

    /**
     * get concentration(aggregated) at all times
     *
     * includes time in a separate array
     */
    fun getAggregatedConcentrationsAndTime(): Pair<Array<Int>, Array<Float>> = Pair(
        weeks,
        mapOverTime(aggregation)
    )

    /**
     * return a graph(List of Entry) of concentration over time(weeks)
     */
    fun getConcentrationsAsGraph(): List<Entry> =
        weeks.zip(
            concentrations
                .map { arr -> //for each latlong grid at a given time
                    aggregation(arr) //apply aggregation
                }
        ).map { (week, conc) -> //we have List<Pair<...>> make it into List<Entry>
            Entry(week.toFloat(), conc)
        }.reversed()

    ///////////////
    // UTILITIES //
    ///////////////
    override fun toString() =
        "InfectiousPressureTimeSeries:" +
                weeks.zip(concentrations).fold("\n") { accTotal, (date, concentration2D) ->
                    accTotal + "week %2d: [".format(date) +
                            concentration2D.fold("") { accGrid, concentrationRow ->
                                accGrid + concentrationRow.fold("[") { accRow, concentration ->
                                    accRow + "%5.2f, ".format(concentration)
                                } + "], "
                            } +
                            "] -> " +
                            "%9.7f".format(aggregation(concentration2D)) +
                            "\n"
                }

    /**
     * map each arrayFloat2D over time
     * reified and inlined so that T can be inferred(in toTypedArray)
     */
    private inline fun <reified T> mapOverTime(reduction: (FloatArray2D) -> T): Array<T> =
        concentrations.map { arr ->
            reduction(arr)
        }.toTypedArray()

    ////////////////////
    // AUTO-GENERATED //
    ////////////////////
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InfectiousPressureTimeSeries

        if (siteId != other.siteId) return false

        return true
    }

    override fun hashCode(): Int {
        return siteId
    }
}