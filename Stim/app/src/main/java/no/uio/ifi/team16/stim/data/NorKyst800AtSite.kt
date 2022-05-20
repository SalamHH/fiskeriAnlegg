package no.uio.ifi.team16.stim.data

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.github.mikephil.charting.data.Entry
import no.uio.ifi.team16.stim.util.NullableFloatArray2D
import no.uio.ifi.team16.stim.util.NullableFloatArray4D
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
    val current: NorKyst800,
    val all: LiveData<NorKyst800?>
) {
    companion object {
        const val TAG = "NORKYST800AtSite"
    }

    private val radius = Options.norKyst800AtSiteRadius

    //how concentrations at a given time are aggregated to a single float
    private val aggregation: (NullableFloatArray2D) -> Float? = { arr ->
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
    override fun toString() =
        "NorKyst800AtSite: \n" +
                "\tsite: $siteId\n" +
                "\tnorkyst: $current\n"

    fun getTemperature(): Float? = getTemperature(0, 0, 0, 0)
    private fun getTemperature(time: Int, depth: Int, y: Int, x: Int): Float? =
        current.temperature.get(time, depth, radius + y, radius + x)
            ?: averageOf(time, depth, current.temperature)

    fun getSalinity(): Float? = getSalinity(0, 0, 0, 0)
    private fun getSalinity(time: Int, depth: Int, y: Int, x: Int): Float? =
        current.salinity.get(time, depth, radius + y, radius + x)
            ?: averageOf(time, depth, current.salinity)

    /**
     * return a graph(List of Entry) of salinity over time(hours)
     */
    fun observeTemperatureAtSurfaceAsGraph(
        owner: LifecycleOwner,
        action: (List<Entry>) -> Unit
    ): Unit =
        all.observe(owner) { nork ->
            //make graph data
            nork?.time?.zip(
                nork.temperature
                    .mapNotNull { arr -> //for each latlong grid at a given time
                        aggregation(arr.first()) //apply aggregation at surface
                    }
            )?.map { (seconds, temp) -> //we have List<Pair<...>> make it into List<Entry>
                //also map seconds to hours
                Entry(seconds / 3600, temp)
            }?.let { graph -> //perform action on the graph data
                action(graph)
            }
        }

    fun observeSalinityAtSurfaceAsGraph(
        owner: LifecycleOwner,
        action: (List<Entry>) -> Unit
    ): Unit =
        all.observe(owner) { nork ->
            //make graph data
            nork?.time?.zip(
                nork.salinity
                    .mapNotNull { arr -> //for each latlong grid at a given time
                        aggregation(arr.first()) //apply aggregation at surface
                    }
            )?.map { (seconds, salt) -> //we have List<Pair<...>> make it into List<Entry>
                //also map seconds to hours
                Entry(seconds / 3600, salt)
            }?.let { graph -> //perform action on the graph data
                action(graph)
            }
        }


    fun getVelocity(): Float? = getVelocity(0, 0, 0, 0)

    private fun getVelocity(time: Int, depth: Int, y: Int, x: Int): Float? {
        val u = current.velocity.first.get(time, depth, radius + y, radius + x)
            ?: averageOf(time, depth, current.velocity.first)
        val v = current.velocity.second.get(time, depth, radius + y, radius + x)
            ?: averageOf(time, depth, current.velocity.second)
        //val w = norKyst800.velocity.third.get(time, depth, radius + y, radius + x)
        //    ?: averageOf(time, depth, norKyst800.temperature) TODO add vertical component?
        return u?.let { uu ->
            v?.let { vv ->
                Math.sqrt((u * u + v * v).toDouble()).toFloat()
            }
        }
    }

    fun getVelocityDirectionInXYPlane(): Float? =
        getVelocityDirectionInXYPlane(0, 0, radius, radius)

    private fun getVelocityDirectionInXYPlane(time: Int, depth: Int, y: Int, x: Int): Float? {
        val u = current.velocity.first.get(time, depth, radius + y, radius + x)
            ?: averageOf(time, depth, current.velocity.first)
        val v = current.velocity.second.get(time, depth, radius + y, radius + x)
            ?: averageOf(time, depth, current.velocity.second)
        return u?.let { _ ->
            v?.let { _ -> //return null if any of them null
                Math.sqrt((u * u + v * v).toDouble()).toFloat()
            }
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