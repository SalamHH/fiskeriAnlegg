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

    /*
    /**
     * The most general load
     * loads the specified ranges of data
     *
     * it is currently not possible to sample at a given depth in meters, only on index.
     * The same holds for time.
     *
     * In any case, index 0 for depth corresponds to the surface and time index 0 corresponds to
     * the current time(TODO: specify)
     *
     * @param latitudeFrom upper left corner latitude
     * @param latitudeTo lower right corner longitude
     * @param latitudeResolution resolution of latitude, a resolution of 0.001 means that we sample latitudes 0.001 apart
     * @param longitudeFrom upper left corner longitude
     * @param longitudeTo lower right corner longitude
     * @param longitudeResolution resolution of longitude, a resolution of 0.001 means that we sample longitudes 0.001 apart
     * @param depthFrom depth index to sample from, 0 is at the surface, higher index means lower
     * @param depthStride stride between from and to depth index
     * @param depthTo depth index to sample to, 0 is at the surface, higher index means lower
     * @param timeFrom time index to sample from, 0 is current time (TODO CHECK), higher index means further ahead in time
     * @param timeStride time between from and to depth index
     * @param timeTo time index to sample to, 0 is current time, higher index means further ahead in time
     */
    suspend fun get(
        latitudeFrom: Float,
        latitudeTo: Float,
        latitudeResolution: Float, //TODO: not used, yet, must find solution to convert to yStride, using Options defualt
        longitudeFrom: Float,
        longitudeTo: Float,
        longitudeResolution: Float, //TODO: not used, yet, must find solution to convert to Xstride, using Options defualt
        depthFrom: Int,
        depthStride: Int,
        depthTo: Int,
        timeFrom: Int,
        timeStride: Int,
        timeTo: Int,
    ): NorKyst800? {
        if (!mocked) {
            if (dirty) {
                cache = dataSource.load(
                    latitudeFrom, latitudeTo, latitudeResolution,
                    longitudeFrom, longitudeTo, longitudeResolution,
                    depthFrom, depthStride, depthTo,
                    timeFrom, timeStride, timeTo
                )
                dirty = false
            }
        }
        return cache
    }

    /**
     * The most general load
     * loads the specified ranges of data
     *
     * convenience loader, gets depth and timeranges as strings rather than int indexes
     * @see load(Float, Float, Float, FLoat, FLoat, Float, Int, Int, Int, Int, Int, Int)
     *
     * @param latitudeFrom upper left corner latitude
     * @param latitudeTo lower right corner longitude
     * @param latitudeResolution resolution of latitude, a resolution of 0.001 means that we sample latitudes 0.001 apart
     * @param longitudeFrom upper left corner longitude
     * @param longitudeTo lower right corner longitude
     * @param longitudeResolution resolution of longitude, a resolution of 0.001 means that we sample longitudes 0.001 apart
     * @param depthRange depth as a range with format from:stride:to
     * @param timeRange depth as a range with format from:stride:to
     */
    suspend fun get(
        latitudeFrom: Float,
        latitudeTo: Float,
        latitudeResolution: Float, //TODO: not used, yet, must find solution to convert to yStride, using Options defualt
        longitudeFrom: Float,
        longitudeTo: Float,
        longitudeResolution: Float, //TODO: not used, yet, must find solution to convert to Xstride, using Options defualt
        depthRange: String,
        timeRange: String
    ): NorKyst800? {
        if (!mocked) {
            if (dirty) {
                cache = dataSource.load(
                    latitudeFrom, latitudeTo, latitudeResolution,
                    longitudeFrom, longitudeTo, longitudeResolution,
                    depthRange,
                    timeRange
                )
                dirty = false
            }
        }
        return cache
    }
    */

    /**
     * get the data.
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
}