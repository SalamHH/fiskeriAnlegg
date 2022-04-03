package no.uio.ifi.team16.stim.data.dataLoader

//import thredds.catalog.ThreddsMetadata
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.util.LatLong
import ucar.ma2.ArrayDouble
import ucar.ma2.ArrayInt
import no.uio.ifi.team16.stim.data.dataLoader.parser.NorKyst800RegexParser

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

    var url =
        "https://thredds.met.no/thredds/dodsC/fou-hi/norkyst800m-1h/NorKyst-800m_ZDEPTHS_his.fc.2022040300.nc.ascii?" +
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
    //fun load(): NorKyst800? = load(-90f, 90f, 0.001f, -90f, 90f, 0.001f)

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
        latLongUpperLeft: LatLong,
        latLongLowerRight: LatLong,
        latitudeResolution: Int,
        longitudeResolution: Int,
        depthRange: IntProgression,
        timeRange: IntProgression
    ): NorKyst800? = NorKyst800(
        Log.d(TAG, url)
        Log.d(TAG, "requesting norkyst800")
        val responseStr = Fuel.get(url).awaitString()
        //val responseStr = norkString
        Log.d(TAG, "got norkyst800")
        if (responseStr.isEmpty()) {
            return null
        }

        Log.d(TAG, "parsing norkyst800")
        return NorKyst800RegexParser().parse(responseStr)
    )
}