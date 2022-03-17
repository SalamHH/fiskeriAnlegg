package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import no.uio.ifi.team16.stim.data.NorKyst800
import ucar.ma2.ArrayDouble
import ucar.ma2.ArrayInt
import ucar.ma2.InvalidRangeException
import ucar.nc2.Variable
import ucar.nc2.dataset.NetcdfDataset
import java.io.IOException
import java.net.URI

/**
 * DataLoader for data related tot he norkyst800 model.
 * Temperature, salinity, water velocity etc
 **/
class NorKyst800DataLoader : THREDDSDataLoader() {
    private val TAG = "NorKyst800DataLoader"
    val resolution = "500"
    val depthResolution = "15"
    val timeResolution = "41"
    val yRange = "0:${resolution}:901"
    val xRange = "0:${resolution}:901"
    val depthRange = "0:${depthResolution}:15"
    val timeRange = "0:${timeResolution}:42"

    //                 "https://thredds.met.no/thredds/fileServer/fou-hi/norkyst800m-1h/NorKyst-800m_ZDEPTHS_his.fc.2022031000.nc"
    override val url =
        "https://thredds.met.no/thredds/fileServer/fou-hi/norkyst800m-1h/NorKyst-800m_ZDEPTHS_his.fc.2022031700.nc?" +
                "depth[${depthRange}]," +
                "lat[${yRange}][${xRange}]," +
                "lon[${yRange}][${xRange}]," +
                "salinity[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "temperature[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "time[${timeRange}]," +
                "u[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "v[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "w[${timeRange}][${depthRange}][${yRange}][${xRange}]"

    /**
     * load the "entire" dataset
     * 388234558 byte allocation with 4194304 free bytes and 193MB until OOM
     * 385873886 byte allocation with 4194304 free bytes and 194MB until OOM
     * 385808110 byte allocation with 4194304 free bytes and 194MB until OOM
     */
    fun load(): NorKyst800? = load(-90f, 90f, 0.001f, -90f, 90f, 0.001f)

    /**
     * return data between latitude from/to, and latitude from/to, with given resolution.
     * Uses minimum of given and possible resolution.
     * crops to dataset if latitudes or longitudes exceed the dataset.
     *
     * TODO:
     * in general, finding the distance between two points of latitude/longitude is "hard",[${timeRage}][${depthRange}][${yRange}][${xRange}]
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
     * @return NorKyst800 data in the prescribed data range, primarily stream and wave data(?)
     */
    fun load(
        latitudeFrom: Float,
        latitudeTo: Float,
        latitudeResolution: Float,
        longitudeFrom: Float,
        longitudeTo: Float,
        longitudeResolution: Float
    ): NorKyst800? = NetcdfDataset.openInMemory(URI(url)).let { ncfile ->
        Log.d(TAG, "OPENING $url")
        try {
            Log.d(TAG, "OPENDAP URL OPENED")
            val depth: Variable = ncfile.findVariable("depth") ?: return null
            val lat: Variable = ncfile.findVariable("lat") ?: return null
            val lon: Variable = ncfile.findVariable("lon") ?: return null
            val salinity: Variable = ncfile.findVariable("salinity") ?: return null
            val temperature: Variable = ncfile.findVariable("temperature") ?: return null
            val time: Variable = ncfile.findVariable("time") ?: return null
            val u: Variable = ncfile.findVariable("u") ?: return null
            val v: Variable = ncfile.findVariable("v") ?: return null
            val w: Variable = ncfile.findVariable("w") ?: return null

            // note that this way of reading does not apply scale or offset
            // see variable attributes "scale_factor" and "add_offset".
            val norKyst800 = NorKyst800(
                depth.read() as ArrayDouble,
                lat.read() as ArrayDouble,
                lon.read() as ArrayDouble,
                salinity.read() as ArrayInt,
                temperature.read() as ArrayInt,
                time.read() as ArrayDouble,
                u.read() as ArrayInt,
                v.read() as ArrayInt,
                w.read() as ArrayInt
            )
            ncfile.close()
            norKyst800 //returned from let-, and then try-black
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