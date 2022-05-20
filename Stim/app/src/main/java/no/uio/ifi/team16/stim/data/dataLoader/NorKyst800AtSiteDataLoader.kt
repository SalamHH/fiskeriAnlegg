package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.result.getOrElse
import com.github.kittinunf.result.onError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.data.NorKyst800AtSite
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.dataLoader.parser.NorKyst800Parser
import no.uio.ifi.team16.stim.util.Options
import no.uio.ifi.team16.stim.util.project
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.ranges.IntProgression.Companion.fromClosedRange

/**
 * Dataloader for loading NorKyst800 data around a specified site
 **/
class NorKyst800AtSiteDataLoader {
    companion object {
        const val TAG = "NorKyst800AtSiteDataLoader"
    }

    //the catalog url is assumed to be time-invariant. Here all entries are listed.
    private val catalogUrl =
        "https://thredds.met.no/thredds/catalog/fou-hi/norkyst800m-1h/catalog.html"
    private var forecastUrlCache: String? = null

    /////////////
    // LOADERS //
    /////////////

    suspend fun load(
        site: Site
    ): NorKyst800AtSite? {
        //get the forecast utl from the catalo, caching the result.
        val forecastUrl = forecastUrlCache ?: run {
            val url = loadForecastUrl() ?: run {
                Log.e(
                    TAG,
                    "Failed to load the forecast URL from the catalog, is the catalog URL correct?"
                )
                return null
            }
            forecastUrlCache = url
            //parse out date of forecast url
            url
        }
        Log.d(TAG, "loading forecast date")
        val forecastDate = parseDateFromForecastUrl(forecastUrl) ?: return null
        val currentDate = LocalDate.now()
        Log.d(TAG, "got forecast date $forecastDate")

        val historicalUrl = forecastUrlIntoHistoricalUrl(forecastUrl)

        //the current data is in either historical or forecast dataset.
        //if the clock is past 17, it is in the historical one, otherwise it is in the forecast.
        val hourOfDay = Instant.now().atZone(ZoneId.systemDefault()).hour
        //if the forecast date is (<=) the current date, use the forecast url, otherwise use the historical url

        val currentUrl = if (forecastDate <= currentDate) {
            Log.d(TAG, "loading current from an")
            historicalUrl
        } else {
            Log.d(TAG, "loading current from fc")
            forecastUrl
        }
        Log.d(TAG, "at index $hourOfDay")

        //start an async load of all the available data
        val allData = MutableLiveData<NorKyst800>()
        CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
            //load for each set
            val forecastAtSite = loadWithUrl(site, forecastUrl)
            val historicalAtSite = loadWithUrl(site, historicalUrl)
            //now merge the two datasets
            if (historicalAtSite != null && forecastAtSite != null) {
                allData.postValue(historicalAtSite.append(forecastAtSite))
            }
        }

        //load the current data
        val currentAtSite = loadWithUrl(site, currentUrl, hourOfDay) ?: return null

