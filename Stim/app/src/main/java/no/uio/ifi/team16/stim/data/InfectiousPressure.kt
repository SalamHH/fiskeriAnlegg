package no.uio.ifi.team16.stim.data

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.heatmaps.WeightedLatLng
import no.uio.ifi.team16.stim.util.*
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

//typealias FloatArray2D = Array<FloatArray>
//typealias FloatArray3D = Array<FloatArray2D>

/**
 * Class representing infectious pressure over a grid at a given time.
 */
class InfectiousPressure(
    val concentration: FloatArray2D, //Particle concentration, aggregated number of particles in grid cell
    val time: Float,               //seconds since 2000-01-01 00:00:00
    val projection: CoordinateTransform, //transforms between latlong and projection coordinates
) {
    fun getHeatMapData(screenBound: LatLngBounds, n: Int): List<WeightedLatLng> {
        //make inverse projection, mapping from grid indexes to latlng
        val ctFactory = CoordinateTransformFactory()
        val stereoCRT = projection.targetCRS
        val latLngCRT = projection.sourceCRS
        val inverseProjection = ctFactory.createTransform(stereoCRT, latLngCRT)

        val northWest = LatLong(
            screenBound.northeast.asLatLong().lat,
            screenBound.southwest.asLatLong().lng
        )
        val southEast = LatLong(
            screenBound.southwest.asLatLong().lat,
            screenBound.northeast.asLatLong().lng
        )

        //find indexes of screenbound
        val northEastIndex = projection.project(screenBound.northeast.asLatLong()).let { (x, y) ->
            Pair(x / 800, y / 800)
        }
        val southWestIndex = projection.project(screenBound.southwest.asLatLong()).let { (x, y) ->
            Pair(x / 800, y / 800)
        }
        val northWestIndex = projection.project(northWest).let { (x, y) ->
            Pair(x / 800, y / 800)
        }
        val southEastIndex = projection.project(southEast).let { (x, y) ->
            Pair(x / 800, y / 800)
        }

        val maxX = min(northWestIndex.second.roundToInt(), Options.norKyst800XEnd - 1)
        val minX = max(southEastIndex.second.roundToInt(), 0)
        val maxY = min(northEastIndex.first.roundToInt(), Options.norKyst800YEnd - 1)
        val minY = max(southWestIndex.first.roundToInt(), 0)

        val xRange = IntProgression.fromClosedRange(minX, maxX, n)
        val yRange = IntProgression.fromClosedRange(minY, maxY, n)

        val dx = Options.defaultNorKyst800XStride * 800
        val dy = Options.defaultNorKyst800YStride * 800
        return concentration.get(xRange, yRange)
            .flatMapIndexed { x, row -> //get at surface(0), current time.flatMapIndexed { y, row ->
                row.mapIndexed { y, entry ->
                    WeightedLatLng(
                        inverseProjection.projectXY(
                            Pair(
                                (yRange.first + yRange.step * y) * dy.toFloat(),  //from index to meters along gridproj
                                (xRange.first + xRange.step * x) * dx.toFloat()
                            )
                        ).let { latLong ->
                            LatLng(latLong.lat, latLong.lng)
                        },
                        entry.toDouble()
                    )
                }
            }
    }


    ///////////////
    // UTILITIES //
    ///////////////
    override fun toString() = "InfectiousPressure:" +
            "\nTime since 2000-01-01: ${time}, GridMapping: -----" +
            "\nConcentration:\n" +
            concentration.prettyPrint()
}