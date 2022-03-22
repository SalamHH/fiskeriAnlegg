package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import no.uio.ifi.team16.stim.data.*
import no.uio.ifi.team16.stim.util.LatLng
import org.json.JSONArray

/**
 * Load Sites
 * TODO figure out slicing
 */
class SitesDataLoader {
    val TAG = "SitesDataLoader"

    /**
     * URL to the API
     */
    private val url: String = "https://api.fiskeridir.no/pub-aqua/api/v1/sites"

    /**
     * Loads the 100 first sites from the url
     *
     * @param position a position
     */
    suspend fun load(): Sites? {
        val responseStr = Fuel.get(
            url, listOf(
                "range" to "0-99" //parameters to query, more parameters after a ","
            )
        ).awaitString()

        if (responseStr.isEmpty()) {
            return null
        }

        val sites = JSONArray(responseStr)
        var out: MutableList<Site> = mutableListOf()
        for (i in 0 until sites.length()) {
            //try to parse, if succesfull add to out, otherwise just try next
            sites.getJSONObject(i)?.runCatching {
                out.add(
                    Site(
                        this.getInt("siteId"),
                        this.getString("name"),
                        LatLng(this.getDouble("latitude"), this.getDouble("longitude")),
                        //a site might not have an areaplacement(?)
                        this.getJSONObject("placement")?.let { APJSON ->
                            AreaPlacement(
                                APJSON.getInt("municipalityCode"),
                                APJSON.getString("municipalityName"),
                                APJSON.getInt("countyCode"),
                                runCatching { //many sites have no production area, return null
                                    ProdArea(
                                        APJSON.getInt("prodAreaCode"),
                                        APJSON.getString("prodAreaName"),
                                        ProdAreaStatus.valueOf(APJSON.getString("prodAreaStatus"))
                                    )
                                }.getOrDefault(null)
                            )
                        }
                    )
                )
            }?.onFailure() { failure ->
                Log.w(TAG, "Failed to create a site due to: $failure")
            }
        }
        return Sites(out.toList())
    }
}

