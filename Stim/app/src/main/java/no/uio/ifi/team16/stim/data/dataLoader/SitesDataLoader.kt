package no.uio.ifi.team16.stim.data.dataLoader

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.Sites
import no.uio.ifi.team16.stim.util.LatLng
import org.json.JSONArray

/**
 * Load Sites
 * TODO figure out slicing
 */
class SitesDataLoader {
    /**
     * URL to the API
     */
    private val url: String = "https://api.fiskeridir.no/pub-aqua/api/v1/sites"

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
        for (i in 0 until sites.length()) {
            //try to parse, if succesfull add to out
            sites.getJSONObject(i)?.runCatching {
                out.add(
                    Site(
                        this.getInt("siteId"),
                        this.getString("name"),
                        LatLng(this.getDouble("latitude"), this.getDouble("longitude"))
                    )
                )
            }
        }
        return Sites(out.toList())
    }
}

