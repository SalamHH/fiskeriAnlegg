package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.dataLoader.parser.BarentsWatchDataLoader

class BarentsWatchRepository {

    private val TAG = "BarentsWatchRepository"
    private val dataSource = BarentsWatchDataLoader()

    /**
     * Load the data from the datasource
     */
    suspend fun getToken(): String? {
        var response = ""

        response = dataSource.getToken()

        return response
    }
}
