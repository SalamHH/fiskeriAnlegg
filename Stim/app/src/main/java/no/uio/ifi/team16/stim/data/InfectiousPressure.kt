package no.uio.ifi.team16.stim.data

import ucar.ma2.ArrayFloat
import ucar.nc2.NCdumpW

/**
 * Class represeting infectious pressure over a grid.
 *
 * TODO: expand
 * TODO: getters/setters - resolution
 */
class InfectiousPressure(val concentration : ArrayFloat,
                          val eta_rho : ArrayFloat,
                          val xi_rho : ArrayFloat,
                          val latitude : ArrayFloat,
                          val longitude : ArrayFloat,
                         val time : Float,
                         val gridMapping : Int) {


    override fun toString() = "InfectiousPressure:\n" +
                "Time: ${time}, GridMapping: $gridMapping" +
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