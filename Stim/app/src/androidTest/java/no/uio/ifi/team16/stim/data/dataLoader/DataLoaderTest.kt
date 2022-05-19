package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import kotlinx.coroutines.runBlocking
import no.uio.ifi.team16.stim.util.*
import org.junit.Assert.*
import org.junit.Test



class DataLoaderTest {
    private val sitesDataLoader = SitesDataLoader()
    private val addressDataSource = AddressDataLoader()

    @Test
    fun testAbleToLoadSites(){
        val data = runBlocking {
            sitesDataLoader.loadSitesByName("Klubben")
        }
        assertNotNull(data)
    }

    @Test
    fun testAbleToLoadAddress(){
        val data = runBlocking {
            val testPosition = LatLong(60.0, 10.0)
            addressDataSource.loadMunicipalityNr(testPosition)
        }
        assertNotNull(data)
    }





}