package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.dataLoader.AddressDataLoader
import no.uio.ifi.team16.stim.util.LatLng

class AddressRepository {

    val dataSource = AddressDataLoader()
    private val cache: MutableMap<LatLng, String> = mutableMapOf()

    suspend fun getMunicipalityNr(latLng: LatLng): String? {
        return cache.getOrPut(latLng) {
            return dataSource.getMunicipalityNr(latLng)
        }
    }
}