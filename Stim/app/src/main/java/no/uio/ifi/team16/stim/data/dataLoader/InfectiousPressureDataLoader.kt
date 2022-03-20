package no.uio.ifi.team16.stim.data.dataLoader

//import ucar.nc2.dataset.NetcdfDatasets
import android.util.Log
import no.uio.ifi.team16.stim.data.InfectiousPressure
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
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
    //"https://thredds.met.no/thredds/dodsC/fou-hi/norkyst800m-1h/NorKyst-800m_ZDEPTHS_his.an.2022020900.nc"

    /**
     * load the entire dataset
     */
    fun load(): InfectiousPressure? = load(-90f, 90f, 0.0001f, -90f, 90f, 0.0001f)

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
                //make some extra ranges to access data
                val range2 = "$rangeX,$rangeY"
                val range3 = "0,$range2"

                //make the projection
                val crsFactory = CRSFactory()
                val stereoCRT = crsFactory.createFromParameters(
                    null,
                    gridMapping.findAttribute("proj4string")!!.stringValue!!
                )
                val latLngCRT = stereoCRT.createGeographic()
                val ctFactory = CoordinateTransformFactory()
                val latLngToStereo: CoordinateTransform =
                    ctFactory.createTransform(latLngCRT, stereoCRT)

                // note that this way of reading does not apply scale or offset
                // see variable attributes "scale_factor" and "add_offset".
                val infectiousPressure = InfectiousPressure(
                    concentrations.read(range3) as ArrayFloat,
                    eta_rhos.read(rangeX) as ArrayFloat,
                    xi_rhos.read(rangeY) as ArrayFloat,
                    lat.read(range2) as ArrayFloat,
                    lon.read(range2) as ArrayFloat,
                    time.readScalarFloat(),
                    latLngToStereo,
                    parseDate(ncfile.findGlobalAttribute("fromdate")!!.stringValue!!),
                    parseDate(ncfile.findGlobalAttribute("todate")!!.stringValue!!)
                    /* projection, kept in comments to implement correctly later!
                    StereographicAzimuthalProjection(
                        gridMapping.findAttribute("latitude_of_projection_origin")!!.numericValue!!.toDouble(),
                        gridMapping.findAttribute("straight_vertical_longitude_from_pole")!!.numericValue!!.toDouble(),
                        1.0,
                        1.0,
                        gridMapping.findAttribute("false_easting")!!.numericValue!!.toDouble(),
                        gridMapping.findAttribute("false_northing")!!.numericValue!!.toDouble(),
                        Earth()
                        //gridMapping.findAttribute("semi_major_axis").numericValue.toDouble())
                    ),*/
                    //gridMapping.findAttribute("dx")!!.numericValue as Double
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