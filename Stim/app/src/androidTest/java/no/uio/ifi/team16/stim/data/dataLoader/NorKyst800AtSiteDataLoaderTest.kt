package no.uio.ifi.team16.stim.data.dataLoader

import kotlinx.coroutines.runBlocking
import no.uio.ifi.team16.stim.util.Options
import org.junit.Assert.assertNotNull
import org.junit.Test

class NorKyst800AtSiteDataLoaderTest {
    val TAG = "NorKyst800AtSiteDataLoaderTest"
    val dataLoader = NorKyst800AtSiteDataLoader()

    /**
     * Test wether the dataloader is able to load a non-null object
     */
    @Test
    fun testAbleToLoad() {
        val data = runBlocking {
            dataLoader.load(
                Options.fakeSite
            )
        }
        assertNotNull(data)
    }
}