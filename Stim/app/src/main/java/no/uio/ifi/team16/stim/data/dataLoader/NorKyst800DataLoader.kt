package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.result.getOrElse
import com.github.kittinunf.result.onError
import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.data.dataLoader.parser.NorKyst800RegexParser
import no.uio.ifi.team16.stim.util.LatLong
import no.uio.ifi.team16.stim.util.Options

/**
 * DataLoader for data related tot he norkyst800 model.
 * Temperature, salinity, water velocity etc
 **/
class NorKyst800DataLoader : THREDDSDataLoader() {
    private val TAG = "NorKyst800DataLoader"

    private val catalogUrl =
        "https://thredds.met.no/thredds/catalog/fou-hi/norkyst800m-1h/catalog.html"

    /////////////
    // LOADERS //
    /////////////
    /**
     * Get NorKyst800 data in the given range
     *
     * @param xRange range of x-values to get from
     * @param yRange range of y-values to get from
     * @param depthRange depth as a range with format from:stride:to
     * @param timeRange time as a range with format from:stride:to
     * @return Norkyst800 data in the given range
     */
    suspend fun load(
        xRange: IntProgression,
        yRange: IntProgression,
        depthRange: IntProgression,
        timeRange: IntProgression
    ): NorKyst800? {
        val baseUrl = loadForecastUrl() ?: run {
            Log.e(
                TAG,
                "Failed to load the forecast URL form the catalog, is the catalog URL correct?"
            )
            return null
        }

        val parametrizedUrl = makeParametrizedUrl(baseUrl, xRange, yRange, depthRange, timeRange)

        val responseStr = Fuel.get(parametrizedUrl).awaitStringResult().onError { error ->
            Log.e(TAG, "Failed to load norkyst800data due to:\n $error")
            return null
        }.getOrElse {
            Log.e(
                TAG,
                "No errors thrown, but unable to get NorKyst800 data from get request. Is the URL correct?"
            )
            return null
        }

        if (responseStr.isEmpty()) {
            Log.e(TAG, "Empty response")
            return null
        }

        Log.d(TAG, "parsing norkyst800")
        return NorKyst800RegexParser().parse(responseStr)
    }

    /**
     * return data in a box specified by the given coordinates.
     *
     * @param latLongUpperLeft upper left coordinate in a box
     * @param latLongLowerRight lower right coordinate in a box
     * @param xStride stride between x-values in grid. 1=max resolution
     * @param yStride stride between y-values in grid. 1=max resolution
     * @param depthRange depth as a range with format from:stride:to
     * @param timeRange time as a range with format from:stride:to
     * @return NorKyst800 data in the prescribed data range
     */
    suspend fun load(
        latLongUpperLeft: LatLong,
        latLongLowerRight: LatLong,
        xStride: Int,
        yStride: Int,
        depthRange: IntProgression,
        timeRange: IntProgression
    ): NorKyst800? {
        //convert parameters to ranges
        val (xRange, yRange) = geographicCoordinateToRange(
            latLongUpperLeft,
            latLongLowerRight,
            xStride,
            yStride,
            Options.defaultProjection() //TODO: cannot be read from file in .ascii solution, but MIGHT be wrong
        )
        return load(xRange, yRange, depthRange, timeRange)
    }

    //load with default parameters(as specified in Options)
    suspend fun loadDefault(): NorKyst800? = load(
        Options.defaultNorKyst800XRange,
        Options.defaultNorKyst800YRange,
        Options.defaultNorKyst800DepthRange,
        Options.defaultNorKyst800TimeRange
    )

    ///////////////
    // UTILITIES //
    ///////////////
    protected fun makeParametrizedUrl(
        baseUrl: String,
        xRange: IntProgression,
        yRange: IntProgression,
        depthRange: IntProgression,
        timeRange: IntProgression
    ): String = baseUrl +
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
                    match.groupValues[1] + //if there is a match, the group (entry[1]) is guaranteed to exist
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