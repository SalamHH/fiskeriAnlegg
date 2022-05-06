package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.WeatherForecast
import no.uio.ifi.team16.stim.data.dataLoader.WeatherDataLoader

class WeatherRepository {

    private val TAG = "WeatherRepository"
    private val dataSource = WeatherDataLoader()
    private val cache: MutableMap<Site, WeatherForecast?> = mutableMapOf()

    /**
     * Load the data from the datasource
     */
    suspend fun getWeatherForecast(site: Site): WeatherForecast? {
        var forecast = cache[site]

        if (forecast == null) {
            forecast = dataSource.load(site.latLong)
            cache[site] = forecast
            site.weatherForecast = forecast
        }

        return forecast
    }

    /**
     * Empties the cache. Call this in case of low memory warning
     */
    fun clearCache() {
        cache.clear()
    }
}