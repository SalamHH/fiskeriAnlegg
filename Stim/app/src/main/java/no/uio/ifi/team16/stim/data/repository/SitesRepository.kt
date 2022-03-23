package no.uio.ifi.team16.stim.data.repository

import android.util.Log
import no.uio.ifi.team16.stim.data.Sites
import no.uio.ifi.team16.stim.data.dataLoader.SitesDataLoader

class SitesRepository : Repository<Sites, SitesDataLoader>() {
    private val TAG = "SitesRepository"
    override val dataSource = SitesDataLoader()
    var cache: Sites? = null

    //load the data from the datasource, then out it in
    //see Repository.getData()
    suspend fun getData(): Sites? {
        Log.d(TAG, "loading sitesdata from repository")
        if (!mocked) {
            if (dirty) {
                cache = dataSource.loadSomeData()
                dirty = false
            }
        }
        Log.d(TAG, "loading sitesdata from repository - DONE")
        return cache
    }
}