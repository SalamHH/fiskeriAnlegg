package no.uio.ifi.team16.stim.data

import com.github.mikephil.charting.data.Entry
import no.uio.ifi.team16.stim.util.NullableFloatArray2D
import no.uio.ifi.team16.stim.util.NullableFloatArray4D
import no.uio.ifi.team16.stim.util.Options
import no.uio.ifi.team16.stim.util.get
import java.time.Instant
import java.time.temporal.ChronoUnit
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
    val aggregation: (NullableFloatArray2D) -> Float = { arr ->
        meanAggregation(arr.flatten().filterNotNull().toFloatArray())
    }

    /////////////////
    // AGGREGATORS //
    /////////////////
    /**
     * return max value of a 2D array
     */
    private fun maxAggregation(array: FloatArray): Float = array.maxOf { i -> i }

    /**
     * return mean value of a 2D array
     */
    private fun meanAggregation(array: FloatArray): Float = array.sum() / array.size

    /**
     * return sum of a 2D array
     */
    private fun sumAggregation(array: FloatArray): Float = array.sum()

    ///////////////
    // UTILITIES //
    ///////////////
    override fun toString() =
        "NorKyst800AtSite: \n" +
                "\tsite: $siteId\n" +
                "\tnorkyst: $norKyst800\n"

    fun getTemperature(): Float? = getTemperature(0, 0, 0, 0)
    fun getTemperature(y: Int, x: Int): Float? = getTemperature(0, 0, y, x)
    fun getTemperature(depth: Int, time: Int, y: Int, x: Int): Float? =
        norKyst800.temperature.get(depth, time, radius + y, radius + x)
            ?: averageOf(depth, time, norKyst800.temperature)

    fun getSalinity(): Float? = getSalinity(0, 0, 0, 0)
    fun getSalinity(y: Int, x: Int): Float? = getSalinity(0, 0, y, x)
    fun getSalinity(depth: Int, time: Int, y: Int, x: Int): Float? =
        norKyst800.salinity.get(depth, time, radius + y, radius + x)
            ?: averageOf(depth, time, norKyst800.salinity)

    /**
     * return a graph(List of Entry) of salinity over time(hours)
     */
    fun getSalinityAtSurfaceAsGraph(): List<Entry> =
        norKyst800.time.zip(
            norKyst800.salinity
                .map { arr -> //for each latlong grid at a given time
                    aggregation(arr.first()) //apply aggregation at surface
                }
        ).map { (seconds, salt) -> //we have List<Pair<...>> make it into List<Entry>
            //also map seconds from dat to hours from now
            //seconds is seconds since 1970-01-01 00:00:00
            val secondsFrom1970ToNow = Instant.EPOCH.until(Instant.now(), ChronoUnit.SECONDS)
            Entry(((seconds - secondsFrom1970ToNow) / 3600).roundToInt().toFloat(), salt)
        }

    /**
     * return a list of graphs(list of entries) where each entry is a graph of salinity
     * at the depth indicated by index
     */
    fun getSalinitiesAsGraphs(): List<List<Entry>> =
        norKyst800.salinity
            .map { salinityOverTime -> //for each depth
                salinityOverTime.map { salinity -> //for each latlong grid at a given time
                    aggregation(salinity) //apply aggregation
                }.zip(
                    norKyst800.time.toList()
                ).map { (hour, salt) -> //we have List<Pair<...>> make it into List<Entry>
                    Entry(hour, salt)
                }
            }

    /**
     * return a graph(List of Entry) of salinity over time(hours)
     */
    fun getTemperatureAtSurfaceAsGraph(): List<Entry> =
        norKyst800.time.zip(
            norKyst800.temperature
                .map { arr -> //for each latlong grid at a given time
                    aggregation(arr.first()) //apply aggregation at surface
                }
        ).map { (seconds, temp) -> //we have List<Pair<...>> make it into List<Entry>
            //also map seconds from dat to hours from now
            //seconds is seconds since 1970-01-01 00:00:00
            val secondsFrom1970ToNow = Instant.EPOCH.until(Instant.now(), ChronoUnit.SECONDS)
            Entry(((seconds - secondsFrom1970ToNow) / 3600).roundToInt().toFloat(), temp)
        }

    /**
     * return a list of graphs(list of entries) where each entry is a graph of salinity
     * at the depth indicated by index
     */
    fun getTemperaturesAsGraphs(): List<List<Entry>> =
        norKyst800.temperature
            .map { temperatureOverTime -> //for each depth
                temperatureOverTime.map { temperature -> //for each latlong grid at a given time
                    aggregation(temperature) //apply aggregation
                }.zip(
                    norKyst800.time.toList()
                ).map { (hour, temp) -> //we have List<Pair<...>> make it into List<Entry>
                    Entry(hour, temp)
                }
            }

    ///////////////
    // UTILITIES //
    ///////////////
    /**
     * take out all non-null, then average them.
     *
     * Used when getters find null, so we get all sorrounnding entries and average them to get a meaningful result
     */
    private fun averageOf(depth: Int, time: Int, arr: NullableFloatArray4D): Float =
        arr[depth][time]
            .flatMap { row -> row.toList() } //flatten
            .filterNotNull() //take out null
            .let { elements -> //with the flattened array of non-null values
                elements.fold(0f) { acc, element -> //sum all
                    acc + element
                } / elements.size //divide by amount of elements
            }
}