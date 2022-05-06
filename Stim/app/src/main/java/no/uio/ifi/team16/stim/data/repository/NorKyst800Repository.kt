package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.data.dataLoader.NorKyst800DataLoader
import no.uio.ifi.team16.stim.data.dataLoader.THREDDSDataLoader
import no.uio.ifi.team16.stim.util.LatLong

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
     * return data in a box specified by the given coordinates.
     *
     * @param latLongUpperLeft latlong of upper left corner in a box
     * @param latLongLowerRight latlong of lower right corner in a box
     * @param depthRange range of depth indexes to load from
     * @param timeRange range of time indexes to load from
     * @param xStride stride between x coordinates
     * @param yStride stride between y coordinates
     * @return data of infectious pressure in the prescribed data range.
     *
     * @see THREDDSDataLoader.THREDDSLoad()
     */
    suspend fun get(
        latLongUpperLeft: LatLong,
        latLongLowerRight: LatLong,
        depthRange: IntProgression,
        timeRange: IntProgression,
        xStride: Int,
        yStride: Int
    ): NorKyst800? {
        if (!mocked && dirty) {
            cache = dataSource.load(
                latLongUpperLeft,
                latLongLowerRight,
                xStride,
                yStride,
                depthRange,
                timeRange
            )
            dirty = false
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
     * @param timeRange range of time indexes to load from
     * @return data of infectious pressure in the prescribed data range.
     *
     * @see THREDDSDataLoader.THREDDSLoad()
     */
    suspend fun get(
        xRange: IntProgression,
        yRange: IntProgression,
        depthRange: IntProgression,
        timeRange: IntProgression
    ): NorKyst800? {
        if (!mocked && dirty) {
            cache = dataSource.load(
                xRange,
                yRange,
                depthRange,
                timeRange
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