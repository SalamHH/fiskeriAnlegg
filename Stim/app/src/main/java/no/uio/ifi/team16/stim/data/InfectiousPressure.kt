package no.uio.ifi.team16.stim.data

import no.uio.ifi.team16.stim.util.FloatArray2D
import no.uio.ifi.team16.stim.util.LatLong
import no.uio.ifi.team16.stim.util.get
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.ProjCoordinate
import java.util.*
import kotlin.math.cos
import kotlin.math.round

//typealias FloatArray2D = Array<FloatArray>
//typealias FloatArray3D = Array<FloatArray2D>

/**
 * Class representing infectious pressure over a grid at a given time.
 */
class InfectiousPressure(
    val concentration: FloatArray2D, //Particle concentration, aggregated number of particles in grid cell
    val time: Float,               //seconds since 2000-01-01 00:00:00
    val projection: CoordinateTransform, //transforms between latlong and projection coordinates
    val fromDate: Date?,
    val toDate: Date?,
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

    /**
     * DEPRECATED! use InfectiousPressureTimeSeries, get from InfectiousPressureTimeSeriesRepository
     * will be removed in a future commit
     */
    @Deprecated("use InfectiousPressureTimeSeries, get from InfectiousPressureTimeSeriesRepository")
    fun getConcentration(latLong: LatLong, weeksFromNow: Int): Float {
        /*find the concentrationgrid closest to our latlongpoint,
        we use euclidean distance, or technically L1, to measure distance between latlngs.*/
        val (row, column) = getClosestIndex(latLong)
        return concentration[row][column] * cos(weeksFromNow.toFloat() / 2 * 3.141592f)
    }

    ///////////////
    // UTILITIES //
    ///////////////
    private fun getClosestIndex(latLong: LatLong): Pair<Int, Int> {
        //map latLng to projection coordinates(eta, xi)
        val (eta, xi) = project(latLong)
        //divide by length between points, then round to get correct index
        return Pair(round(eta / dy).toInt(), round(xi / dx).toInt())
    }

    /**
     * project a latlng point to a point on the projection.
     * in the thredds dataset, maps from latlong to eps, xi.
     *
     * TODO: these should really be handled py a projection class!
     */
    fun project(lat: Float, lng: Float): Pair<Float, Float> =
        ProjCoordinate(0.0, 0.0).let { p ->
            projection.transform(ProjCoordinate(lng.toDouble(), lat.toDouble()), p)
        }.let { p ->
            Pair(p.y.toFloat(), p.x.toFloat())
        }

    fun project(latLong: LatLong): Pair<Float, Float> =
        project(latLong.lat.toFloat(), latLong.lng.toFloat())

    override fun toString() = "InfectiousPressure:" +
            "\nFrom: ${fromDate}, to: $toDate" +
            "\nTime since 2000-01-01: ${time}, GridMapping: -----" +
            "\nConcentration:\n" +
            concentration.toString()
}