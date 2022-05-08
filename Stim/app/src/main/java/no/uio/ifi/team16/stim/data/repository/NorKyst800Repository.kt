package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.data.dataLoader.NorKyst800DataLoader
import no.uio.ifi.team16.stim.data.dataLoader.THREDDSDataLoader

class NorKyst800Repository {
    private val TAG = "NorKyst800Repository"
    private val dataSource = NorKyst800DataLoader()
    private var cache: NorKyst800? = null
    var mocked: Boolean = false
    var dirty: Boolean = true

    /**
     * get the default data, as specified by Options
     * If in testmode(mocked data), return the testdata
     * otherwise;
     * if the cache is not up to date(dirty), load the data anew,
     * otherwise just return the data in the cache.
     *
     * @return mocked, cached or newly loaded data.
     */
    suspend fun getDefaultData(): NorKyst800? {
        if (!mocked) {
            if (dirty) {
                cache = dataSource.loadDefault()
                dirty = false
            }
        }
        return cache
    }

    /**
     * return data in the given range.
     * Usually the data is in a 2602x902x?x? y-x-depth-time-grid
     *
     * @param xRange range of x-coordinates to get
     * @param yRange range of y-coordinates to get
     * @param depthRange range of depth indexes to load from
     * @return data of infectious pressure in the prescribed data range.
     *
     * @see THREDDSDataLoader.THREDDSLoad()
     */
    suspend fun get(
        xRange: IntProgression,
        yRange: IntProgression,
        depthRange: IntProgression
    ): NorKyst800? {
        if (!mocked && dirty) {
            cache = dataSource.load(
                xRange,
                yRange,
                depthRange
            )
            dirty = false
        }
        return cache
    }

    /**
     * Empties the cache. Call this in case of low memory warning
     */
    fun clearCache() {
        cache = null
        dirty = true
    }
}