package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.InfectiousPressureTimeSeries
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.dataLoader.InfectiousPressureTimeSeriesDataLoader
import kotlin.ranges.IntProgression.Companion.fromClosedRange

/**
 * Repository for infectious pressure time series data.
 * The Single Source of Truth(SST) for the viewmodel.
 * This is the class that should be queried for data when a class in the model layer wants a set of data.
 *
 * The repository can load data and return data or return cached data
 */
class InfectiousPressureTimeSeriesRepository() {
    private val TAG = "InfectiousPressureTimeSeriesRepository"
    private val dataSource = InfectiousPressureTimeSeriesDataLoader()
    private var cache: MutableMap<Int, InfectiousPressureTimeSeries> = mutableMapOf()

    /**
     * get infectious pressure timeseries data at a given site.
     *
     * if the cache is not up to date(dirty), load the data anew,
     * otherwise just return the data in the cache.
     *
     * @param site site to load data around
     * @param weeksFromNow amount of weeks from th current week to load
     * @return mocked, cached or newly loaded data.
     */
    suspend fun getDataAtSite(site: Site, weeksFromNow: Int): InfectiousPressureTimeSeries? {
        return cache.getOrPutOrPass(site.nr) {
            dataSource.load(site, fromClosedRange(0, weeksFromNow, 1))
        }
    }

    /**
     * get infectious pressure timeseries data at a given site.
     *
     * if the cache is not up to date(dirty), load the data anew,
     * otherwise just return the data in the cache.
     *
     * @param site site to load data around
     * @param weekRange range of weeks(from the current week!) to load from
     * @return mocked, cached or newly loaded data.
     */
    suspend fun getDataAtSite(
        site: Site,
        weekRange: IntProgression
    ): InfectiousPressureTimeSeries? =
        cache.getOrPutOrPass(site.nr) {
            dataSource.load(site, weekRange)
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
    }
}