package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import no.uio.ifi.team16.stim.data.NorKyst800AtSite
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.util.Options
import no.uio.ifi.team16.stim.util.project
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.ranges.IntProgression.Companion.fromClosedRange

/**
 * Dataloader for loading NorKyst800 data around a specified site
 **/
class NorKyst800AtSiteDataLoader : NorKyst800DataLoader() {
    private val TAG = "NorKyst800AtSiteDataLoader"

    private val catalogUrl =
        "https://thredds.met.no/thredds/catalog/fou-hi/norkyst800m-1h/catalog.html"

    private val radius = Options.norKyst800AtSiteRadius

    val projection = Options.defaultProjection()

    /////////////
    // LOADERS //
    /////////////
    /**
     * Get NorKyst800 data around a given site. The amount of grid cells to get around is specified in
     * Options
     *
     * @param site site to load data around
     * @return Norkyst800 data in the given range
     */
    suspend fun load(
        site: Site
    ): NorKyst800AtSite? {
        val baseUrl = loadForecastUrl() ?: run {
            Log.e(
                TAG,
                "Failed to load the forecast URL form the catalog, is the catalog URL correct?"
            )
            return null
        }

        val (y, x) = projection.project(site.latLong).let { (yf, xf) ->
            Pair((yf / 800).roundToInt(), (xf / 800).roundToInt())
        }

        val minX = max(x - radius, 0)
        val maxX = min(max(x + radius, 0), Options.norKyst800XEnd)
        val minY = max(y - radius, 0)
        val maxY = min(max(y + radius, 0), Options.norKyst800YEnd)

        val xRange = fromClosedRange(minX, maxX, 1)
        val yRange = fromClosedRange(minY, maxY, 1)

        var nork = NorKyst800DataLoader().load(
            xRange,
            yRange,
            Options.norKyst800AtSiteDepthRange,
            Options.norKyst800AtSiteTimeRange
        )

        return NorKyst800AtSite(site.nr, nork ?: return null)
    }
}