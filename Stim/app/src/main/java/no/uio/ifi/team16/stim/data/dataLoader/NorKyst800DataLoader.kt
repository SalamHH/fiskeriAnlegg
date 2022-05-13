package no.uio.ifi.team16.stim.data.dataLoader

//import no.uio.ifi.team16.stim.data.dataLoader.parser.NorKyst800Parser
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.data.dataLoader.parser.NorKyst800Parser
import no.uio.ifi.team16.stim.util.Options
import java.time.Instant
import java.time.ZonedDateTime
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
     * Due to the abscence of certain critical java libraries for doing requests over https with
     * netcdf java, we have to fetch the data "ourselves", with a fuel request. We then open the file in memory.
     * The downside is that we cannot stream the data, but the data can be retrieved pre-sliced
     *
     * First we have to load the catalog(unless previously loaded and cached). Then we have to load
     * the das of the data(attributes of variables). Finally we load the data itself, but beacuse the response is
     * large we do it in two separate requests.
     *
     * @param xRange range of x-values to get from
     * @param yRange range of y-values to get from
     * @param depthRange depth as a range with format from:stride:to
     * @return Norkyst800 data in the given range
     */
    suspend fun load(
        xRange: IntProgression,
        yRange: IntProgression,
        depthRange: IntProgression,
    ): NorKyst800? {
        val now = Instant.now() //seconds since 1970-01-01,
        val baseUrl =
            (if (ZonedDateTime.now().hour < 18) //use forecast until 18, then its a forecast from next day
                loadUrl(forecastUrlRegex)
            else
                loadUrl(currentUrlRegex)) ?: run {
                Log.e(
                    TAG,
                    "Failed to load the forecast URL from the catalog, is the catalog URL correct?"
                )
                return null
            }

        ////////////////////
        // TIME AND DEPTH //
        ////////////////////
        val (time, depth) = NorKyst800Parser.getTimeAndDepth(baseUrl)
            ?: return null //logged in inner
        //we now have time, and we want to find the index corresponding to our time
        //find index in array corresponding to current time
        val timeIndex: Int = time
            .takeWhile { t -> t < now.epochSecond }
            .size

        Log.d(TAG, "using timeindex $timeIndex")
        val t = fromClosedRange(timeIndex, timeIndex, 1)

        //////////////////////
        // DAS / ATTRIBUTES //
        //////////////////////
        val (FSOs, projection) = NorKyst800Parser.getDAS(baseUrl) ?: return null
        val salinityFSO = FSOs[0]
        val temperatureFSO = FSOs[1]
        val uFSO = FSOs[2]
        val vFSO = FSOs[3]
        val wFSO = FSOs[4]

        //////////////
        // SALINITY //
        //////////////
        val salinityUrl = NorKyst800Parser.makeSingle4DVariableUrl(
            baseUrl,
            "salinity",
            xRange,
            yRange,
            depthRange,
            t
        )

        val salinity = NorKyst800Parser.getNullable4DArrayFrom(salinityUrl, salinityFSO, "salinity")
            ?: return null

        /////////////////
        // TEMPERATURE //
        /////////////////
        val temperatureUrl = NorKyst800Parser.makeSingle4DVariableUrl(
            baseUrl,
            "temperature",
            xRange,
            yRange,
            depthRange,
            t
        )

        val temperature =
            NorKyst800Parser.getNullable4DArrayFrom(temperatureUrl, temperatureFSO, "temperature")
                ?: return null

        //////////////
        // VELOCITY //
        //////////////
        val velocityUrl = NorKyst800Parser.makeVelocityUrl(
            baseUrl,
            xRange,
            yRange,
            depthRange,
            t
        )

        val velocity = NorKyst800Parser.getVelocity(velocityUrl, uFSO, vFSO, wFSO) ?: return null

        ///////////
        // DONE! //
        ///////////
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
                Options.defaultNorKyst800DepthRange
            )
        } catch (err: OutOfMemoryError) {
            Log.e(TAG, "out of memory while loading Norkyst800, increasing stride and trying again")
            //incease stride to decrease datasize, will divide size by about 4
            Options.defaultNorKyst800XStride = 2 * Options.defaultNorKyst800XStride
            Options.defaultNorKyst800XRange =
                fromClosedRange(
                    0,
                    Options.norKyst800XEnd,
                    Options.defaultNorKyst800XStride
                )
            Options.defaultNorKyst800YStride = 2 * Options.defaultNorKyst800YStride
            Options.defaultNorKyst800YRange =
                fromClosedRange(
                    0,
                    Options.norKyst800XEnd,
                    Options.defaultNorKyst800XStride
                )
            loadDefault()
        }

    ///////////////
    // UTILITIES //
    ///////////////
    /**
     * load the thredds catalog for the norkyst800 dataset, then return the URL
     * for the forecast data(which changes periodically)
     */
    private val forecastUrlRegex =
        Regex("""'catalog\.html\?dataset=norkyst800m_1h_files/(.*?\.fc\..*?)'""")

    private val currentUrlRegex =
        Regex("""'catalog\.html\?dataset=norkyst800m_1h_files/(.*?\.an\..*?)'""")

    private suspend fun loadUrl(regex: Regex): String? = try {
        Fuel.get(catalogUrl).awaitString()
    } catch (e: Exception) {
        Log.e(NorKyst800Parser.TAG, "Unable to retrieve NorKyst800 catalog due to", e)
        null
    }?.let { responseStr -> //regex out the url with .fc. in it
        regex.find(responseStr)?.let { match ->
            "https://thredds.met.no/thredds/dodsC/fou-hi/norkyst800m-1h/" +
                    match.groupValues[1]  //if there is a match, the group (entry[1]) is guaranteed to exist
        } ?: run { //"catch" unsucssessfull parse
            Log.e(
                NorKyst800Parser.TAG,
                "Failed to parse out the forecast URL from\n ${responseStr}\n with regex $forecastUrlRegex,\n returning null"
            )
            null
        }
    }

}