package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.InfectiousPressure
import no.uio.ifi.team16.stim.data.dataLoader.InfectiousPressureDataLoader
import no.uio.ifi.team16.stim.data.dataLoader.THREDDSDataLoader
import no.uio.ifi.team16.stim.util.LatLong

/**
 * Repository for infectious pressure data.
 * The Single Source of Truth(SST) for the viewmodel.
 * This is the class that should be queried for data when a class in the model layer wants a set of data.
 *
 * The repository can load data and return it, return cached data or provide mocked data(for testing)
 *
 * If constructed without parameters, it behaves normally.
 *
 * If constructed with a InfectiousPressure object it uses test behaviour; always returning
 * the given data for every query
 *
 * TODO: implement system to check if cache is not up-to-date
 */
class InfectiousPressureRepository() {
    private val TAG = "InfectiousPressureRepository"
    private val dataSource = InfectiousPressureDataLoader()
    private var cache: InfectiousPressure? = null
    private var dirty: Boolean = true

    /**
     * get the entire dataset at current time
     *
     * if the cache is not up to date(dirty), load the data anew,
     * otherwise just return the data in the cache.
     *
     * @return mocked, cached or newly loaded data.
     */
    suspend fun getDefault(): InfectiousPressure? {
        if (dirty) {
            cache = dataSource.loadDefault()
            dirty = false
        }
        return cache
    }

    /**
     * return data in a box specified by the given coordinates.
     *
     * @param latLongUpperLeft latlong of upper left corner in a box
     * @param latLongLowerRight latlong of lower right corner in a box
     * @param xStride stride between x coordinates
     * @param yStride stride between y coordinates
     * @return data of infectious pressure in the prescribed data range.
     *
     * TODO: CUT?
     *
     * @see THREDDSDataLoader.THREDDSLoad()
     */
    suspend fun get(
        latLongUpperLeft: LatLong,
        latLongLowerRight: LatLong,
        xStride: Int,
        yStride: Int
    ): InfectiousPressure? {
        if (dirty) {
            cache = dataSource.load(
                latLongUpperLeft,
                latLongLowerRight,
                xStride,
                yStride
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
    }
}