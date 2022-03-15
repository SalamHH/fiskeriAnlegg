package no.uio.ifi.team16.stim.data.repository

import kotlinx.coroutines.runBlocking
import no.uio.ifi.team16.stim.util.LatLng
import org.junit.Assert.assertNotNull
import org.junit.Test

class WeatherRepositoryTest {

    private val repository = WeatherRepository()

    @Test
    fun getData() {
        runBlocking {
            val position = LatLng(60.0, 10.0)
            val weather = repository.getData(position)
            assertNotNull(weather)
        }
    }
}