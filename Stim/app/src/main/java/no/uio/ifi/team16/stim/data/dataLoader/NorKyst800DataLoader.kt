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
import no.uio.ifi.team16.stim.util.Options
import no.uio.ifi.team16.stim.util.reformatFSL
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import java.time.Instant
import kotlin.ranges.IntProgression.Companion.fromClosedRange

/**
 * DataLoader for data related to the norkyst800 model.
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
     * Get NorKyst800 data in the given range.
     *
     * First we have to load the catalog(unless previously loaded and cached). Then we have to load
     * the das of the data(attributes of variables). Finally we load the data itself, but beacuse the response is
     * large we do it in two separate requests.
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
            return null
        }

        ///////////////////
        // MAKE THE URLS //
        ///////////////////
        val timeAndDepthUrl = makeTimeAndDepthUrl(baseUrl)

        ////////////////////
        // TIME AND DEPTH //
        ////////////////////
        val timeAndDepthString = requestData(timeAndDepthUrl, "time and depth") ?: return null

        val depth =
            NorKyst800RegexParser.make1DFloatArrayOf("depth", timeAndDepthString)
                ?: run {
                    Log.e(TAG, "Failed to read <depth> from NorKyst800")
                    return null
                }

        val time =
            NorKyst800RegexParser.make1DFloatArrayOf("time", timeAndDepthString)
                ?: run {
                    Log.e(NorKyst800RegexParser.TAG, "Failed to read <time> from NorKyst800")
                    return null
                }

        //we now have time, and we want to find the index corresponding to our time
        val now = Instant.now().epochSecond //seconds since 1970-01-01,
        //find th
        val timeIndex: Int = time
            .takeWhile { t -> t < now }
            .size
        /*?: run {
            Log.w(TAG, "failed to find time corresponding to now in dataset, using first") //TODO: or fail
        }*/
        Log.w(TAG, "using timeindex $timeIndex")
        val t = fromClosedRange(timeIndex, timeIndex, 1)

        val salinityTemperatureUrl = makeSalinityTemperatureUrl(
            baseUrl,
            xRange,
            yRange,
            depthRange,
            t
        )

        val velocityUrl = makeVelocityUrl(
            baseUrl,
            xRange,
            yRange,
            depthRange,
            t
        )

        val dasUrl = makeDasUrl(baseUrl)

        //////////////////////
        // DAS / ATTRIBUTES //
        //////////////////////
        /////////////
        // GET DAS //
        /////////////
        val dasString = requestData(dasUrl, "das") ?: return null

        ///////////////
        // PARSE DAS //
        ///////////////
        //make a map from variable names to strings of their attributes
        val variablesToAttributes = NorKyst800RegexParser.parseDas(dasString)
        Log.d(TAG, variablesToAttributes.toString())
        //SALINITY, make standard, then parse and put any non-null into it
        val salinityFSO = Triple(-32767, 0.001f, 30.0f).let { defaultFSO ->
            val (f, s, o) = getFSO(variablesToAttributes, "salinity")
            Triple(
                f ?: defaultFSO.first,
                s ?: defaultFSO.second,
                o ?: defaultFSO.third
            )
        }
        //TEMPERATURE, make standard, then parse and put any non-null into it
        val temperatureFSO = Triple(-32767, 0.01f, 0.0f).let { defaultFSO ->
            val (f, s, o) = getFSO(variablesToAttributes, "temperature")
            Triple(
                f ?: defaultFSO.first,
                s ?: defaultFSO.second,
                o ?: defaultFSO.third
            )
        }
        //U, make standard, then parse and put any non-null into it
        val uFSO = Triple(-32767, 0.001f, 0.0f).let { defaultFSO ->
            val (f, s, o) = getFSO(variablesToAttributes, "u")
            Triple(
                f ?: defaultFSO.first,
                s ?: defaultFSO.second,
                o ?: defaultFSO.third
            )
        }
        //V, make standard, then parse and put any non-null into it
        val vFSO = Triple(-32767, 0.001f, 0.0f).let { defaultFSO ->
            val (f, s, o) = getFSO(variablesToAttributes, "v")
            Triple(
                f ?: defaultFSO.first,
                s ?: defaultFSO.second,
                o ?: defaultFSO.third
            )
        }
        //W, make standard, then parse and put any non-null into it
        val wFSO = Triple(1.0E37f, 1.0f, 0.0f).let { defaultFSO ->
            val (f, s, o) = getFSO(variablesToAttributes, "w")
            Triple(
                f ?: defaultFSO.first,
                s ?: defaultFSO.second,
                o ?: defaultFSO.third
            )
        }

        //PROJECTION
        val proj4String = variablesToAttributes["projection"]
            ?.toList() //evaluate sequence
            ?.find { (_, name, _) -> //find the correct attribute
                name == "proj4String"
            }
            ?.third //take the value of that attribute
            ?: run {
                Log.w(TAG, "Failed to parse projection from DAS response, using default")
                Options.defaultProj4String
            }

        //make the projection from string
        val projection: CoordinateTransform =
            CRSFactory().createFromParameters(null, proj4String).let { stereoCRT ->
                val latLngCRT = stereoCRT.createGeographic()
                val ctFactory = CoordinateTransformFactory()
                ctFactory.createTransform(latLngCRT, stereoCRT)
            }

        //////////////
        // VELOCITY //
        //////////////
        val velocityString = requestData(velocityUrl, "velocity") ?: return null

        //PARSE VELOCITY
        val velocity = Triple(
            NorKyst800RegexParser.makeNullable4DFloatArrayOf(
                "u",
                velocityString,
                uFSO
            ) ?: run {
                Log.e(NorKyst800RegexParser.TAG, "Failed to read <u> from NorKyst800")
                return null
            },
            NorKyst800RegexParser.makeNullable4DFloatArrayOf(
                "v",
                velocityString,
                vFSO
            ) ?: run {
                Log.e(NorKyst800RegexParser.TAG, "Failed to read <v> from NorKyst800")
                return null
            },
            NorKyst800RegexParser.makeNullable4DFloatArrayOfW(
                "w",
                velocityString,
                wFSO
            ) ?: run {
                Log.e(NorKyst800RegexParser.TAG, "Failed to read <w> from NorKyst800")
                return null
            }
        )

        //////////////////////////
        // SALINITY, TEMPERATURE//
        //////////////////////////
        var salinityTemperatureString =
            requestData(salinityTemperatureUrl, "salinity and temperature") ?: return null

        //PARSE
        val salinity =
            NorKyst800RegexParser.makeNullable4DFloatArrayOf(
                "salinity",
                salinityTemperatureString,
                salinityFSO
            ) ?: run {
                Log.e(NorKyst800RegexParser.TAG, "Failed to read <salinity> from NorKyst800")
                return null
            }

        val temperature =
            NorKyst800RegexParser.makeNullable4DFloatArrayOf(
                "temperature",
                salinityTemperatureString,
                temperatureFSO
            ) ?: run {
                Log.e(NorKyst800RegexParser.TAG, "Failed to read <temperature> from NorKyst800")
                return@load null
            }

        return NorKyst800(
            depth,
            salinity,
            temperature,
            time,
            velocity,
            projection
        )
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
    private suspend fun requestData(url: String, name: String): String? {
        val string =
            Fuel.get(url).awaitStringResult().onError { error ->
                Log.e(TAG, "Failed to load norkyst800data - $name response due to:\n $error")
                return null
            }.getOrElse { err ->
                Log.e(
                    TAG,
                    "Unable to get NorKyst800-$name data from get request. Is the URL correct? $err"
                )
                return null
            }

        if (string.isEmpty()) {
            Log.e(TAG, "Empty $name response")
            return null
        }

        return string
    }

    /**
     * get Fillvalue, Scaling and Offset of a given attribute associated to a given variable
     * Note that not all variables have all three attributes, for example w in the norkyst dataset.
     *
     * @param variablesToAttributes map from variable-names to sequences of their attirbutes
     * an attribute has a type(first), name(second) and value(third)
     * @param variable name of variable to look up attributes for
     * @retrun fillvalue, scale and offset of the given variable
     */
    private fun getFSO(
        variablesToAttributes:
        Map<String, Sequence<Triple<String, String, String>>>,
        variable: String
    ): Triple<Number?, Number?, Number?> {
        var fillValue: Number? = null
        var scale: Number? = null
        var offset: Number? = null

        //open attributes of the given variable, the nparse out FSO from it
        variablesToAttributes[variable]?.let { attributes ->
            val attributeMap = NorKyst800RegexParser.parseVariableAttributes(attributes)

            fillValue = attributeMap["_FillValue"]?.let { (typeString, valueString) ->
                parseFSOAttributeAs(typeString, valueString) //parse valuestring to typestring
            } ?: Log.w(
                TAG,
                "Failed to load norkyst800data - ${variable}FillValue from das, using default"
            )

            scale = attributeMap["scale_factor"]?.let { (typeString, valueString) ->
                parseFSOAttributeAs(typeString, valueString) //parse valuestring to typestring
            } ?: Log.w(
                TAG,
                "Failed to load norkyst800data - ${variable}Scaling from das, using default"
            )

            offset = attributeMap["add_offset"]?.let { (typeString, valueString) ->
                parseFSOAttributeAs(typeString, valueString) //parse valuestring to typestring
            } ?: Log.w(
                TAG,
                "Failed to load norkyst800data - ${variable}Offset from das, using default"
            )
        }

        return Triple(fillValue, scale, offset)
    }

    /**
     * parse an attribute with given type and value
     *
     * this function is not exhaustive, but sufficient.
     */
    private fun parseFSOAttributeAs(type: String, attr: String): Number? =
        when (type) {
            "Float32" -> attr.toFloat()
            "Int16" -> attr.toInt()
            else -> run {
                Log.w(TAG, "Failed to parse FSO attribute with unknown type $type, returning null")
                null
            }
        }

    /**
     * make an url to get temperature, salinity
     *
     * @param baseUrl base url of dataset, usually retrieved by loadForecastURL().
     * @param xRange range of x-values to get from
     * @param yRange range of y-values to get from
     * @param depthRange depth as a range with format from:stride:to
     * @param timeRange time as a range with format from:stride:to
     */
    private fun makeSalinityTemperatureUrl(
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
                ".ascii?" +
                "salinity$dtxyString," +
                "temperature$dtxyString"
    }

    /**
     * make an url to get time and depth
     *
     * @param baseUrl base url of dataset, usually retrieved by loadForecastURL().
     * @param depthRange depth as a range with format from:stride:to
     * @param timeRange time as a range with format from:stride:to
     */
    private fun makeTimeAndDepthUrl(
        baseUrl: String
    ): String {
        return baseUrl +
                ".ascii?" +
                "depth," +
                "time"
    }


    /**
     * make an url to get velocity
     *
     * @param baseUrl base url of dataset, usually retrieved by loadForecastURL().
     * @param xRange range of x-values to get from
     * @param yRange range of y-values to get from
     * @param depthRange depth as a range with format from:stride:to
     * @param timeRange time as a range with format from:stride:to
     */
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
                ".ascii?" +
                "u$dtxyString," +
                "v$dtxyString," +
                "w$dtxyString"
    }

    /**
     * given a baseurl, return an url that gives the DAS of the dataset
     */
    private fun makeDasUrl(baseUrl: String): String {
        return "$baseUrl.das?"
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
                    match.groupValues[1]  //if there is a match, the group (entry[1]) is guaranteed to exist
        } ?: run { //"catch" unsucssessfull parse
            Log.e(
                TAG,
                "Failed to parse out the forecast URL from\n ${responseStr}\n with regex $forecastUrlRegex,\n returning null"
            )
            null
        }
    }
}