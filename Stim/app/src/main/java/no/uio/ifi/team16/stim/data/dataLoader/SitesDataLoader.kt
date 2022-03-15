package no.uio.ifi.team16.stim.data.dataLoader

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.Sites
import no.uio.ifi.team16.stim.util.LatLng

/**
 * Load Sites
 */
class SitesDataLoader {
    /**
     * URL to the API
     */
    private val url: String = "https://api.fiskeridir.no/pub-aqua/api/v1/sites?range=0-9"

    //load some data TODO figure out slicing
    //fun load(): Sites? = mockLoad()


    private fun mockLoad(): Sites {
        return Sites(
            listOf(
                Site(0, "Bingbong", LatLng(24.024409, 32.1234124)),
                Site(34, "Skiptvet", LatLng(3.1234234, 9.34234)),
                Site(666, "Helvete", LatLng(6.666, 6.666)),
            )
        )
    }

    /**
     * Loads the 10 first sites from the url
     *
     * @param position a position
     */
    suspend fun load(): Sites? {
        val responseStr = Fuel.get(
            url, listOf(
                "range" to "0-9" //parameters to query, more parameters after a ,
            )
        ).awaitString()

        if (responseStr.isEmpty()) {
            return null
        }

        return mockLoad()

        /*val response = JSONObject(responseStr)
        val properties = response.getJSONObject("properties")
        val timeseries = properties.getJSONArray("timeseries")

        if (timeseries.length() == 0) {
            return null
        }

        val first = timeseries.getJSONObject(0)

        val details = first.getJSONObject("data").getJSONObject("instant").getJSONObject("details")

        val temperature = details.getDouble("air_temperature")
        val humidity = details.getDouble("relative_humidity")
        */

        //return null
    }
}

