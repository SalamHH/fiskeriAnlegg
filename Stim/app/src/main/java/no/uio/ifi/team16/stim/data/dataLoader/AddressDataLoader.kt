package no.uio.ifi.team16.stim.data.dataLoader

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import no.uio.ifi.team16.stim.util.LatLng
import org.json.JSONObject

class AddressDataLoader {

    private val BASE_URL = "https://ws.geonorge.no/adresser/v1/punktsok"
    private val RADIUS = 1000

    suspend fun getMunicipalityNr(latLng: LatLng): String? {

        val side = "side" to 0
        val radius = "radius" to RADIUS
        val utkoordsys = "utkoordsys" to 4258
        val koordsys = "koordsys" to 4258
        val lat = "lat" to latLng.lat
        val lon = "lon" to latLng.lng

        val params = listOf<Pair<String, Any>>(side, radius, utkoordsys, koordsys, lat, lon)
        val result = Fuel.get(BASE_URL, params).awaitString()

        if (result.isBlank()) {
            return null
        }

        val response = JSONObject(result)
        val addresses = response.getJSONArray("adresser")

        if (addresses.length() > 0) {
            val first = addresses.getJSONObject(0)
            return first.getString("kommunenummer")
        }

        return null
    }
}