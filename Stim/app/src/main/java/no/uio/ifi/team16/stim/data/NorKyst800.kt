package no.uio.ifi.team16.stim.data

import no.uio.ifi.team16.stim.util.*
import kotlin.math.roundToInt

/**
 * data from the NorKyst800 model. Mostly we will use stream data.
 */
data class NorKyst800(
    val depth: DoubleArray,
    val salinity: DoubleArray4D,
    val temperature: DoubleArray4D,
    val time: DoubleArray,
    val velocity: Triple<DoubleArray4D, DoubleArray4D, DoubleArray4D>
) {
    val TAG = "NORKYST800"
    val projection = Options.defaultProjection()
    //////////////////////
    // GETTER FUNCTIONS //
    //////////////////////
    /**
     * Get salinity closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     */
    fun getSalinity(latLng: LatLong, time: Int, depth: Int): Double? {
        val index = getClosestIndex(latLng)
        return salinity.get(time, depth, index.first, index.second)
    }

    //wrapper, get at "smallest" time and at surface
    fun getSalinity(latLng: LatLong) = getSalinity(latLng, 0, 0)

    /**
     * Get temperature closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     */
    fun getTemperature(latLng: LatLong, time: Int, depth: Int): Double? {
        val index = getClosestIndex(latLng)
        return temperature.get(time, depth, index.first, index.second)
    }

    //wrapper, get at "smallest" time and at surface
    fun getTemperature(latLng: LatLong) = getTemperature(latLng, 0, 0)

    /**
     * Get velocity in all three directions(xyz) closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     */
    fun getVelocity(latLng: LatLong, time: Int, depth: Int): Triple<Double?, Double?, Double?> {
        val index = getClosestIndex(latLng)
        return Triple(
            velocity.first.get(time, depth, index.first, index.second),
            velocity.second.get(time, depth, index.first, index.second),
            velocity.third.get(time, depth, index.first, index.second)
        )
    }

    //wrapper, get at "smallest" time and at surface
    fun getVelocity(latLng: LatLong) = getVelocity(latLng, 0, 0)

    //////////////////////
    // HELPER FUNCTIONS //
    //////////////////////
    private fun getClosestIndex(latLng: LatLong): Pair<Int, Int> =
        projection.project(latLng).let { (yf, xf) ->
            Pair(
                (yf / (800 * Options.defaultNorKyst800YStride)).roundToInt(),
                (xf / (800 * Options.defaultNorKyst800XStride)).roundToInt()
            )
        }

    ///////////////
    // UTILITIES //
    ///////////////
    override fun toString() =
        "NorKyst800: \n" +
                "\tdepth: $depth\n" +
                "\ttime: $time\n" +
                "\tsalinity: ${salinity.prettyPrint()}" +
                "\ttemperature: ${temperature.prettyPrint()}" +
                "\tvelocity.x: ${velocity.first.prettyPrint()}" +
                "\tvelocity.y: ${velocity.second.prettyPrint()}" +
                "\tvelocity.z: ${velocity.third.prettyPrint()}"
}