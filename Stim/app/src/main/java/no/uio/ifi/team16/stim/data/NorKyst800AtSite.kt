package no.uio.ifi.team16.stim.data

import com.github.mikephil.charting.data.Entry
import no.uio.ifi.team16.stim.util.DoubleArray2D
import no.uio.ifi.team16.stim.util.DoubleArray4D
import no.uio.ifi.team16.stim.util.NullableDoubleArray4D
import no.uio.ifi.team16.stim.util.Options
import no.uio.ifi.team16.stim.util.get

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
    val aggregation: (DoubleArray2D) -> Double = { arr -> meanAggregation(arr) }

    /////////////////
    // AGGREGATORS //
    /////////////////
    /**
     * return max value of a 2D array
     */
    private fun maxAggregation(array: DoubleArray2D): Double =
        array.maxOf { concentrationRow ->
            concentrationRow.maxOf { concentration ->
                concentration ?: 0.0
            }
        }

    /**
     * return mean value of a 2D array
     * TODO: nulls count towards mean
     */
    private fun meanAggregation(array: DoubleArray2D): Double =
        array.fold(0.0) { sum, concentrationRow ->
            sum + concentrationRow.fold(0.0) { rowSum, concentration ->
                concentration?.let {
                    rowSum + it
                } ?: rowSum
            } / concentrationRow.size
        } / array.size

    /**
     * return sum of a 2D array
     * TODO: nulls count towards mean
     */
    private fun sumAggregation(array: DoubleArray2D): Double =
        array.fold(0.0) { sum, concentrationRow ->
            sum + concentrationRow.fold(0.0) { rowSum, concentration ->
                concentration?.let {
                    rowSum + it
                } ?: rowSum
            }
        }


    ///////////////
    // UTILITIES //
    ///////////////
    override fun toString() =
        "NorKyst800AtSite: \n" +
                "\tsite: $siteId\n" +
                "\tnorkyst: $norKyst800\n"

    fun getTemperature(): Double? = getTemperature(0, 0, 0, 0)
    fun getTemperature(y: Int, x: Int): Double? = getTemperature(0, 0, y, x)
    fun getTemperature(depth: Int, time: Int, y: Int, x: Int): Double? =
        norKyst800.temperature.get(depth, time, radius + y, radius + x)
            ?: averageOf(depth, time, norKyst800.temperature)

    fun getSalinity(): Double? = getSalinity(0, 0, 0, 0)
    fun getSalinity(y: Int, x: Int): Double? = getSalinity(0, 0, y, x)
    fun getSalinity(depth: Int, time: Int, y: Int, x: Int): Double? =
        norKyst800.salinity.get(depth, time, radius + y, radius + x)
            ?: averageOf(depth, time, norKyst800.salinity)

    /**
     * return a graph(List of Entry) of salinity over time(hours)
     */
    fun getSalinityAtSurfaceAsGraph(): List<Entry> =
        norKyst800.time.zip(
            norKyst800.salinity
                .first() //get at surface
                .map { arr -> //for each latlong grid at a given time
                    aggregation(arr) //apply aggregation
                }
        ).map { (hour, salt) -> //we have List<Pair<...>> make it into List<Entry>
            Entry(hour.toFloat(), salt.toFloat())
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
                    Entry(hour.toFloat(), salt.toFloat())
                }
            }

    /**
     * return a graph(List of Entry) of salinity over time(hours)
     */
    fun getTemperatureAtSurfaceAsGraph(): List<Entry> =
        norKyst800.time.zip(
            norKyst800.temperature
                .first() //get at surface
                .map { arr -> //for each latlong grid at a given time
                    aggregation(arr) //apply aggregation
                }
        ).map { (hour, temp) -> //we have List<Pair<...>> make it into List<Entry>
            Entry(hour.toFloat(), temp.toFloat())
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
                    Entry(hour.toFloat(), temp.toFloat())
                }
            }

    ///////////////
    // UTILITIES //
    ///////////////
    /**
     * take out all non-null, then average them
     * TODO WRONG!!!
     */
    fun averageOf(depth: Int, time: Int, arr: NullableDoubleArray4D): Double = arr.data[depth][time]
        .flatMap { row -> row.toList() }
        .filterNotNull()
        .let { elements ->
            elements.fold(0.0) { acc, element ->
                acc + element
            } / elements.count()
        }
}