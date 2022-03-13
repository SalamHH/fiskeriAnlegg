package no.uio.ifi.team16.stim.data

import ucar.ma2.ArrayFloat
import ucar.nc2.NCdumpW
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
    val gridMapping: Int           //grid mapping  (???)
) {

    override fun toString() = "InfectiousPressure:" +
            "\nFrom: ${fromDate}, to: $toDate" +
            "\nTime since 2000-01-01: ${time}, GridMapping: $gridMapping" +
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