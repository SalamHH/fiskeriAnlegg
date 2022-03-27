package no.uio.ifi.team16.stim.data

import no.uio.ifi.team16.stim.util.IntArray4D
import no.uio.ifi.team16.stim.util.LatLng
import no.uio.ifi.team16.stim.util.get

/**
 * data from the NorKyst800 model. Mostly we will use stream data.
 */
data class NorKyst800(
    val depth: DoubleArray,
    val salinity: IntArray4D,
    val temperature: IntArray4D,
    val time: DoubleArray,
    val velocity: Triple<IntArray4D, IntArray4D, IntArray4D>
) {
    val TAG = "NORKYST800"
    //////////////////////
    // GETTER FUNCTIONS //
    //////////////////////
    /**
     * Get salinity closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     */
    fun getSalinity(latLng: LatLng, time: Int, depth: Int): Double {
        val index = getClosestIndex(latLng)
        return salinity.get(time, depth, index.first, index.second).toDouble().toDouble()
        //.get(time, depth, index.first, index.second) //TODO: WRONG! NOT SCALED
    }

    //wrapper, get at "smallest" time and at surface
    fun getSalinity(latLng: LatLng) = getSalinity(latLng, 0, 0)

    /**
     * Get temperature closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     */
    fun getTemperature(latLng: LatLng, time: Int, depth: Int): Double {
        val index = getClosestIndex(latLng)
        return temperature.get(time, depth, index.first, index.second)
            .toDouble() //TODO: WRONG! NOT SCALED
    }

    //wrapper, get at "smallest" time and at surface
    fun getTemperature(latLng: LatLng) = getTemperature(latLng, 0, 0)

    /**
     * Get velocity in all three directions(xyz) closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     */
    fun getVelocity(latLng: LatLng, time: Int, depth: Int): Triple<Double, Double, Double> {
        val index = getClosestIndex(latLng)
        return Triple(
            velocity.first.get(time, depth, index.first, index.second).toDouble(),
            velocity.second.get(time, depth, index.first, index.second).toDouble(),
            velocity.third.get(time, depth, index.first, index.second).toDouble()
        )
    }

    //wrapper, get at "smallest" time and at surface
    fun getVelocity(latLng: LatLng) = getVelocity(latLng, 0, 0)

    //////////////////////
    // HELPER FUNCTIONS //
    //////////////////////
    private fun getClosestIndex(latLng: LatLng): Pair<Int, Int> {
        return Pair(0, 0)
    }
}