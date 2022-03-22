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

/**
 * Class representing infectious pressure over a grid at a given time.
 */
class InfectiousPressure(
    val concentration: ArrayFloat, //Particle concentration, aggregated number of particles in grid cell
    val eta_rho: ArrayFloat,       //projection_y_coordinate
    val xi_rho: ArrayFloat,        //projection_x_coordinate
    val latitude: ArrayFloat,
    val longitude: ArrayFloat,
    val time: Float,               //seconds since 2000-01-01 00:00:00
    val projection: CoordinateTransform, //transforms between latlong and projection coordinates
    val fromDate: Date?,
    val toDate: Date?
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
     * However, since stereographcprojection does not work, we use a searching algorithm,
     * which is much slower and might be incorrect in some border cases
     *
     * concentration is defined inside a bounded grid, so coordinates outside this will not return
     * valid results
     * TODO: decide appropriate output, null? closest border concentration?
     *
     * @param latLng latitude-longitude coordinate we want to find concentration at
     * @return concentration at specified lat long coordinate
     */
    fun getConcentration(latLng: LatLng): Float {
        /*find the concentrationgrid closest to our latlongpoint,
        we use euclidean distance, or technically L1, to measure distance between latlngs.*/
        val index = getClosestIndex(latLng)
        return concentration.get(index.first, index.second)
    }

    fun getLatitude(row: Int, column: Int): Float = latitude.get(row, column)
    fun getLongitude(row: Int, column: Int): Float = longitude.get(row, column)
    fun getEtaRho(i: Int): Float = eta_rho.getFloat(ideta.set(i))
    fun getXiRho(i: Int): Float = xi_rho.getFloat(idxi.set(i))

    /**
     * get concentration at a latitude a given amount of weeks from now
     *
     * TODO: this is just a mock-function, shapes the data from this week so it makes a "nice" curve
     *
     * @see getConcentration(L atLng)
     */
    fun getConcentration(latLng: LatLng, weeksFromNow: Int): Float {
        /*find the concentrationgrid closest to our latlongpoint,
        we use euclidean distance, or technically L1, to measure distance between latlngs.*/
        val index = getClosestIndex(latLng)
        return concentration.get(
            index.first,
            index.second
        ) * cos(weeksFromNow.toFloat() / 2f * 3.141592f)
    }

    /**
     * Use L1 distance to find the index in the array closest to a given LatLng
     *
     * There are some possible hand-tuned optimization that can be made, but nothing close
     * to using projection
     *
     * Is currently O(n^2) (n = grid size), can be made O(1) with stereographicprojection
     * TODO: make version with stereographic projection, is faster and correct
     */
    private fun getClosestIndex(latLng: LatLng): Pair<Int, Int> {
        var row = 0
        var column = 0
        var minDistance = 1000.0
        var distance: Double
        //find row from latitude
        for (i in 0 until shape.first) {
            for (j in 0 until shape.second) {
                /*we dont need to use squareroot, since min of distance with squareroot has the same
                minimum as without(since squareroot is an ascending function)*/
                distance = latLng.haversine(
                    LatLng(
                        latitude.get(i, j).toDouble(),
                        longitude.get(i, j).toDouble()
                    )
                )
                if (distance < minDistance) {
                    row = i
                    column = j
                    minDistance = distance
                }
            }
        }
        return Pair(row, column)
    }

    //extend arrayFloat with a getter since thiers is very impractical
    fun ArrayFloat.get(row: Int, column: Int): Float =
        this.getFloat(idx.set(0, row, column))

    //TODO: stereographicprojection solution:
    /*/** map from eta xi(ProjectionPoint) to LatLng */
    fun projectionPointToLatLng(point: ProjectionPoint): LatLng =
        projection.projToLatLon(point).let { latLongPoint ->
            LatLng(
                latLongPoint.latitude,
                latLongPoint.longitude
            )
        }*/

    //TODO: stereographicprojection solution:
    /*/** map from latitude longitude to eta xi(ProjectionPoint) */
    fun latLngToProjectionPoint(latlng: LatLng): ProjectionPoint =
        projection.latLonToProj(LatLonPointImpl(latlng.lat, latlng.lng))*/

    override fun toString() = "InfectiousPressure:" +
            "\nFrom: ${fromDate}, to: $toDate" +
            "\nTime since 2000-01-01: ${time}, GridMapping: -----" +
            "\nConcentration:\n" +
            NCdumpW.toString(concentration) +
            "\nLatitude:\n" +
            NCdumpW.toString(latitude) +
            "\nLongitude:\n" +
            NCdumpW.toString(longitude) +
            "\nEta_rho:\n" +
            NCdumpW.toString(eta_rho) +
            "\nXi_rho:\n" +
            NCdumpW.toString(xi_rho) + "\n"

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

    /*companion object Projection {
        val lat0 = 90f
        val lng0 = 0f

        fun project(lat: Float, lng: Float) : Pair<Float, Float> {
            val xi = 2* atan(
                tan(
                (1/4* PI +1/2*lat)*
                        ((1- exp(1f) * sin(lat))/(1+ exp(1f) * sin(lat))).pow(exp(1f) /2)
            )
            ) - PI /2
            val Re = 6378000f
            val R  = Re*cos(lat)/(1- exp(2f) * sin(lat) * sin(lat))*cos(xi)
            val k  = 2*R/(1+lat0* sin(lat) +cos(lat0)*cos(lat)*cos(lng-lng0))

            val x = k*cos(lat)* sin(lng-lng0)
            val y = k*(cos(lat0)* sin(lat) - sin(lat0) *cos(lat)*cos(lng-lng0))
            return Pair(x.toFloat(),y.toFloat())
        }*/

}