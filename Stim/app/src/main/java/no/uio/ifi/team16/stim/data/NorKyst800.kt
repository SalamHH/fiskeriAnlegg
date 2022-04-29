package no.uio.ifi.team16.stim.data

import android.util.Log
import no.uio.ifi.team16.stim.util.*
import org.locationtech.proj4j.CoordinateTransform
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * data from the NorKyst800 model. Mostly we will use stream data.
 */
data class NorKyst800(
    val depth: FloatArray1D,
    val salinity: NullableFloatArray4D,
    val temperature: NullableFloatArray4D,
    val time: FloatArray1D,
    val velocity: Triple<NullableFloatArray4D, NullableFloatArray4D, NullableFloatArray4D>,
    val projection: CoordinateTransform, //transforms between latlong and projection coordinates
) {
    val TAG = "NORKYST800"
    //////////////////////
    // GETTER FUNCTIONS //
    //////////////////////
    /**
     * Get salinity closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     */
    fun getSalinity(latLng: LatLong, time: Int, depth: Int): Float? {
        val (y, x) = getClosestIndex(latLng)
        return salinity.get(time, depth, y, x)
    }

    /**
     * Get salinity closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     *
     * If the salinity at the given coordinates is null, take the average of a larger area around
     * that coordinate to hopefully get something that is not null.
     */
    fun getSorroundingSalinity(latLng: LatLong, time: Int, depth: Int): Float? {
        val (x, y) = getClosestIndex(latLng) //TODO: I might have flipped x-y
        return (0..Options.norKyst800MaxRadius) //for each possible radius
            .asSequence() //do as sequence, ie evaluae lazily
            .firstNotNullOf { radius -> //take out first value in lazy-sequnce returning not-null
                averageOf(salinity[time][depth].getSorrounding(y, x, radius))
            }
    }

    /**
     * Get temperature closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     */
    fun getTemperature(latLng: LatLong, time: Int, depth: Int): Float? {
        val (y, x) = getClosestIndex(latLng)
        return temperature.get(time, depth, y, x)
    }

    /**
     * Get temperature closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     *
     * If the salinity at the given coordinates is null, take the average of a larger area around
     * that coordinate to hopefully get something that is not null.
     */
    fun getSorroundingTemperature(latLng: LatLong, time: Int, depth: Int): Float? {
        val (x, y) = getClosestIndex(latLng) //TODO: I might have flipped x-y
        return (0..Options.norKyst800MaxRadius) //for each possible radius
            .asSequence() //do as sequence, ie evaluae lazily
            .firstNotNullOfOrNull { radius -> //take out first value in lazy-sequnce returning not-null

                val v = averageOf(temperature[time][depth].getSorrounding(y, x, radius))
                Log.d(TAG, temperature[time][depth].getSorrounding(y, x, radius).prettyPrint())
                Log.d(TAG, "[$y, $x] radius ${radius}, got $v")
                v
            }
    }

    /**
     * Get velocity in all three directions(xyz) closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     */
    fun getVelocityVector(latLng: LatLong, time: Int, depth: Int): Triple<Float, Float, Float>? {
        val (y, x) = getClosestIndex(latLng)
        return Triple(
            velocity.first.get(time, depth, y, x) ?: return null,
            velocity.second.get(time, depth, y, x) ?: return null,
            velocity.third.get(time, depth, y, x) ?: return null
        )
    }

    fun getVelocity(latLng: LatLong, time: Int, depth: Int): Float? =
        getVelocityVector(latLng, time, depth)?.let { (x, y, z) ->
            sqrt(x * x + y * y + z * z)
        }

    fun getVelocityDirectionInXYPlane(latLng: LatLong, time: Int, depth: Int): Float? =
        getVelocityVector(latLng, time, depth)?.let { (x, y, z) ->
            atan2(y, x)
        }

    /////////////////////
    // GETTER-WRAPPERS //
    /////////////////////
    //get at "smallest" time and at surface
    fun getVelocity(latLng: LatLong) = getVelocity(latLng, 0, 0)

    //get at "smallest" time and at surface
    fun getVelocityDirectionInXYPlane(latLng: LatLong) = getVelocityDirectionInXYPlane(latLng, 0, 0)

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

    private fun averageOf(arr: NullableFloatArray2D): Float? =
        arr.flatMap { row -> row.toList() } //flatten
            .filterNotNull() //take out null, might return empty
            .let { elements -> //with the flattened array of non-null values
                elements.reduceOrNull { acc, element -> //sum all
                    acc + element
                }?.div(elements.size) //divide by amount of elements
            }

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
        if (!salinity.contentDeepEquals(other.salinity)) return false
        if (!temperature.contentDeepEquals(other.temperature)) return false
        if (!time.contentEquals(other.time)) return false
        if (velocity != other.velocity) return false
        if (projection != other.projection) return false

        return true
    }

    override fun hashCode(): Int {
        var result = depth.contentHashCode()
        result = 31 * result + salinity.contentDeepHashCode()
        result = 31 * result + temperature.contentDeepHashCode()
        result = 31 * result + time.contentHashCode()
        result = 31 * result + velocity.hashCode()
        result = 31 * result + projection.hashCode()
        return result
    }
}