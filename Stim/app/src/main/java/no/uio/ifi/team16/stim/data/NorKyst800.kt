package no.uio.ifi.team16.stim.data

import no.uio.ifi.team16.stim.util.*
import kotlin.math.roundToInt

/**
 * data from the NorKyst800 model. Mostly we will use stream data.
 */
data class NorKyst800(
    val depth: DoubleArray,
    val salinity: NullableDoubleArray4D,
    val temperature: NullableDoubleArray4D,
    val time: DoubleArray,
    val velocity: Triple<NullableDoubleArray4D, NullableDoubleArray4D, NullableDoubleArray4D>
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

    /**
     * Get temperature closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     */
    fun getTemperature(latLng: LatLong, time: Int, depth: Int): Double? {
        val index = getClosestIndex(latLng)
        return temperature.get(time, depth, index.first, index.second)
    }

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

    /////////////////////
    // GETTER-WRAPPERS //
    /////////////////////
    //get at "smallest" time and at surface
    fun getVelocity(latLng: LatLong) = getVelocity(latLng, 0, 0)

    //get at "smallest" time and at surface
    fun getTemperature(latLng: LatLong) = getTemperature(latLng, 0, 0)

    //get at "smallest" time and at surface
    fun getSalinity(latLng: LatLong) = getSalinity(latLng, 0, 0)

    //////////////////////
    // HELPER FUNCTIONS //
    //////////////////////
    /**
     * get the closest y-x index corresponding to a given latLng
     */
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

    ////////////////////
    // AUTO-GENERATED //
    ////////////////////
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NorKyst800

        if (!depth.contentEquals(other.depth)) return false
        if (!salinity.data.contentDeepEquals(other.salinity.data)) return false
        if (!temperature.data.contentDeepEquals(other.temperature.data)) return false
        if (!time.contentEquals(other.time)) return false
        if (velocity != other.velocity) return false
        if (projection != other.projection) return false

        return true
    }

    override fun hashCode(): Int {
        var result = depth.contentHashCode()
        result = 31 * result + salinity.data.contentDeepHashCode()
        result = 31 * result + temperature.data.contentDeepHashCode()
        result = 31 * result + time.contentHashCode()
        result = 31 * result + velocity.hashCode()
        result = 31 * result + projection.hashCode()
        return result
    }
}