package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.Sites
import no.uio.ifi.team16.stim.data.dataLoader.SitesDataLoader
import android.util.Log

/**
 * Repository for sites.
 *
 * The cache maps a municipalitynumber to a collection of sites
 */
class SitesRepository {
    private val TAG = "SitesRepository"
    private val dataSource = SitesDataLoader()

    //cache maps a municipalitynr to a list of sites
    var cache: MutableMap<String, Sites?> = mutableMapOf()

    //some sites, prefferably we will use the above cache, but not all municipalities have Sites
    var someData: Sites? = null

    /**
     * load the sites at the given municipalitycode
     */
    suspend fun getData(municipalityCode: String): Sites? {
        var sites = cache[municipalityCode]
        if (sites != null) {
            return sites
        }
        sites = dataSource.loadDataByMunicipalityCode(municipalityCode)
        cache[municipalityCode] = sites
        return sites
    }

    suspend fun getDataByName(name: String): Sites? {
        var sites = cache[name]
        if (sites != null) {
            return sites
        }
        sites = dataSource.loadDataByName(name)
        cache[name] = sites
        return sites
    }




    /**
     * Load some data, the 100 first sites.
     * used initially to just get some placeholder data,
     * does not properly cache
     */
    /*suspend fun getSomeData(): Sites? {
        Log.d(TAG, "loading 100 first sitesdata from repository")
        someData = dataSource.loadSomeData()
        Log.d(TAG, "loading 100 first sitesdata from repository - DONE")
        return someData
    }*/
}