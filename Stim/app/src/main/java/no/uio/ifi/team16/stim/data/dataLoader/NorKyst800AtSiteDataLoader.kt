package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.result.getOrElse
import com.github.kittinunf.result.onError
import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.data.NorKyst800AtSite
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.dataLoader.parser.NorKyst800Parser
import no.uio.ifi.team16.stim.util.Options
import no.uio.ifi.team16.stim.util.project
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.ranges.IntProgression.Companion.fromClosedRange

/**
 * Dataloader for loading NorKyst800 data around a specified site
 **/
class NorKyst800AtSiteDataLoader {
    private val TAG = "NorKyst800AtSiteDataLoader"

    //the catalog url is assumed to be time-invariant. Here all entries are listed.
    private val catalogUrl =
        "https://thredds.met.no/thredds/catalog/fou-hi/norkyst800m-1h/catalog.html"

    /////////////
    // LOADERS //
    /////////////

    suspend fun load(
        site: Site
    ): NorKyst800AtSite? {
        val forecastUrl = loadForecastUrl() ?: run {
            Log.e(
                TAG,
                "Failed to load the forecast URL from the catalog, is the catalog URL correct?"
            )
            return null
        }
        val currentUrl = forecastUrlIntoCurrentUrl(forecastUrl)
        //load for each set
        val forecastAtSite = loadWithUrl(site, forecastUrl)
        val currentAtSite = loadWithUrl(site, currentUrl)
        //now merge the two datasets
        return if (currentAtSite == null) {
            if (forecastAtSite == null) { //both unsuccesfull
                null
            } else {
                NorKyst800AtSite(
                    site.nr,
                    forecastAtSite
                )
            }
        } else {
            if (forecastAtSite == null) {
                NorKyst800AtSite(
                    site.nr,
                    currentAtSite
                )
            } else { //both succesfull
                NorKyst800AtSite(
                    site.nr,
                    currentAtSite.append(forecastAtSite)
                )
            }
        }
    }


    /**
     * Get NorKyst800AtSite data at the given site.
     *
     * First we have to load the catalog(unless previously loaded and cached). Then we have to load
     * the das of the data(attributes of variables). Finally we load the data itself, but beacuse the response is
     * large we do it in two separate requests.
     *
     * @param site site to load around
     * @param url url to load from
     *
     * @return Norkyst800 data in the given range
     */
    suspend fun loadWithUrl(site: Site, baseUrl: String): NorKyst800? {
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

    ///////////////
    // UTILITIES //
    ///////////////
    private suspend fun requestData(url: String, name: String): String? {
        val string =
            Fuel.get(url).awaitStringResult().onError { error ->
                Log.e(TAG, "Failed to load norkyst800data - $name from $url due to:\n $error")
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

    //replace .fc. with .an.
    private fun forecastUrlIntoCurrentUrl(fcast: String): String =
        fcast.replace(".fc.", ".an.")
}