        return NorKyst800AtSite(
            site.nr,
            currentAtSite,
            allData
        )
    }


    /**
     * Get NorKyst800AtSite data at the given site.
     *
     * First we have to load the catalog(unless previously loaded and cached). Then we have to load
     * the das of the data(attributes of variables). Finally we load the data itself, but beacuse the response is
     * large we do it in two separate requests.
     *
     * @param site site to load around
     * @param baseUrl base url to load from
     *
     * @return Norkyst800 data in the given range
     */
    private suspend fun loadWithUrl(site: Site, baseUrl: String): NorKyst800? {
        val depthRange = Options.norKyst800AtSiteDepthRange

        //////////////////////
        // DAS / ATTRIBUTES //
        //////////////////////
        val (FSOs, projection) = NorKyst800Parser.parseDAS(
            requestData(
                NorKyst800Parser.makeDasUrl(baseUrl),
                "das"
            ) ?: return null
        ) ?: return null
        val salinityFSO = FSOs[0]
        val temperatureFSO = FSOs[1]
        val uFSO = FSOs[2]
        val vFSO = FSOs[3]
        val wFSO = FSOs[4]

        ////////////////////
        // TIME AND DEPTH //
        ////////////////////

        val (time, depth) = NorKyst800Parser.parseTimeAndDepth(
            requestData(
                NorKyst800Parser.makeTimeAndDepthUrl(baseUrl),
                "time and depth"
            ) ?: return null
        ) ?: return null

        //////////////////////////////////////
        // USE PROJECTION TO GET X-Y RANGES //
        //////////////////////////////////////
        val (y, x) = projection.project(site.latLong).let { (yf, xf) ->
            Pair((yf / 800).roundToInt(), (xf / 800).roundToInt())
        }

        val radius = Options.norKyst800AtSiteRadius
        val minX = max(x - radius, 0)
        val maxX = min(max(x + radius, 0), Options.norKyst800XEnd)
        val minY = max(y - radius, 0)
        val maxY = min(max(y + radius, 0), Options.norKyst800YEnd)

        val xRange = fromClosedRange(minX, maxX, 1)
        val yRange = fromClosedRange(minY, maxY, 1)
        val timeRange = fromClosedRange(0, time.size - 1, 1)

        //////////////////
        // GET ALL DATA //
        //////////////////
        val dataString = requestData(
            NorKyst800Parser.makeFullDataUrl(
                baseUrl,
                xRange,
                yRange,
                depthRange,
                timeRange
            ),
            "all data"
        ) ?: return null

        ///////////
        // PARSE //
        ///////////
        //////////////
        // SALINITY //
        //////////////
        val salinity = NorKyst800Parser.parseNullable4DArrayFrom(
            dataString,
            salinityFSO,
            "salinity"
        ) ?: return null

        /////////////////
        // TEMPERATURE //
        /////////////////
        val temperature = NorKyst800Parser.parseNullable4DArrayFrom(
            dataString,
            temperatureFSO,
            "temperature"
        ) ?: return null

        //////////////
        // VELOCITY //
        //////////////
        val velocity = NorKyst800Parser.parseVelocity(
            dataString,
            uFSO,
            vFSO,
            wFSO
        ) ?: return null

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


    /**
     * Get NorKyst800AtSite data at the given site.
     *
     * First we have to load the catalog(unless previously loaded and cached). Then we have to load
     * the das of the data(attributes of variables). Finally we load the data itself, but beacuse the response is
     * large we do it in two separate requests.
     *
     * @param site site to load around
     * @param baseUrl url to load from
     *
     * @return Norkyst800 data in the given range
     */
    private suspend fun loadWithUrl(site: Site, baseUrl: String, timeIndex: Int): NorKyst800? {
        //////////////////////
        // DAS / ATTRIBUTES //
        //////////////////////
        val (FSOs, projection) = NorKyst800Parser.parseDAS(
            requestData(
                NorKyst800Parser.makeDasUrl(baseUrl),
                "das"
            ) ?: return null
        ) ?: return null
        val salinityFSO = FSOs[0]
        val temperatureFSO = FSOs[1]
        val uFSO = FSOs[2]
        val vFSO = FSOs[3]
        val wFSO = FSOs[4]

        //////////////////////////////////////
        // USE PROJECTION TO GET X-Y RANGES //
        //////////////////////////////////////
        val (y, x) = projection.project(site.latLong).let { (yf, xf) ->
            Pair((yf / 800).roundToInt(), (xf / 800).roundToInt())
        }

        val radius = Options.norKyst800AtSiteRadius
        val minX = max(x - radius, 0)
        val maxX = min(max(x + radius, 0), Options.norKyst800XEnd)
        val minY = max(y - radius, 0)
        val maxY = min(max(y + radius, 0), Options.norKyst800YEnd)

        val xRange = fromClosedRange(minX, maxX, 1)
        val yRange = fromClosedRange(minY, maxY, 1)
        val timeRange = fromClosedRange(timeIndex, timeIndex, 1)
        val depthRange = fromClosedRange(0, 0, 1)

        //////////////////
        // GET ALL DATA //Options.norKyst800AtSiteDepthRange
        //////////////////
        val dataString = requestData(
            NorKyst800Parser.makeFullDataUrl(
                baseUrl,
                xRange,
                yRange,
                depthRange,
                timeRange
            ),
            "all data"
        ) ?: return null

        ///////////
        // PARSE //
        ///////////
        //////////////
        // SALINITY //
        //////////////
        val salinity = NorKyst800Parser.parseNullable4DArrayFrom(
            dataString,
            salinityFSO,
            "salinity"
        ) ?: return null

        /////////////////
        // TEMPERATURE //
        /////////////////
        val temperature = NorKyst800Parser.parseNullable4DArrayFrom(
            dataString,
            temperatureFSO,
            "temperature"
        ) ?: return null

        //////////////
        // VELOCITY //
        //////////////
        val velocity = NorKyst800Parser.parseVelocity(
            dataString,
            uFSO,
            vFSO,
            wFSO
        ) ?: return null

        ///////////
        // DONE! //
        ///////////
        return NorKyst800(
            floatArrayOf(0f),
            salinity,
            temperature,
            floatArrayOf(0f),
            velocity,
            projection
        )
    }

    ///////////////
    // UTILITIES //
    ///////////////
    private suspend fun requestData(url: String, name: String): String? {
        Log.d(TAG, "Loading $name response from $url")
        val string = Fuel.get(url).awaitStringResult().onError { error ->
            Log.e(
                TAG,
                "Failed to load norkyst800data - $name from $url due to:\n $error"
            )
            return null
        }.getOrElse { err ->
            Log.e(
                TAG,
                "Unable to get NorKyst800-$name data from get request on $url. Is the URL correct? $err"
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

    private val forecastDateRegex =
        Regex(""".*fc\.(.*?)\.nc.*""")
    private val forecastDateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHH")
    private fun parseDateFromForecastUrl(fc: String): LocalDate? =
        forecastDateRegex.find(fc)?.let { match ->
            Log.d(TAG, "parsing ${match.groupValues[1]}")
            LocalDate.parse(match.groupValues[1], forecastDateFormatter)
        }

    //replace .fc. with .an.
    private fun forecastUrlIntoHistoricalUrl(fcast: String): String =
        fcast.replace(".fc.", ".an.")

}