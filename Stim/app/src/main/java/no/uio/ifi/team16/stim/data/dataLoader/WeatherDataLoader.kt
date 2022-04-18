package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import no.uio.ifi.team16.stim.data.Weather
import no.uio.ifi.team16.stim.data.WeatherForecast
import no.uio.ifi.team16.stim.data.WeatherIcon
import no.uio.ifi.team16.stim.data.Weekday
import no.uio.ifi.team16.stim.util.LatLong
import org.json.JSONObject
import java.time.ZonedDateTime

/**
 * Loads weather using the LocationForecast API
 */
class WeatherDataLoader {
    private val TAG = "WeatherDataLoader"

    /**
     * URL to the API
     */
    private val url = "https://in2000-apiproxy.ifi.uio.no/weatherapi/locationforecast/2.0/compact"

    /**
     * Loads the weather forecast at a given position
     *
     * @param position a position
     */
    suspend fun load(position: LatLong): WeatherForecast? {

        val lat = "lat" to position.lat
        val lon = "lon" to position.lng

        val responseStr = Fuel.get(url, listOf(lat, lon)).awaitString()

        if (responseStr.isEmpty()) {
            Log.e(TAG, "No response from weather API at $position")
            return null
        }

        val response = JSONObject(responseStr)
        val properties = response.getJSONObject("properties")
        val timeseries = properties.getJSONArray("timeseries")

        if (timeseries.length() == 0) {
            Log.e(TAG, "No weather found at location $position")
            return null
        }

        val first = parseWeatherFromTimeseries(timeseries.getJSONObject(0))

        val now = ZonedDateTime.now()
        val nextThreeDays = mutableListOf<Weather>()
        for (i in 0 until timeseries.length()) {
            if (nextThreeDays.size == 3) break

            val series = timeseries.getJSONObject(i)
            val time = ZonedDateTime.parse(series.getString("time"))

            if (time.hour != 12 || time.dayOfMonth == now.dayOfMonth) continue

            nextThreeDays.add(parseWeatherFromTimeseries(series))
        }

        return WeatherForecast(first, nextThreeDays[0], nextThreeDays[1], nextThreeDays[2])
    }

    /**
     * Create a Weather object from one part of the forecast
     */
    private fun parseWeatherFromTimeseries(timeseries: JSONObject): Weather {
        val data = timeseries.getJSONObject("data")
        val details = data.getJSONObject("instant").getJSONObject("details")
        val temperature = details.getDouble("air_temperature")

        val next12Hours = data.getJSONObject("next_12_hours").getJSONObject("summary")
        val icon = WeatherIcon.fromMetName(next12Hours.getString("symbol_code"))

        val weekday = Weekday.fromISOString(timeseries.getString("time"))
        return Weather(temperature, icon, weekday)
    }
}