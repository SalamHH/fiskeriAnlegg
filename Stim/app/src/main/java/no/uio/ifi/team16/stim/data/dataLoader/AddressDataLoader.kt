package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import no.uio.ifi.team16.stim.util.LatLong
import org.json.JSONObject

class AddressDataLoader {

    private val TAG = "AddressDataLoader"
    private val BASE_URL = "https://ws.geonorge.no/adresser/v1/punktsok"
    private val RADIUS = 1000

    suspend fun loadMunicipalityNr(latLong: LatLong): String? {

        val side = "side" to 0
        val radius = "radius" to RADIUS
        val utkoordsys = "utkoordsys" to 4258
        val koordsys = "koordsys" to 4258
        val lat = "lat" to latLong.lat
        val lon = "lon" to latLong.lng

        val params = listOf<Pair<String, Any>>(side, radius, utkoordsys, koordsys, lat, lon)

        var result = ""
        try {
            result = Fuel.get(BASE_URL, params).awaitString()
        } catch (e: Exception) {
            Log.e(TAG, "Kunne ikke hente kommune for latlng: $latLong", e)
        }

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