package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.Weather
import no.uio.ifi.team16.stim.data.dataLoader.WeatherDataLoader
import no.uio.ifi.team16.stim.util.LatLng

class WeatherRepository : Repository<Weather, WeatherDataLoader>() {

    private val TAG = "WeatherRepository"
    override val dataSource = WeatherDataLoader()
    private val cache: MutableMap<LatLng, Weather> = mutableMapOf()

    /**
     * Load the data from the datasource
     */
    suspend fun getData(position: LatLng): Weather? {
        return cache.getOrPut(position) {
            return dataSource.load(position)
        }
    }
}