package no.uio.ifi.team16.stim.data.dataLoader

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import no.uio.ifi.team16.stim.data.Weather
import no.uio.ifi.team16.stim.util.LatLong
import org.json.JSONObject

/**
 * Loads weather using the LocationForecast API
 */
class WeatherDataLoader {

    /**
     * URL to the API
     */
    private val url = "https://in2000-apiproxy.ifi.uio.no/weatherapi/locationforecast/2.0/compact"

    /**
     * Loads the current weather at a given position
     *
     * @param position a position
     */
    suspend fun load(position: LatLong): Weather? {

        val lat = "lat" to position.lat
        val lon = "lon" to position.lng

        val responseStr = Fuel.get(url, listOf(lat, lon)).awaitString()

        if (responseStr.isEmpty()) {
            return null
        }

        val response = JSONObject(responseStr)
        val properties = response.getJSONObject("properties")
        val timeseries = properties.getJSONArray("timeseries")

        if (timeseries.length() == 0) {
            return null
        }

        val first = timeseries.getJSONObject(0)

        val details = first.getJSONObject("data").getJSONObject("instant").getJSONObject("details")

        val temperature = details.getDouble("air_temperature")
        val humidity = details.getDouble("relative_humidity")

        return Weather(position, temperature, humidity)
    }
}