package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.data.dataLoader.NorKyst800DataLoader
import no.uio.ifi.team16.stim.util.LatLong
import kotlin.ranges.IntProgression.Companion.fromClosedRange

class NorKyst800Repository {
    private val TAG = "NorKyst800Repository"
    private val dataSource = NorKyst800DataLoader()
    private var cache: NorKyst800? = null
    var mocked: Boolean = false
    var dirty: Boolean = true

    /**
     * get the data.
     * If in testmode(mocked data), return the testdata
     * otherwise;
     * if the cache is not up to date(dirty), load the data anew,
     * otherwise just return the data in the cache.
     *
     * @return mocked, cached or newly loaded data.
     */
    fun getDefault(): NorKyst800? {
        //Log.d(TAG, "loading infectiousdata from repository")
        if (!mocked) {
            if (dirty) {
                cache = dataSource.load(
                    LatLong(1.0, 2.0),
                    LatLong(3.0, 4.0),
                    5,
                    6,
                    fromClosedRange(7, 8, 9),
                    fromClosedRange(10, 11, 12)
                ) //TODO: currently mocked, only a 2x2x2x2 grid
                dirty = false
            }
        }
        //Log.d(TAG, "loading infectiousdata from repository - DONE")

        return cache
    }

    /**
     * load a part of the dataset
     * TODO: find cache strategy, patchwork will be very ineffecient
     */
    fun getData(
        latLongUpperLeft: LatLong,
        latLongLowerRight: LatLong,
        latitudeResolution: Int,
        longitudeResolution: Int,
        depthRange: IntProgression,
        timeRange: IntProgression
    ): NorKyst800? {
        if (!mocked) {
            if (dirty) {
                cache = dataSource.load(
                    latLongUpperLeft,
                    latLongLowerRight,
                    latitudeResolution,
                    longitudeResolution,
                    depthRange,
                    timeRange
                ) //TODO: currently mocked, only a 2x2x2x2 grid
                dirty = false
            }
        }
        return cache
    }
}