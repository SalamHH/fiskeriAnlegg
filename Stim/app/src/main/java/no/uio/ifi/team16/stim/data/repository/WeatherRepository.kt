package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.Weather
import no.uio.ifi.team16.stim.data.dataLoader.WeatherDataLoader

class WeatherRepository : Repository<Weather, WeatherDataLoader>() {
    private val TAG = "WeatherRepository"
    override val dataSource = WeatherDataLoader()

    //load the data from the datasource
    //see Repository.getData()
    override fun getData(): Weather? {
        throw NotImplementedError()
        /*
        Log.d(TAG, "loading weatherdata from repository")
        if (!mocked) {
            if (dirty) {
                cache = dataSource.load()
                dirty = false
            }
        }
        Log.d(TAG, "loading weatherdata from repository - DONE")

        return cache
        */
    }
}