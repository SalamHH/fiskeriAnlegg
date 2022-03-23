package no.uio.ifi.team16.stim.data.repository

import android.util.Log
import no.uio.ifi.team16.stim.data.Sites
import no.uio.ifi.team16.stim.data.dataLoader.SitesDataLoader

class SitesRepository : Repository<Sites, SitesDataLoader>() {
    private val TAG = "SitesRepository"
    override val dataSource = SitesDataLoader()

    //cache maps a municipalitynr to a list of sites
    var cache: MutableMap<Int, Sites?> = mutableMapOf()

    //some sites, prefferably we will use the above cache, but not all municipalities have Sites
    var someData: Sites? = null

    /**
     * load the sites at the given municipalitycode
     */
    suspend fun getData(municipalityCode: Int): Sites? {
        Log.d(TAG, "loading sitesdata at municipalitycode $municipalityCode from repository")

        Log.d(TAG, "loading sitesdata at municipalitycode $municipalityCode from repository - DONE")
        return cache.put(
            municipalityCode,
            dataSource.loadDataByMunicipalityCode(municipalityCode)
        ) //TODO: reimplement putifabsent...
    }

    /**
     * Load some data, the 100 first sites.
     * used initially to just get some placeholder data,
     * does not properly cache
     */
    suspend fun getSomeData(): Sites? {
        Log.d(TAG, "loading 100 first sitesdata from repository")
        someData = dataSource.loadSomeData()
        Log.d(TAG, "loading 100 first sitesdata from repository - DONE")
        return someData
    }
}