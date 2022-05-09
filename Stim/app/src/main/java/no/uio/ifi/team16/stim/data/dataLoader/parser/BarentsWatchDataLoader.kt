package no.uio.ifi.team16.stim.data.dataLoader.parser

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class BarentsWatchDataLoader {
    private val TAG = "BarentsWatchDataLoader"

    /**
     * Retrieves an authentication token to be used in API requests to the BW API
     */
    suspend fun getToken(): String {

        val bodyParams = hashMapOf("grant_type" to  "client_credentials",
            "client_secret" to "YXA8wDV&SmUqdo",
            "scope" to "api",
            "client_id" to "andreaav@uio.no:andreaav@uio.no")

        val body = getDataString(bodyParams)

        val (request, response, result) = Fuel.post("https://id.barentswatch.no/connect/token")
                .header(Headers.CONTENT_TYPE, "application/x-www-form-urlencoded")
                //.header(Headers.CONTENT_LENGTH, "344")
                //.header("Host", "www.barentswatch.no")
                .header(Headers.USER_AGENT, "PostmanRuntime/7.29.0")
                .header(Headers.ACCEPT, "*/*")
                .header(Headers.ACCEPT_ENCODING,"gzip, deflate, br")
                .header("Connection","keep-alive")
                .body(body)
                .responseString()

            Log.d(TAG, request.toString())
            Log.d(TAG, response.toString())
            Log.d(TAG, result.toString())

        val tokenStr = result.component1()
        Log.d(TAG, tokenStr.toString())
        val jsonObject = JSONObject(tokenStr)
        val token = jsonObject.getString("access_token")
        Log.d(TAG, token)
        return token
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

}