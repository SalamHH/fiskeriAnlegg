package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.coroutines.awaitString
import no.uio.ifi.team16.stim.data.BarentsWatchAtSite
import no.uio.ifi.team16.stim.data.BarentsWatchToken
import no.uio.ifi.team16.stim.data.Site
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.time.Instant

class BarentsWatchDataLoader {
    private val TAG = "BarentsWatchDataLoader"

    /**
     * Retrieves an authentication token to be used in API requests to the BW API
     * TODO: should use suspending post, this one <seems> to block
     */
    suspend fun getToken(): BarentsWatchToken? {

        val bodyParams = hashMapOf(
            "grant_type" to "client_credentials",
            "client_secret" to "YXA8wDV&SmUqdo",
            "scope" to "api",
            "client_id" to "andreaav@uio.no:andreaav@uio.no"
        )

        val body = getDataString(bodyParams)

        val (request, response, result) = Fuel.post("https://id.barentswatch.no/connect/token")
            .header(Headers.CONTENT_TYPE, "application/x-www-form-urlencoded")
                //.header(Headers.CONTENT_LENGTH, "344")
                //.header("Host", "www.barentswatch.no")
                .header(Headers.USER_AGENT, "PostmanRuntime/7.29.0")
            .header(Headers.ACCEPT, "*/*")
            .header(Headers.ACCEPT_ENCODING, "gzip, deflate, br")
            .header("Connection", "keep-alive")
            .body(body)
            .responseString()

        Log.d(TAG, request.toString())
        Log.d(TAG, response.toString())
        Log.d(TAG, result.toString())

        //TODO: return null if failure

        val tokenStr = result.component1()
        Log.d(TAG, tokenStr.toString())
        val jsonObject = JSONObject(tokenStr)
        val token = jsonObject.getString("access_token")
        Log.d(TAG, token)

        val validityTime = jsonObject.getString("expires_in").toLong()

        return BarentsWatchToken(token, Instant.now().plusSeconds(validityTime))
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getDataString(params: HashMap<String, String>): String {
        val result = StringBuilder()
        var first = true
        for ((key, value) in params) {
            if (first) first = false else result.append("&")
            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(value, "UTF-8"))
        }
        return result.toString()
    }

    public suspend fun loadData(site: Site, token: BarentsWatchToken): BarentsWatchAtSite? {

        val listPD = getPdList(site, token)
        val listILA = getIlaList(site, token)

        //forhåpentligvis kan attributter i site brukes for å gjøre requesten.
        //TODO null if failure
        return BarentsWatchAtSite(listPD, listILA)
    }

    private suspend fun getIlaList(site: Site, token: BarentsWatchToken): java.util.HashMap<String, String> {
        val listILA = HashMap<String, String>()
        val baseUrlILA = "https://www.barentswatch.no/bwapi/v1/geodata/download/localitieswithila"
        val lon = site.latLong.lng
        val lat = site.latLong.lat
        val distance = 1.0

        val responseStr = Fuel.get(baseUrlILA, listOf("format" to "JSON", "lon" to lon, "lat" to lat, "distance" to distance))
            .authentication()
            .bearer(token.key)
            .awaitString()

        val response = JSONObject(responseStr)
        val totalFeatures = response.getString("totalFeatures")
        if (totalFeatures == "0") {
            return HashMap<String, String>()
        }

        val features = response.getJSONArray("features")
        val jsonObject = features.getJSONObject(0)
        val properties = jsonObject.getJSONObject("properties")
        listILA["mistankedato"] = properties.getString("mistankedato")
        listILA["paavistdato"] = properties.getString("paavistdato")

        return listILA
    }

    private suspend fun getPdList(site: Site, token: BarentsWatchToken): HashMap<String, String> {
        val listPD = HashMap<String, String>()
        val baseUrlPD = "https://www.barentswatch.no/bwapi/v1/geodata/download/localitieswithpd"
        val lon = site.latLong.lng
        val lat = site.latLong.lat
        val distance = 1.0

        val responseStr = Fuel.get(baseUrlPD, listOf("format" to "JSON", "lon" to lon, "lat" to lat, "distance" to distance))
            .authentication()
            .bearer(token.key)
            .awaitString()

        val response = JSONObject(responseStr)
        val totalFeatures = response.getString("totalFeatures")
        if (totalFeatures == "0") {
            return HashMap<String, String>()
        }

        val features = response.getJSONArray("features")
        val jsonObject = features.getJSONObject(0)
        val properties = jsonObject.getJSONObject("properties")
        listPD["pd_ukjent_mistankedato"] = properties.getString("pd_ukjent_mistankedato")
        listPD["pd_sav2_mistankedato"] = properties.getString("pd_sav2_mistankedato")
        listPD["pd_sav3_mistankedato"] = properties.getString("pd_sav3_mistankedato")
        listPD["pd_ukjent_paavistdato"] = properties.getString("pd_ukjent_paavistdato")
        listPD["pd_sav2_paavistdato"] = properties.getString("pd_sav2_paavistdato")
        listPD["pd_sav3_paavistdato"] = properties.getString("pd_sav3_paavistdato")

        return listPD
    }


}