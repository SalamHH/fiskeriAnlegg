package no.uio.ifi.team16.stim.data.dataLoader

//import no.uio.ifi.team16.stim.data.dataLoader.parser.NorKyst800RegexParser
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
import no.uio.ifi.team16.stim.util.reformatFSL

/**
 * DataLoader for data related tot he norkyst800 model.
 * Temperature, salinity, water velocity etc
 **/
open class NorKyst800DataLoader : THREDDSDataLoader() {
    private val TAG = "NorKyst800DataLoader"

    //the catalog url is assumed to be time-invariant. Here all entries are listed.
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
        /*
        * Due to the abscence of certain critical java libraries for doing requests over https with
        * netcdf java, we have to fetch the data "ourselves", with a fuel request. We then open the file in memory.
        * The downside is that we cannot stream the data, but the data can be retrieved pre-sliced
        */
        val baseUrl = loadForecastUrl() ?: run {
            Log.e(
                TAG,
                "Failed to load the forecast URL from the catalog, is the catalog URL correct?"
            )
            return@load null
        }

        val salinityFillValue = -32767
        val temperatureFillValue = -32767
        val uFillValue = -32767
        val vFillValue = -32767
        val wFillValue = 1.0E37f

        ///////////////////
        // MAKE THE URLS //
        ///////////////////
        val salinityTemperatureTimeAndDepthUrl = makeSalinityTemperatureTimeAndDepthUrl(
            baseUrl,
            xRange,
            yRange,
            depthRange,
            timeRange
        )
        Log.d(TAG, salinityTemperatureTimeAndDepthUrl)

        val velocityUrl = makeVelocityUrl(
            baseUrl,
            xRange,
            yRange,
            depthRange,
            timeRange
        )

        //////////////
        // VELOCITY //
        //////////////
        var velocityString =
            Fuel.get(velocityUrl).awaitStringResult().onError { error ->
                Log.e(TAG, "Failed to load norkyst800data - velocity due to:\n $error")
                return null
            }.getOrElse { err ->
                Log.e(
                    TAG,
                    "Unable to get NorKyst800-velocity data from get request. Is the URL correct? $err"
                )
                return null
            }

        if (velocityString.isEmpty()) {
            Log.e(TAG, "Empty velocity response")
            return null
        }
        //PARSE VELOCITY
        val velocity = Triple(
            NorKyst800RegexParser.makeNullable4DFloatArrayOf(
                "u",
                velocityString,
                0.001f,
                0.0f,
                uFillValue
            ) ?: run {
                Log.e(NorKyst800RegexParser.TAG, "Failed to read <u> from NorKyst800")
                return@load null
            },
            NorKyst800RegexParser.makeNullable4DFloatArrayOf(
                "v",
                velocityString,
                0.001f,
                0.0f,
                vFillValue
            ) ?: run {
                Log.e(NorKyst800RegexParser.TAG, "Failed to read <v> from NorKyst800")
                return@load null
            },
            NorKyst800RegexParser.makeNullable4DFloatArrayOfW(
                "w",
                velocityString,
                1.0f,
                0.0f,
                wFillValue
            ) ?: run {
                Log.e(NorKyst800RegexParser.TAG, "Failed to read <w> from NorKyst800")
                return@load null
            }
        )

        ///////////////////////////////////////////
        // SALINITY, TEMPERATURE, TIME AND DEPTH //
        ///////////////////////////////////////////
        var salinityTemperatureTimeAndDepthString =
            Fuel.get(salinityTemperatureTimeAndDepthUrl).awaitStringResult().onError { error ->
                Log.e(
                    TAG,
                    "Failed to load NorKyst800-salinity-temperature-depth-time due to:\n $error"
                )
                return null
            }.getOrElse { err ->
                Log.e(
                    TAG,
                    "Unable to get NorKyst800-salinity-temperature-depth-time data from get request. Is the URL correct? $err"
                )
                return null
            }

        if (salinityTemperatureTimeAndDepthString.isEmpty()) {
            Log.e(TAG, "Empty NorKyst800-salinity-temperature-depth-time response")
            return null
        }

        //PARSE salinityTemperatureTimeAndDepth
        val depth =
            NorKyst800RegexParser.make1DFloatArrayOf("depth", salinityTemperatureTimeAndDepthString)
                ?: run {
                    Log.e(TAG, "Failed to read <depth> from NorKyst800")
                    return@load null
                }

        val time =
            NorKyst800RegexParser.make1DFloatArrayOf("time", salinityTemperatureTimeAndDepthString)
                ?: run {
                    Log.e(NorKyst800RegexParser.TAG, "Failed to read <time> from NorKyst800")
                    return@load null
                }

        val salinity =
            NorKyst800RegexParser.makeNullable4DFloatArrayOf(
                "salinity",
                salinityTemperatureTimeAndDepthString,
                0.001f,
                30.0f,
                salinityFillValue
            ) ?: run {
                Log.e(NorKyst800RegexParser.TAG, "Failed to read <salinity> from NorKyst800")
                return@load null
            }

