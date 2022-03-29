package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.dataLoader.AddressDataLoader
import no.uio.ifi.team16.stim.util.LatLong

class AddressRepository {

    val dataSource = AddressDataLoader()
    private val cache: MutableMap<LatLong, String> = mutableMapOf()

    suspend fun getMunicipalityNr(latLong: LatLong): String? {
        return cache.getOrPut(latLong) {
            return dataSource.getMunicipalityNr(latLong)
        }
    }
}