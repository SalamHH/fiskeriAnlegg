package no.uio.ifi.team16.stim.data.repository

import android.util.Log
import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.data.dataLoader.NorKyst800DataLoader

class NorKyst800Repository : Repository<NorKyst800, NorKyst800DataLoader>() {
    private val TAG = "NorKyst800Repository"
    override val dataSource = NorKyst800DataLoader()

    /**
     * get the data.
     * If in testmode(mocked data), return the testdata
     * otherwise;
     * if the cache is not up to date(dirty), load the data anew,
     * otherwise just return the data in the cache.
     *
     * @return mocked, cached or newly loaded data.
     */
    override fun getData(): NorKyst800? {
        Log.d(TAG, "loading infectiousdata from repository")
        if (!mocked) {
            if (dirty) {
                cache = dataSource.load()
                dirty = false
            }
        }
        Log.d(TAG, "loading infectiousdata from repository - DONE")

        return cache
    }
}