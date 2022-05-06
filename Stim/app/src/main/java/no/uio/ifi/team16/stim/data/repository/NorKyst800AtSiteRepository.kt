package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.NorKyst800AtSite
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.dataLoader.NorKyst800AtSiteDataLoader

class NorKyst800AtSiteRepository {
    private val TAG = "NorKyst800AtSiteRepository"
    private val dataSource = NorKyst800AtSiteDataLoader()
    private var cache: MutableMap<Int, NorKyst800AtSite> = mutableMapOf()
    var mocked: Boolean = false
    var dirty: Boolean = true

    /**
     * get NorKyst800 data around a specified site
     *
     * If in testmode(mocked data), return the testdata
     * otherwise;
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

    ///////////////
    // UTILITIES //
    ///////////////
    /**
     * same as Map.getOrPut, but if the put value resuls in null, don't put
     *
     * if the key exists, return its value
     * if not, evaluate default,
     *      if default succeeds(not null) put it in the cache, and return the value
     *      if default fails (null), dont put anything in cache(get or put would put null in cache) and return null
     * @see MutableMap.getOrPut
     */
    private inline fun <K, V> MutableMap<K, V>.getOrPutOrPass(key: K, default: () -> V?): V? =
        getOrElse(key) {
            default()?.let { value ->
                this[key] = value
                value
            }
        }

    /**
     * Empties the cache. Call this in case of low memory warning
     */
    fun clearCache() {
        cache.clear()
        dirty = true
    }
}