package no.uio.ifi.team16.stim.data.dataLoader

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.Sites
import no.uio.ifi.team16.stim.util.LatLng
import org.json.JSONArray
import org.json.JSONObject

/**
 * Load Sites
 * TODO figure out slicing
 */
class SitesDataLoader {
    /**
     * URL to the API
     */
    private val url: String = "https://api.fiskeridir.no/pub-aqua/api/v1/sites"

    //extend JSONArray with an iterator(extend to iterable -> use map?)
    @Suppress("UNCHECKED_CAST")
    operator fun JSONArray.iterator(): Iterator<JSONObject> =
        (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

    /**
     * Loads the 10 first sites from the url
     *
     * @param position a position
     */
    suspend fun load(): Sites? {
        val responseStr = Fuel.get(
            url, listOf(
                "range" to "0-9" //parameters to query, more parameters after a ","
            )
        ).awaitString()

        if (responseStr.isEmpty()) {
            return null
        }


        val sites = JSONArray(responseStr)
        var out: MutableList<Site> = mutableListOf()
        for (site in sites) {
            out.add(
                Site(
                    site.getInt("siteId"),
                    site.getString("name"),
                    LatLng(site.getDouble("latitude"), site.getDouble("longitude"))
                )
            )
        }
        return Sites(out.toList())
    }
}

