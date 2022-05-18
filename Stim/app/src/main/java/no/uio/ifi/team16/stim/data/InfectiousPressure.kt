package no.uio.ifi.team16.stim.data

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.heatmaps.WeightedLatLng
import no.uio.ifi.team16.stim.util.*
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate
import kotlin.math.round
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
    val dx: Float, //x separation, in projection meters, between points
    val dy: Float  //y separation, in projection meters, between points
) {
    val TAG = "InfectiousPressure"

    /**
     * get cconcentration at the given latlong coordinate
     * concentration is specified in grids, and we cannot simply find the concentration at the "closest"
     * latitude longitude(because the closest latitude longitude is hard to find due to the curvature
     * of earth), so we first map the latlng to eta and xi(points on the projection) from which we
     * can find the closest projection coordinate easier(there is no curvature, so finding closest is "easy")
     *
     * concentration is defined inside a bounded grid, so coordinates outside this will not return
     * valid results
     * TODO: decide appropriate output, null? closest border concentration?
     *
     * @param latLong latitude-longitude coordinate we want to find concentration at
     * @return concentration at specified lat long coordinate
     */
    fun getConcentration(latLong: LatLong): Float? {
        val (row, column) = getClosestIndex(latLong)
        return concentration.get(row, column)
    }

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

        //Log.d(TAG, "${screenBound.northeast.asLatLong()} to ${projection.project(LatLong(northEastIndex.first.toDouble(), northEastIndex.second.toDouble()))}")

        //find "distance" on screen
        val height = (southWestIndex.first - northEastIndex.first)
        val width = (northEastIndex.second - southWestIndex.second)
        Log.d(
            TAG,
            "NE: ${screenBound.northeast}, SW: ${screenBound.southwest}\\NEi: ${northEastIndex}, SWi: ${southWestIndex}\\, HEIGHT: ${height}, WIDTH: ${width}"
        )
        //we want height/chunks = num nodes =>

        val maxX = Math.min(northWestIndex.second.roundToInt(), Options.norKyst800XEnd - 1)
        val minX = Math.max(southEastIndex.second.roundToInt(), 0)
        val maxY = Math.min(northEastIndex.first.roundToInt(), Options.norKyst800YEnd - 1)
        val minY = Math.max(southWestIndex.first.roundToInt(), 0)

        val xStride = 1 //Math.max(1, (width / Options.heatMapResolution).roundToInt())
        val yStride = 1 //Math.max(1, (height / Options.heatMapResolution).roundToInt())

        val xRange = IntProgression.fromClosedRange(minX, maxX, n) //
        val yRange = IntProgression.fromClosedRange(minY, maxY, n) //
        Log.d(
            TAG,
            "MAKING HEATMAP ${xRange.first} - ${xRange.last} | ${xRange.step} ::::: ${yRange.first} - ${yRange.last} | ${yRange.step}"
        )

        val dx = Options.defaultNorKyst800XStride * 800
        val dy = Options.defaultNorKyst800YStride * 800
        //Log.d(TAG, "making salinity-heatmap")
        return concentration.get(xRange, yRange)
            .flatMapIndexed { x, row -> //get at surface(0), current time.flatMapIndexed { y, row ->
                row.mapIndexed { y, entry ->
                    //Log.d(TAG, entry.toString())
                    //og.d(TAG, entry.toString())
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
    /**
     * get the index in the dataset that is closest to the given latlong.
     * The latlong is in one of the squares of the grid, and the indexes are
     * the gridpoints closest to that latlong in the grid-cell
     */
    private fun getClosestIndex(latLong: LatLong): Pair<Int, Int> {
        //map latLng to projection coordinates(eta, xi)
        val (eta, xi) = project(latLong)
        //divide by length between points, then round to get correct index
        return Pair(round(eta / dy).toInt(), round(xi / dx).toInt())
    }

    /**
     * project a latlng point to a point on the projection.
     * in the thredds dataset, maps from latlong to eps, xi.
     */
    fun project(latLong: LatLong): Pair<Float, Float> =
        ProjCoordinate(0.0, 0.0).let { p ->
            projection.transform(ProjCoordinate(latLong.lng, latLong.lat), p)
        }.let { p ->
            Pair(p.y.toFloat(), p.x.toFloat())
        }

    override fun toString() = "InfectiousPressure:" +
            "\nTime since 2000-01-01: ${time}, GridMapping: -----" +
            "\nConcentration:\n" +
            concentration.prettyPrint()
}