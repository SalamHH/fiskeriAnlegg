package no.uio.ifi.team16.stim.data

import no.uio.ifi.team16.stim.util.FloatArray2D


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
    val concentrations: Array<Pair<Int, FloatArray2D>>, //array of 2D arrays of concentration and their associated week
    val concentrationShape: Pair<Int, Int>,             //shape of each 2D array. Can be inferred from concentrations, but unsafe
    val dx: Float,                                      //separation between points in x-direction
    val dy: Float                                       //separation between points in y-direction, usually dx
) {
    val TAG = "InfectiousPressureTimeSeries"

    //how concentrations at a given time are aggregated to a single float
    val aggregation: (FloatArray2D) -> Float = { arr -> meanAggregation(arr) }

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
     */
    fun getConcentration(index: Int): Float? =
        concentrations.getOrNull(index)?.second?.let { arr ->
            aggregation(arr)
        }

    /**
     * get concentration(aggregated) at all times
     *
     * includes time
     */
    fun getAllConcentrations(): Array<Pair<Int, Float>> = mapOverTime(aggregation)

    /**
     * get concentration(aggregated) at all times
     *
     * includes time in a separate array
     */
    fun getAllConcentrationsUnzipped(): Pair<Array<Int>, Array<Float>> =
        mapOverTime(aggregation).unzip().let { (timeList, concentrationList) ->
            Pair(
                timeList.toTypedArray(),
                concentrationList.toTypedArray()
            )
        }

    ///////////////
    // UTILITIES //
    ///////////////
    override fun toString() =
        "InfectiousPressureTimeSeries:" +
                concentrations.fold("\n") { accTotal, (date, concentration2D) ->
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
     */
    private fun <T> mapOverTime(reduction: (FloatArray2D) -> T): Array<Pair<Int, T>> =
        concentrations.map { (week, arr) ->
            Pair(week, reduction(arr))
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