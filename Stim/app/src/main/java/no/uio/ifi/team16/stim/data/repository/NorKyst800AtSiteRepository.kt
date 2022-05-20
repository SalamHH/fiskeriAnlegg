package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.NorKyst800AtSite
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.dataLoader.NorKyst800AtSiteDataLoader
import no.uio.ifi.team16.stim.util.getOrPutOrPass

class NorKyst800AtSiteRepository {
    private val dataSource = NorKyst800AtSiteDataLoader()
    private var cache: MutableMap<Int, NorKyst800AtSite> = mutableMapOf()

    /**
     * get NorKyst800 data around a specified site
     *
     * if the cache is not up to date(dirty), load the data anew,
     * otherwise just return the data in the cache.
     *
     * @param site site to load data around
     * @return mocked, cached or newly loaded data.
     */
    suspend fun getDataAtSite(site: Site): NorKyst800AtSite? {
        return cache.getOrPutOrPass(site.nr) {
            dataSource.load(site)
        }
    }

    /**
     * Empties the cache. Call this in case of low memory warning
     */
    fun clearCache() {
        cache.clear()
    }
}