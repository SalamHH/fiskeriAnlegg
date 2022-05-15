package no.uio.ifi.team16.stim.data

import com.github.mikephil.charting.data.Entry
import no.uio.ifi.team16.stim.util.NullableFloatArray2D
import no.uio.ifi.team16.stim.util.NullableFloatArray4D
import no.uio.ifi.team16.stim.util.Options
import no.uio.ifi.team16.stim.util.get
import java.time.Instant
import kotlin.math.roundToInt

/**
 * Class representing NorKyst800 data at a specific site.
 *
 * Note that this norkyst, contrary to the "general one" is relatively indexed when asking for temperature etc
 * That is, if you ask for getTempperature(x=-1,y=-1) you ge the temperature in the grid cell to the
 * SOUTHWEST of the site.
 */
data class NorKyst800AtSite(
    val siteId: Int,
    val norKyst800: NorKyst800
) {
    val TAG = "NORKYST800AtSite"

    val radius = Options.norKyst800AtSiteRadius

    //how concentrations at a given time are aggregated to a single float
    val aggregation: (NullableFloatArray2D) -> Float? = { arr ->
        meanAggregation(arr.flatten().filterNotNull().toFloatArray())
    }

    /////////////////
    // AGGREGATORS //
    /////////////////
    /**
     * return max value of a 2D array
     */
    private fun maxAggregation(array: FloatArray): Float? =
        if (array.isEmpty())
            null
        else
            array.maxOf { i -> i }

    /**
     * return mean value of a 2D array
     */
    private fun meanAggregation(array: FloatArray): Float? =
        if (array.isEmpty())
            null
        else
            array.sum() / array.size

    /**
     * return sum of a 2D array
     */
    private fun sumAggregation(array: FloatArray): Float? =
        if (array.isEmpty())
            null
        else
            array.sum()

    ///////////////
    // UTILITIES //
    ///////////////
    private fun getCurrentTimeIndex() =
        ((Instant.now().epochSecond.toFloat() - norKyst800.time[0]) / 3600).roundToInt()


    override fun toString() =
        "NorKyst800AtSite: \n" +
                "\tsite: $siteId\n" +
                "\tnorkyst: $norKyst800\n"

    fun getTemperature(): Float? = getTemperature(getCurrentTimeIndex(), 0, 0, 0)
    fun getTemperature(y: Int, x: Int): Float? = getTemperature(getCurrentTimeIndex(), 0, y, x)
    fun getTemperature(time: Int, depth: Int, y: Int, x: Int): Float? =
        norKyst800.temperature.get(time, depth, radius + y, radius + x)
            ?: averageOf(time, depth, norKyst800.temperature)

    fun getSalinity(): Float? = getSalinity(getCurrentTimeIndex(), 0, 0, 0)
    fun getSalinity(y: Int, x: Int): Float? = getSalinity(getCurrentTimeIndex(), 0, y, x)
    fun getSalinity(time: Int, depth: Int, y: Int, x: Int): Float? =
        norKyst800.salinity.get(time, depth, radius + y, radius + x)
            ?: averageOf(time, depth, norKyst800.salinity)

    /**
     * return a graph(List of Entry) of salinity over time(hours)
     */
    fun getSalinityAtSurfaceAsGraph(): List<Entry> =
        norKyst800.time.zip(
            norKyst800.salinity
                .mapNotNull { arr -> //for each latlong grid at a given time
                    aggregation(arr.first()) //apply aggregation at surface
                }
        ).map { (seconds, salt) -> //we have List<Pair<...>> make it into List<Entry>
            //also map seconds to hours
            Entry(seconds / 3600, salt)
        }

    /**
     * return a graph(List of Entry) of salinity over time(hours)
     */
    fun getTemperatureAtSurfaceAsGraph(): List<Entry> =
        norKyst800.time.zip(
            norKyst800.temperature
                .mapNotNull { arr -> //for each latlong grid at a given time
                    aggregation(arr.first()) //apply aggregation at surface
                }
        ).map { (seconds, temp) -> //we have List<Pair<...>> make it into List<Entry>
            //also map seconds to hours
            Entry(seconds / 3600, temp)
        }

    ///////////////
    // UTILITIES //
    ///////////////
    /**
     * take out all non-null, then average them.
     *
     * Used when getters find null, so we get all sorrounnding entries and average them to get a meaningful result
     */
    private fun averageOf(time: Int, depth: Int, arr: NullableFloatArray4D): Float? =
        arr[time][depth]
            .flatMap { row -> row.toList() } //flatten
            .filterNotNull() //take out null
            .let { elements -> //with the flattened array of non-null values
                if (elements.isEmpty()) {
                    return null
                } else {
                    elements.fold(0f) { acc, element -> //sum all
                        acc + element
                    } / elements.size //divide by amount of elements
                }
            }
}