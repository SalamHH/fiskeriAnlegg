package no.uio.ifi.team16.stim.data

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.heatmaps.WeightedLatLng
import no.uio.ifi.team16.stim.util.*
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.ranges.IntProgression.Companion.fromClosedRange

/**
 * data from the NorKyst800 model.
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
    val currentHour =
        0 //TODO: make into current hour relative to dataset, remember that the time(which maps onto first axis of every dataset)
    //TODO: is seconds from 1980-00-00. see hours in the norkyst salinity and temp graph for example how to get.
    //////////////////////
    // GETTER FUNCTIONS //
    //////////////////////
    /**
     * Get salinity closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     */
    private fun getSalinity(latLng: LatLong, time: Int, depth: Int): Float? {
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
            .firstNotNullOfOrNull { radius -> //take out first value in lazy-sequnce returning not-null
                averageOf(salinity[time][depth].getSorrounding(y, x, radius))
            }
    }

    /**
     * Get temperature closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     */
    private fun getTemperature(latLng: LatLong, time: Int, depth: Int): Float? {
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
                averageOf(temperature[time][depth].getSorrounding(y, x, radius))
            }
    }

    fun getSorroundingVelocity(latLng: LatLong, time: Int, depth: Int): Float? {
        val (x, y) = getClosestIndex(latLng) //TODO: I might have flipped x-y
        return (0..Options.norKyst800MaxRadius) //for each possible radius
            .asSequence() //do as sequence, ie evaluae lazily
            .firstNotNullOfOrNull { radius -> //take out first value in lazy-sequnce returning not-null
                val u = averageOf(velocity.first[time][depth].getSorrounding(y, x, radius))
                val v = averageOf(velocity.second[time][depth].getSorrounding(y, x, radius))
                if (u == null || v == null) {
                    null
                } else {
                    Math.sqrt((u * u + v * v).toDouble()).toFloat()
                }
            }
    }

    fun getTemperatureHeatMapData(screenBound: LatLngBounds): List<WeightedLatLng> {
        //make inverse projection, mapping from grid indexes to latlng TODO: move outside
        val ctFactory = CoordinateTransformFactory()
        val stereoCRT = projection.targetCRS
        val latLngCRT = projection.sourceCRS
        val inverseProjection = ctFactory.createTransform(stereoCRT, latLngCRT)

        //find indexes of screenbound
        val northEastIndex = projection.project(screenBound.northeast.asLatLong())
        val southWestIndex = projection.project(screenBound.southwest.asLatLong())

        val maxX = Math.min(northEastIndex.second.roundToInt(), Options.norKyst800XEnd)
        val minX = Math.max(southWestIndex.second.roundToInt(), 0)
        val maxY = Math.min(northEastIndex.first.roundToInt(), Options.norKyst800YEnd)
        val minY = Math.max(southWestIndex.first.roundToInt(), 0)

        val xStride = Math.max(1, ((maxX - minX) / Options.heatMapResolution).roundToInt())
        val yStride = Math.max(1, ((maxY - minY) / Options.heatMapResolution).roundToInt())

        val xRange = fromClosedRange(minX, maxX, xStride) //
        val yRange = fromClosedRange(minY, maxY, yStride) //
        Log.d(
            TAG,
            "MAKING HEATMAP ${xRange.first} - ${xRange.last} | ${xRange.step} ... ${yRange.first} - ${yRange.last} | ${yRange.step}"
        )


        val dx = Options.defaultNorKyst800XStride * 800
        val dy = Options.defaultNorKyst800YStride * 800
        return temperature[currentHour][0].get(yRange, xRange)
            .flatMapIndexed { y, row -> //get at surface(0), current time.flatMapIndexed { y, row ->
                row.filterNotNull()
                    .mapIndexed { x, entry ->
                        WeightedLatLng(
                            inverseProjection.projectXY(
                                Pair(
                                    (xRange.first + xRange.step * x) * (dx).toFloat(), //from index to meters along gridproj
                                    (yRange.first + yRange.step * y) * (dy.toFloat())  //from index to meters along gridproj
                                )
                            ).let { latLong ->
                                LatLng(latLong.lat, latLong.lng)
                            },
                            entry.toDouble()
                        )
                    }
            }
    }

    fun getSalinityHeatMapData(screenBound: LatLngBounds): List<WeightedLatLng> {
        //make inverse projection, mapping from grid indexes to latlng TODO: move outside
        val ctFactory = CoordinateTransformFactory()
        val stereoCRT = projection.targetCRS
        val latLngCRT = projection.sourceCRS
        val inverseProjection = ctFactory.createTransform(stereoCRT, latLngCRT)

        //find indexes of screenbound
        val northEastIndex = projection.project(screenBound.northeast.asLatLong())
        val southWestIndex = projection.project(screenBound.southwest.asLatLong())

        val maxX = Math.min(northEastIndex.second.roundToInt(), Options.norKyst800XEnd)
        val minX = Math.max(southWestIndex.second.roundToInt(), 0)
        val maxY = Math.min(northEastIndex.first.roundToInt(), Options.norKyst800YEnd)
        val minY = Math.max(southWestIndex.first.roundToInt(), 0)

        val xStride = Math.max(1, ((maxX - minX) / Options.heatMapResolution).roundToInt())
        val yStride = Math.max(1, ((maxY - minY) / Options.heatMapResolution).roundToInt())

        val xRange = fromClosedRange(minX, maxX, xStride) //
        val yRange = fromClosedRange(minY, maxY, yStride) //
        Log.d(
            TAG,
            "MAKING HEATMAP ${xRange.first} - ${xRange.last} | ${xRange.step} ... ${yRange.first} - ${yRange.last} | ${yRange.step}"
        )

        val dx = Options.defaultNorKyst800XStride * 800
        val dy = Options.defaultNorKyst800YStride * 800
        Log.d(TAG, "making salinity-heatmap")
        return salinity[currentHour][0].get(yRange, xRange)
            .flatMapIndexed { y, row -> //get at surface(0), current time.flatMapIndexed { y, row ->
                row.filterNotNull()
                    .mapIndexed { x, entry ->
                        Log.d(TAG, entry.toString())
                        WeightedLatLng(
                            inverseProjection.projectXY(
                                Pair(
                                    (xRange.first + xRange.step * x) * (dx).toFloat(), //from index to meters along gridproj
                                    (yRange.first + yRange.step * y) * (dy.toFloat())  //from index to meters along gridproj
                                )
                            ).let { latLong ->
                                LatLng(latLong.lat, latLong.lng)
                            },
                            entry.toDouble()
                        )
                    }
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

    fun getSorroundingVelocityVector(
        latLng: LatLong,
        time: Int,
        depth: Int
    ): Triple<Float, Float, Float>? {
        val (x, y) = getClosestIndex(latLng) //TODO: I might have flipped x-y
        return (0..Options.norKyst800MaxRadius) //for each possible radius
            .asSequence() //do as sequence, ie evaluae lazily
            .firstNotNullOfOrNull { radius -> //take out first value in lazy-sequnce returning not-null
                val u = averageOf(velocity.first[time][depth].getSorrounding(y, x, radius))
                val v = averageOf(velocity.second[time][depth].getSorrounding(y, x, radius))
                val w = averageOf(velocity.third[time][depth].getSorrounding(y, x, radius))
                if (u == null || v == null || w == null) {
                    null
                } else {
                    Triple(
                        u,
                        v,
                        w
                    )
                }
            }
    }

    //return velocity in m/s
    fun getVelocity(latLng: LatLong, time: Int, depth: Int): Float? =
        getSorroundingVelocityVector(latLng, time, depth)?.let { (x, y, z) ->
            sqrt(x * x + y * y + z * z)
        }

    //return direction along the XYplane(water surface) in radians

    fun getVelocityDirectionInXYPlane(latLng: LatLong, time: Int, depth: Int): Float? =
        getSorroundingVelocityVector(latLng, time, depth)?.let { (x, y, z) ->
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
        arr.flatMap { row ->
            row.toList()
        } //flatten
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

    //join two datasets at time dimension. Assumess all other dimensions of equal size
    fun append(other: NorKyst800): NorKyst800 =
        NorKyst800(
            depth,
            salinity.plus(other.salinity),
            temperature.plus(other.temperature),
            time.plus(other.time),
            Triple(
                velocity.first.plus(other.velocity.first),
                velocity.second.plus(other.velocity.second),
                velocity.third.plus(other.velocity.third)
            ),
            projection
        )

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