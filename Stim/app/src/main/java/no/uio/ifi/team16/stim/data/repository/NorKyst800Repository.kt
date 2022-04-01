package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.data.dataLoader.NorKyst800DataLoader

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
    fun getData(): NorKyst800? {
        //Log.d(TAG, "loading infectiousdata from repository")
        if (!mocked) {
            if (dirty) {
                cache = dataSource.load() //TODO: currently mocked, only a 2x2x2x2 grid
                dirty = false
            }
        }
        //Log.d(TAG, "loading infectiousdata from repository - DONE")

        return cache
    }
}