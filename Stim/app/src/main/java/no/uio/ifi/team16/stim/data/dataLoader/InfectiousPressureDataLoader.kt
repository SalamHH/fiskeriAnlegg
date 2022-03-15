package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import no.uio.ifi.team16.stim.data.InfectiousPressure
import ucar.ma2.ArrayFloat
import ucar.ma2.InvalidRangeException
import ucar.nc2.Variable
import ucar.nc2.dataset.NetcdfDataset
import java.io.IOException

/**
 * DataLoader for infectious pressure data.
 *
 * Data is loaded through load(...) and returned as an InfectiousPressure, with the
 * concentration of salmon louse represented as a grid.
 **/
class InfectiousPressureDataLoader : THREDDSDataLoader() {
    private val TAG = "InfectiousPressureDataLoader"
    override val url =
        "http://thredds.nodc.no:8080/thredds/fileServer/smittepress_new2018/agg_OPR_2022_9.nc"

    /**
     * load the entire dataset
     */
    fun load(): InfectiousPressure? = load(-90f, 90f, 0.001f, -90f, 90f, 0.001f)

    /**
     * return data between latitude from/to, and latitude from/to, with given resolution.
     * Uses minimum of given and possible resolution.
     * crops to dataset if latitudes or longitudes exceed the dataset.
     *
     * TODO:
     * in general, finding the distance between two points of latitude/longitude is "hard",
     * need to find a library(netcdf probably has it) for doing this.
     * until then resolution is set to max(ie uses every datapoint between range)
     *
     * @param latitudeFrom smallest latitude to get data from
     * @param latitudeTo largest latitude to get data from
     * @param latitudeResolution resolution of latitude. A latitude resolution of 0.001 means that
     * the data is sampled from latitudeFrom to latitudeTo with 0.001 latitude between points
     * @param longitudeFrom smallest longitude to get data from
     * @param longitudeTo largest longitude to get data from
     * @param latitudeResolution resolution of longitude.
     * @return data of infectious pressure in the prescribed data range.
     */
    fun load(
        latitudeFrom: Float,
        latitudeTo: Float,
        latitudeResolution: Float,
        longitudeFrom: Float,
        longitudeTo: Float,
        longitudeResolution: Float
    ): InfectiousPressure? =
        NetcdfDataset.openDataset(url).let { ncfile ->
            Log.d(TAG, "OPENING $url")
            try {
                Log.d(TAG, "OPENDAP URL OPENED")
                //convert parameters to ranges
                val (rangeX, rangeY) = geographicCoordinateToRange(
                    latitudeFrom, latitudeTo,
                    latitudeResolution, longitudeFrom, longitudeTo, longitudeResolution
                )
                //lets make some infectious pressure
                //Variables are data that are NOT READ YET. findVariable() is not null-safe
                val concentrations: Variable = ncfile.findVariable("C10") ?: return null
                val eta_rhos: Variable = ncfile.findVariable("eta_rho") ?: return null
                val xi_rhos: Variable = ncfile.findVariable("xi_rho") ?: return null
                val lat: Variable = ncfile.findVariable("lat") ?: return null
                val lon: Variable = ncfile.findVariable("lon") ?: return null
                val time: Variable = ncfile.findVariable("time") ?: return null
                val gridMapping: Variable = ncfile.findVariable("grid_mapping") ?: return null
                //Stereographic(double latt, double lont, double scale, double false_easting, double false_northing, double radius)
                /**
                 *
                 *     latt - tangent point of projection, also origin of projection coord system
                lont - tangent point of projection, also origin of projection coord system
                scale - scale factor at tangent point, "normally 1.0 but may be reduced"
                false_easting - false easting in units of x coords
                false_northing - false northing in units of y coords
                radius - earth radius in km
                 */
                //Stereographic(double latt, double lont, double scale, double false_easting, double false_northing)
                //make some extra ranges to access data
                val range2 = "$rangeX,$rangeY"
                val range3 = "0,$range2"
                // note that this way of reading does not apply scale or offset
                // see variable attributes "scale_factor" and "add_offset"
                val infectiousPressure = InfectiousPressure(
                    concentrations.read(range3) as ArrayFloat,
                    eta_rhos.read(rangeX) as ArrayFloat,
                    xi_rhos.read(rangeY) as ArrayFloat,
                    lat.read(range2) as ArrayFloat,
                    lon.read(range2) as ArrayFloat,
                    time.readScalarFloat(),
                    parseDate(ncfile.findGlobalAttribute("fromdate").stringValue),
                    parseDate(ncfile.findGlobalAttribute("todate").stringValue),
                    gridMapping.readScalarInt()
                )
                ncfile.close()
                infectiousPressure //returned from let-, and then try-black
            } catch (e: IOException) {
                Log.e("ERROR", e.toString())
                null
            } catch (e: InvalidRangeException) {
                Log.e("ERROR", e.toString())
                null
            } catch (e: NullPointerException) {
                Log.e(
                    TAG,
                    "ERROR: a Variable might be read as null, are you sure you are using the correct url/dataset?"
                )
                Log.e("ERROR", e.toString())
                null
            } finally {
                //NetcdfDataset.shutdown() TODO should be called on application shutdown!
                Log.d(TAG, " load - DONE")
                ncfile.close()
            }
        }
}