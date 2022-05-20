package no.uio.ifi.team16.stim.data

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
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
    val currentWeek: Int,
    val currentConcentrations: FloatArray2D,
    val historicalData:
    MutableLiveData<
            Pair<Array<Int>, FloatArray3D>
            >, //historical data, concentrations and corresponding week-numbers
    val dx: Float,                           //separation between points in x-direction
    val dy: Float                            //separation between points in y-direction, usually dx
) {
    //how concentrations at a given time are aggregated to a single float
    val aggregation: (FloatArray2D) -> Float = { arr -> meanAggregation(arr) }

    /////////////////
    // AGGREGATORS //
    /////////////////
    /**
     * return mean value of a 2D array
     */
    private fun meanAggregation(array: FloatArray2D): Float =
        array.fold(0f) { sum, concentrationRow ->
            sum + concentrationRow.fold(0f) { rowSum, concentration ->
                rowSum + concentration
            } / concentrationRow.size
        } / array.size


    /////////////////////////
    // CONVENIENCE-GETTERS //
    /////////////////////////
    /**
     * get concentration(aggregated) at a given index, 0 being the most recent
     *
     * nullable to return null when out of bounds.
     */
    fun getCurrentConcentration(): Float =
        aggregation(currentConcentrations)

    /**
     * apply an action to the graphdata when it is available
     *
     * @param owner: owner of the lifecycle
     * @param action: action to perform on graphdata(List<Entry>) WHEN the data is available
     */
    fun observeConcentrationsGraph(owner: LifecycleOwner, action: (List<Entry>) -> Unit) =
        historicalData.observe(owner) { (weeks, concentrations) ->
            action(
                mutableListOf(Entry(0f, aggregation(currentConcentrations))).apply {
                    addAll(
                        weeks.zip(
                            concentrations
                                .map { arr -> //for each latlong grid at a given time
                                    aggregation(arr) //apply aggregation
                                }
                        ).map { (week, conc) -> //we have List<Pair<...>> make it into List<Entry>
                            Entry(week.toFloat(), conc)
                        }
                    )
                }.toList()
            )
        }

    /**
     * apply an action to the concentrationdata WHEN it is available
     *
     * @param owner: owner of the lifecycle
     * @param action: action to perform on data(List<Float>) WHEN the data is available
     */
    fun observeConcentrations(owner: LifecycleOwner, action: (List<Int>, List<Float>) -> Unit) =
        historicalData.observe(owner) { (weeks, concentrations) ->
            val data = mutableListOf(aggregation(currentConcentrations))
            data.addAll(concentrations.map { aggregation(it) })
            val allWeeks = mutableListOf(0)
            allWeeks.addAll(weeks)
            action(allWeeks, data)
        }

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