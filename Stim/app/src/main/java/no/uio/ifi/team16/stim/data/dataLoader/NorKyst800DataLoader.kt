package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.result.getOrElse
import com.github.kittinunf.result.onError
import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.util.LatLong
import ucar.ma2.ArrayDouble
import ucar.ma2.ArrayInt
import no.uio.ifi.team16.stim.data.dataLoader.parser.NorKyst800RegexParser
import no.uio.ifi.team16.stim.util.Options

/**
 * DataLoader for data related tot he norkyst800 model.
 * Temperature, salinity, water velocity etc
 **/
class NorKyst800DataLoader : THREDDSDataLoader() {
    private val TAG = "NorKyst800DataLoader"

    private val xStride = Options.defaultNorKyst800XStride
    private val yStride = Options.defaultNorKyst800YStride
    private val depthStride = Options.defaultNorKyst800DepthStride
    private val timeStride = Options.defaultNorKyst800TimeStride
    private val yRange = "0:${yStride}:901"
    private val xRange = "0:${xStride}:901"
    private val depthRange = "0:${depthStride}:15"
    val timeRange = "0:${timeStride}:42"

    private val catalogUrl =
        "https://thredds.met.no/thredds/catalog/fou-hi/norkyst800m-1h/catalog.html"

    //make the default url, we need the base url first, however, and that has to be loaded from the catalog
    private var defaultUrlFactory: (String) -> String = { baseUrl ->
        baseUrl +
                "depth[${depthRange}]," +
                "lat[${yRange}][${xRange}]," +
                "lon[${yRange}][${xRange}]," +
                "salinity[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "temperature[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "time[${timeRange}]," +
                "u[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "v[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "w[${timeRange}][${depthRange}][${yRange}][${xRange}]"
    }

    /*
    /**
     * return data between latitude from/to, and latitude from/to, with given resolution.
     * Uses minimum of given and possible resolution.
     * crops to dataset if latitudes or longitudes exceed the dataset.
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
    ): NorKyst800? {
        //get the forecast url
        val baseUrl = loadForecastUrl() ?: return null
        //convert parameters to ranges
        val (xRange, yRange) = geographicCoordinateToRange(
            latitudeFrom, latitudeTo, Options.defaultNorKyst800YStride,
            longitudeFrom, longitudeTo, Options.defaultNorKyst800XStride,
            Options.defaultProjection() //TODO: cannot be read from file in .ascii solution, but MIGHT be wrong
        )
        val depthRange = "${depthFrom}:${depthStride}:${depthTo}"
        val timeRange = "${timeFrom}:${timeStride}:${timeTo}"
        //make the parametrized url from the forecast url and parameters
        val parametrizedUrl = baseUrl + "depth[${depthRange}]," +
                "lat[${yRange}][${xRange}]," +
                "lon[${yRange}][${xRange}]," +
                "salinity[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "temperature[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "time[${timeRange}]," +
                "u[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "v[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "w[${timeRange}][${depthRange}][${yRange}][${xRange}]"

        val responseStr = Fuel.get(parametrizedUrl).awaitString()
        if (responseStr.isEmpty()) {
            return null
        }

        Log.d(TAG, "parsing norkyst800")
        return NorKyst800RegexParser().parse(responseStr)
    }

    /**
     * The most general load
     * loads the specified ranges of data
     *
     * convenience loader, gets depth and timeranges as strings rather than int indexes
     * @see load(Float, Float, Float, FLoat, FLoat, Float, Int, Int, Int, Int, Int, Int)
     *
     * Copy of load(...) TODO: reformat to reuse code, this is too bloated and there is a lot of common code
     *
     * @param latitudeFrom upper left corner latitude
     * @param latitudeTo lower right corner longitude
     * @param latitudeResolution resolution of latitude, a resolution of 0.001 means that we sample latitudes 0.001 apart
     * @param longitudeFrom upper left corner longitude
     * @param longitudeTo lower right corner longitude
     * @param longitudeResolution resolution of longitude, a resolution of 0.001 means that we sample longitudes 0.001 apart
     * @param depthRange depth as a range with format from:stride:to
     * @param timeRange depth as a range with format from:stride:to
     */
    suspend fun load(
        latitudeFrom: Float,
        latitudeTo: Float,
        latitudeResolution: Float, //TODO: not used, yet, must find solution to convert to yStride, using Options defualt
        longitudeFrom: Float,
        longitudeTo: Float,
        longitudeResolution: Float, //TODO: not used, yet, must find solution to convert to Xstride, using Options defualt
        depthRange: String,
        timeRange: String
    ): NorKyst800? {
        //get the forecast url
        val baseUrl = loadForecastUrl()
        //convert parameters to ranges
        val (xRange, yRange) = geographicCoordinateToRange(
            latitudeFrom, latitudeTo, Options.defaultNorKyst800YStride,
            longitudeFrom, longitudeTo, Options.defaultNorKyst800XStride,
            Options.defaultProjection() //TODO: cannot be read from file in .ascii solution, but MIGHT be wrong
        )

        val parametrizedUrl = baseUrl + "depth[${depthRange}]," +
                "lat[${yRange}][${xRange}]," +
                "lon[${yRange}][${xRange}]," +
                "salinity[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "temperature[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "time[${timeRange}]," +
                "u[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "v[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "w[${timeRange}][${depthRange}][${yRange}][${xRange}]"

        Log.d(TAG, parametrizedUrl)
        Log.d(TAG, "requesting norkyst800")
        val responseStr = Fuel.get(parametrizedUrl).awaitStringResult().onError { error ->
            Log.e(TAG, "Failed to load norkyst800data due to:\n $error")
            return null
        }.getOrElse {
            Log.e(TAG, "No errors thrown, but unable to get NorKyst800 data from get request. Is the URL correct?")
            return null
        }

        Log.d(TAG, "got norkyst800")
        if (responseStr.isEmpty()) {
            return null
        }

        Log.d(TAG, "parsing norkyst800")
        return NorKyst800RegexParser().parse(responseStr)
    }
    */


    //load with default parameters(as specified in Options)
    suspend fun loadDefault(): NorKyst800? {
        //get the forecast url
        val baseUrl = loadForecastUrl() ?: return null
        //make the default url from the ase url
        val defaultUrl = defaultUrlFactory(baseUrl)
        Log.d(TAG, defaultUrl)
        Log.d(TAG, "requesting norkyst800")
        val responseStr = Fuel.get(defaultUrl).awaitStringResult().onError { error ->
            Log.e(TAG, "Failed to load norkyst800data due to:\n $error")
            return null
        }.getOrElse {
            Log.e(
                TAG,
                "No errors thrown, but unable to get NorKyst800 data from get request. Is the URL correct?"
            )
            return null
        }

        Log.d(TAG, "got norkyst800")
        if (responseStr.isEmpty()) {
            return null
        }

        Log.d(TAG, "parsing norkyst800")
        return NorKyst800RegexParser().parse(responseStr)
    }


    /**
     * load the thredds catalog for the norkyst800 dataset, then return the URL
     * for the forecast data(which changes periodically)
     */
    private val forecastUrlRegex =
        Regex("""'catalog\.html\?dataset=norkyst800m_1h_files/(.*?\.fc\..*?)'""")
    private suspend fun loadForecastUrl(): String? = try {
        Fuel.get(catalogUrl).awaitString()
    } catch (e: Exception) {
        Log.e(TAG, "Unable to retrieve NorKyst800 catalog due to", e)
        null
    }?.let { responseStr -> //regex out the url with .fc. in it
        forecastUrlRegex.find(responseStr)?.let { match ->
            "https://thredds.met.no/thredds/dodsC/fou-hi/norkyst800m-1h/" +
                    match.groupValues!![1] + //if there is a match, the group (entry[1]) is guaranteed to exist
                    ".ascii?"
        } ?: run { //"catch" unsucssessfull parse
            Log.e(
                TAG,
                "Failed to parse out the forecast URL from\n ${responseStr}\n with regex $forecastUrlRegex,\n returning null"
            )
            null
        }
    }
}