        val temperature =
            NorKyst800RegexParser.makeNullable4DFloatArrayOf(
                "temperature",
                salinityTemperatureTimeAndDepthString,
                0.01f,
                0.0f,
                salinityFillValue
            ) ?: run {
                Log.e(NorKyst800RegexParser.TAG, "Failed to read <temperature> from NorKyst800")
                return@load null
            }

        /*val parametrizedUrl = makeParametrizedUrl(baseUrl, xRange, yRange, depthRange, timeRange)
        Log.d(TAG, parametrizedUrl)

        val responseStr = Fuel.get(parametrizedUrl).awaitStringResult().onError { error ->
            Log.e(TAG, "Failed to load norkyst800data due to:\n $error")
            return@load null
        }.getOrElse {
            Log.e(
                TAG,
                "No errors thrown, but unable to get NorKyst800 data from get request. Is the URL correct?"
            )
            return@load null
        }

        if (responseStr.isEmpty()) {
            Log.e(TAG, "Empty response")
            return null
        }

        //open the file
        var nork: NorKyst800? = null

        val parseTime = measureTimeMillis {
            nork = NorKyst800RegexParser().parse(responseStr)
        }
        Log.d(TAG, "Parsed data in $parseTime ms")*/

        return NorKyst800(
            depth,
            salinity,
            temperature,
            time,
            velocity
        )
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
    suspend fun loadDefault(): NorKyst800? =
        try {
            load(
                Options.defaultNorKyst800XRange,
                Options.defaultNorKyst800YRange,
                Options.defaultNorKyst800DepthRange,
                Options.defaultNorKyst800TimeRange
            )
        } catch (err: OutOfMemoryError) {
            Log.e(TAG, "out of memory while loading Norkyst800, increasing stride and trying again")
            //incease stride to decrease datasize, will divide size by about 4
            Options.defaultNorKyst800XStride = 2 * Options.defaultNorKyst800XStride
            Options.defaultNorKyst800XRange =
                IntProgression.fromClosedRange(
                    0,
                    Options.norKyst800XEnd,
                    Options.defaultNorKyst800XStride
                )
            Options.defaultNorKyst800YStride = 2 * Options.defaultNorKyst800YStride
            Options.defaultNorKyst800YRange =
                IntProgression.fromClosedRange(
                    0,
                    Options.norKyst800XEnd,
                    Options.defaultNorKyst800XStride
                )
            loadDefault()
        }

    ///////////////
    // UTILITIES //
    ///////////////
    protected fun makeParametrizedUrl(
        baseUrl: String,
        xRange: IntProgression,
        yRange: IntProgression,
        depthRange: IntProgression,
        timeRange: IntProgression
    ): String {
        val xyString =
            "[${xRange.reformatFSL()}][${yRange.reformatFSL()}]"
        val dString = "[${depthRange.reformatFSL()}]"
        val tString = "[${timeRange.reformatFSL()}]"
        val dtxyString = dString + tString + xyString
        return baseUrl +
                "depth$dString," +
                "salinity$dtxyString," +
                "temperature$dtxyString," +
                "time$tString," +
                "u$dtxyString," +
                "v$dtxyString," +
                "w$dtxyString"
    }

    private fun makeSalinityTemperatureTimeAndDepthUrl(
        baseUrl: String,
        xRange: IntProgression,
        yRange: IntProgression,
        depthRange: IntProgression,
        timeRange: IntProgression
    ): String {
        val xyString =
            "[${xRange.reformatFSL()}][${yRange.reformatFSL()}]"
        val dString = "[${depthRange.reformatFSL()}]"
        val tString = "[${timeRange.reformatFSL()}]"
        val dtxyString = dString + tString + xyString
        return baseUrl +
                "depth$dString," +
                "salinity$dtxyString," +
                "temperature$dtxyString," +
                "time$tString"
    }

    private fun makeVelocityUrl(
        baseUrl: String,
        xRange: IntProgression,
        yRange: IntProgression,
        depthRange: IntProgression,
        timeRange: IntProgression
    ): String {
        val xyString =
            "[${xRange.reformatFSL()}][${yRange.reformatFSL()}]"
        val dString = "[${depthRange.reformatFSL()}]"
        val tString = "[${timeRange.reformatFSL()}]"
        val dtxyString = dString + tString + xyString
        return baseUrl +
                "u$dtxyString," +
                "v$dtxyString," +
                "w$dtxyString"
    }

    /**
     * load the thredds catalog for the norkyst800 dataset, then return the URL
     * for the forecast data(which changes periodically)
     */
    private val forecastUrlRegex =
        Regex("""'catalog\.html\?dataset=norkyst800m_1h_files/(.*?\.fc\..*?)'""")

    protected suspend fun loadForecastUrl(): String? = try {
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