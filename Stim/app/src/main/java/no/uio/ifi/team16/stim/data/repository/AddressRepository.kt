package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.dataLoader.AddressDataLoader
import no.uio.ifi.team16.stim.util.LatLong

class AddressRepository {

    private val dataSource = AddressDataLoader()
    private val cache: MutableMap<LatLong, String?> = mutableMapOf()

    suspend fun getMunicipalityNr(latLong: LatLong): String? {
        var nr = cache[latLong]
        if (nr != null) {
            return nr
        }

        nr = dataSource.getMunicipalityNr(latLong)
        cache[latLong] = nr
        return nr
    }
}