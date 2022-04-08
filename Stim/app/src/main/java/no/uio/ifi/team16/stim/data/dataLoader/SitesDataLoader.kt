package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.coroutines.awaitString
import no.uio.ifi.team16.stim.data.*
import no.uio.ifi.team16.stim.util.LatLong
import no.uio.ifi.team16.stim.util.Options
import org.json.JSONArray

/**
 * Load Municipality
 *
 * Shortcomings:
 * The municipality API can only respond with at moost 100 municipality at a time,
 * hopefully we will not need more than that, but otherwise we will have to merge
 * several responses into a single response
 */
class SitesDataLoader {
    private val TAG = "SitesDataLoader"

    /**
     * URL to the API
     */
    private val url: String = "https://api.fiskeridir.no/pub-aqua/api/v1/sites"

    /**
     * Loads the 100 first municipality from the url, with the given query
     *
     * See the municipality-API parameters banner for all parameters.
     * https://api.fiskeridir.no/pub-aqua/api/swagger-ui/index.html?configUrl=/pub-aqua/api/api-docs/swagger-config#/site-resource/municipality
     *
     * @param parameters list of parameters to the query, can be in the form of List<Pair<String,Any?>>,
     * or using Fules own syntax, listof(param1string to param1, ...)
     */
    private suspend fun loadWithParameters(parameters: Parameters?): List<Site>? {
        var responseStr = ""
        try {
            responseStr = Fuel.get(url, parameters).awaitString()
        } catch (e: Exception) {
            Log.e(TAG, "Kunne ikke hente municipality med params: $parameters", e)
        }

        if (responseStr.isEmpty()) {
            return null
        }

        val sites = JSONArray(responseStr)
        val out: MutableList<Site> = mutableListOf()

        for (i in 0 until sites.length()) {
            //try to parse, if succesfull add to out, otherwise just try next
            sites.getJSONObject(i)?.runCatching {
                out.add(
                    Site(
                        this.getInt("siteId"),
                        this.getString("name"),
                        LatLong(this.getDouble("latitude"), this.getDouble("longitude")),
                        //a site might not have an areaplacement(?)
                        this.getJSONObject("placement")?.let { APJSON ->
                            AreaPlacement(
                                APJSON.getInt("municipalityCode"),
                                APJSON.getString("municipalityName"),
                                APJSON.getInt("countyCode"),
                                runCatching { //many municipality have no production area, return null
                                    ProdArea(
                                        APJSON.getInt("prodAreaCode"),
                                        APJSON.getString("prodAreaName"),
                                        ProdAreaStatus.valueOf(APJSON.getString("prodAreaStatus"))
                                    )
                                }.getOrDefault(null)
                            )
                        },
                        this.getDouble("capacity"),
                        this.getString("placementType"),
                        this.getString("waterType")
                    )
                )
            }?.onFailure { failure ->
                Log.w(TAG, "Failed to create a site due to: $failure")
            }
        }
        return out.toList()
    }

    /**
     * @see loadWithParameters
     */
    suspend fun loadDataByMunicipalityCode(municipalityCode: String): Sites? =
        loadWithParameters(
            listOf(
                "range" to Options.sitesRange,
                "municipality-code" to municipalityCode
            )
        )?.let { sites ->
            Municipality(
                municipalityCode,
                sites
            )
        }

    suspend fun loadDataByName(name: String): Site? {
        val sites = loadWithParameters(
            listOf(
                "range" to Options.sitesRange,
                "name" to name
            )
        )
        return sites?.getOrNull(0)
    }

    /**
     * @see loadWithParameters
     */
    /*suspend fun loadDataByCountyCode(countyCode: Int): Municipality? =
        loadWithParameters(
            listOf(
                "range" to Options.sitesRange,
                "county-code" to countyCode.toString()
            )
        )*/

    /**
     * @see loadWithParameters
     */
    /*suspend fun loadDataByProductionAreaCode(paCode: Int): Municipality? =
        loadWithParameters(
            listOf(
                "range" to Options.sitesRange,
                "production-area-code" to paCode.toString()
            )
        )*/
}

