package no.uio.ifi.team16.stim.data.repository

import android.util.Log
import no.uio.ifi.team16.stim.data.InfectiousPressureTimeSeries
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.dataLoader.InfectiousPressureTimeSeriesDataLoader

/**
 * Repository for infectious pressure time series data.
 * The Single Source of Truth(SST) for the viewmodel.
 * This is the class that should be queried for data when a class in the model layer wants a set of data.
 *
 * The repository can load data and return data, return cached data or provide mocked data(for testing)
 *
 * If constructed without parameters, it behaves normally.
 *
 * If constructed with a InfectiousPressureTimeSeries object it uses test behaviour; always returning
 * the given data for every query
 */
class InfectiousPressureTimeSeriesRepository() {
    private val TAG = "InfectiousPressureTimeSeriesRepository"
    private val dataSource = InfectiousPressureTimeSeriesDataLoader()
    private var cache: MutableMap<Int, InfectiousPressureTimeSeries> = mutableMapOf()
    var mocked: Boolean = false

    constructor(infectiousPressureTimeSeries: Map<Int, InfectiousPressureTimeSeries>) : this() {
        mocked = true
        cache = infectiousPressureTimeSeries.toMutableMap()
    }

    /**
     * get the most recent data from the infectious pressure catalog
     *
     * If in testmode(mocked data), return the testdata
     * otherwise;
     * if the cache is not up to date(dirty), load the data anew,
     * otherwise just return the data in the cache.
     *
     * @return mocked, cached or newly loaded data.
     */
    fun getDataAtSite(site: Site, weeksFromNow: Int): InfectiousPressureTimeSeries? {
        //Log.d(TAG, "loading infectioustimeseriesdata from repository")
        if (!mocked) {
            cache.getOrPutOrPass(site.id) {
                dataSource.load(site, weeksFromNow)
            }
        }
        //Log.d(TAG, "loading current infectioustimeseriesdata from repository - DONE")
        return cache[site.id]
    }

    /**
     * get the most recent data from the infectious pressure catalog
     *
     * If in testmode(mocked data), return the testdata
     * otherwise;
     * if the cache is not up to date(dirty), load the data anew,
     * otherwise just return the data in the cache.
     *
     * @return mocked, cached or newly loaded data.
     */
    fun getDataAtSite(site: Site, weekRange: IntProgression): InfectiousPressureTimeSeries? {
        //Log.d(TAG, "loading infectioustimeseriesdata from repository")
        if (!mocked) {
            cache.getOrPutOrPass(site.id) {
                dataSource.load(site, weekRange)
            }
        }
        //Log.d(TAG, "loading current infectioustimeseriesdata from repository - DONE")
        return cache[site.id]
    }

    ///////////////
    // UTILITIES //
    ///////////////
    /**
     * same as Map.getOrPut, but if the put value resuls in null, don't put
     * @see MutableMap.getOrPut
     */
    private fun <K, V> MutableMap<K, V>.getOrPutOrPass(key: K, default: () -> V?) {
        this.getOrElse(key) {
            default()?.let { value ->
                this.put(key, value)
            }
        }
    }
}