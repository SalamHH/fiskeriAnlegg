package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.dataLoader.AddressDataLoader
import no.uio.ifi.team16.stim.util.LatLong

class AddressRepository {

    private val dataSource = AddressDataLoader()

    /**
     * Key = latlng, value = municipality nr
     */
    private val municipalityNrCache: MutableMap<LatLong, String?> = mutableMapOf()

    /**
     * Get municipality number for a given coordinate
     */
    suspend fun getMunicipalityNr(latLong: LatLong): String? {
        var nr = municipalityNrCache[latLong]
        if (nr != null) {
            return nr
        }

        nr = dataSource.loadMunicipalityNr(latLong)
        municipalityNrCache[latLong] = nr
        return nr
    }
}