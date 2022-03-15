package no.uio.ifi.team16.stim.data.repository

import android.util.Log
import no.uio.ifi.team16.stim.data.InfectiousPressure
import no.uio.ifi.team16.stim.data.dataLoader.InfectiousPressureDataLoader

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
class InfectiousPressureRepository :
    Repository<InfectiousPressure, InfectiousPressureDataLoader>() {
    private val TAG = "InfectiousPressureRepository"
    override val dataSource = InfectiousPressureDataLoader()
    private var cache: InfectiousPressure? = null

    /**
     * get SOME of the data.
     * If in testmode(mocked data), return the testdata
     * otherwise;
     * if the cache is not up to date(dirty), load the data anew,
     * otherwise just return the data in the cache.
     *
     * @return mocked, cached or newly loaded data.
     */
    fun getSomeData(): InfectiousPressure? {
        Log.d(TAG, "loading infectiousdata from repository")
        if (!mocked && dirty) {
            cache = dataSource.load()
            dirty = false
        }
        Log.d(TAG, "loading infectiousdata from repository - DONE")
        return cache
    }
}