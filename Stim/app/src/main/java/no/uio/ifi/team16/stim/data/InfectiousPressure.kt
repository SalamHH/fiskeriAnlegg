package no.uio.ifi.team16.stim.data

import no.uio.ifi.team16.stim.util.LatLng
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.ProjCoordinate
import ucar.ma2.ArrayFloat
import ucar.ma2.Index1D
import ucar.ma2.Index3D
import ucar.nc2.NCdumpW
import java.util.*
import kotlin.math.cos
import kotlin.math.round

/**
 * Class representing infectious pressure over a grid at a given time.
 */
class InfectiousPressure(
    val concentration: ArrayFloat, //Particle concentration, aggregated number of particles in grid cell
    val time: Float,               //seconds since 2000-01-01 00:00:00
    val projection: CoordinateTransform, //transforms between latlong and projection coordinates
    val fromDate: Date?,
    val toDate: Date?,
    val dx: Float, //x separation, in projection meters, between points
    val dy: Float  //y separation, in projection meters, between points
) {
    val shape: Pair<Int, Int> = Pair(concentration.shape[1], concentration.shape[2])
    var idx: Index3D = Index3D(concentration.shape)
    var ideta: Index1D = Index1D(intArrayOf(concentration.shape[1]))
    var idxi: Index1D = Index1D(intArrayOf(concentration.shape[2]))

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
     * @param latLng latitude-longitude coordinate we want to find concentration at
     * @return concentration at specified lat long coordinate
     */
    fun getConcentration(latLng: LatLng): Float {
        val index = getClosestIndex(latLng)
        return concentration.get(index.first, index.second)
    }

    fun getConcentration(latLng: LatLng, weeksFromNow: Int): Float {
        /*find the concentrationgrid closest to our latlongpoint,
        we use euclidean distance, or technically L1, to measure distance between latlngs.*/
        val index = getClosestIndex(latLng)
        return concentration.get(
            index.first,
            index.second
        ) * cos(weeksFromNow.toFloat() / 2 * 3.141592f)
    }

    fun getClosestIndex(latLng: LatLng): Pair<Int, Int> {
        //map latLng to projection coordinates(eta, xi)
        val (eta, xi) = project(latLng)
        //divide by length between points, then round to get correct index
        return Pair(round(eta / dy).toInt(), round(xi / dx).toInt())
    }

    //extend arrayFloat with a getter since theirs is very impractical
    fun ArrayFloat.get(row: Int, column: Int): Float =
        this.getFloat(idx.set(0, row, column))



    /**
     * project a latlng point to a point on the projection.
     * in the thredds dataset, maps from latlong to eps, xi.
     */
    fun project(lat: Float, lng: Float): Pair<Float, Float> =
        ProjCoordinate(0.0, 0.0).let { p ->
            projection.transform(ProjCoordinate(lng.toDouble(), lat.toDouble()), p)
        }.let { p ->
            Pair(p.y.toFloat(), p.x.toFloat())
        }

    fun project(latLng: LatLng): Pair<Float, Float> =
        project(latLng.lat.toFloat(), latLng.lng.toFloat())

    override fun toString() = "InfectiousPressure:" +
            "\nFrom: ${fromDate}, to: $toDate" +
            "\nTime since 2000-01-01: ${time}, GridMapping: -----" +
            "\nConcentration:\n" +
            NCdumpW.toString(concentration)
}