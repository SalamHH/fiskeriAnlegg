package no.uio.ifi.team16.stim.data

import no.uio.ifi.team16.stim.util.LatLng
import ucar.ma2.ArrayFloat
import ucar.ma2.Index3D
import ucar.nc2.NCdumpW
import ucar.unidata.geoloc.LatLonPointImpl
import ucar.unidata.geoloc.ProjectionPoint
import ucar.unidata.geoloc.projection.proj4.StereographicAzimuthalProjection
import java.util.*

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
    val fromDate: Date,
    val toDate: Date,
    val projection: StereographicAzimuthalProjection,
    val separation: Double
) {
    var idx: Index3D = Index3D(concentration.shape)

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
     * @param latlon latitude-longitude coordinate we want to find concentration at
     * @return concentration at specified lat long coordinate
     */
    fun getConcentration(latlng: LatLng) {
        latLngToProjectionPoint(latlng).let { point ->
            //fin

        }
    }

    fun getConcentration(row: Int, column: Int): Float {
        return concentration.getFloat(idx.set(0, row, column))
    }

    /** map from eta xi(ProjectionPoint) to LatLng */
    fun projectionPointToLatLng(point: ProjectionPoint): LatLng =
        projection.projToLatLon(point).let { latLongPoint ->
            LatLng(
                latLongPoint.latitude,
                latLongPoint.longitude
            )
        }

    /** map from latitude longitude to eta xi(ProjectionPoint) */
    fun latLngToProjectionPoint(latlng: LatLng): ProjectionPoint =
        projection.latLonToProj(LatLonPointImpl(latlng.lat, latlng.lng))

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
}