package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.Weather
import no.uio.ifi.team16.stim.data.dataLoader.WeatherDataLoader
import no.uio.ifi.team16.stim.util.LatLong

class WeatherRepository {

    private val TAG = "WeatherRepository"
    val dataSource = WeatherDataLoader()
    private val cache: MutableMap<LatLong, Weather> = mutableMapOf()

    /**
     * Load the data from the datasource
     */
    suspend fun getData(position: LatLong): Weather? {
        return cache.getOrPut(position) {
            return dataSource.load(position)
        }
    }
}