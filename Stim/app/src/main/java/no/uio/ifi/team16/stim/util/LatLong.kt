package no.uio.ifi.team16.stim.util

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

/**
 * A latitude/longitude-pair
 */
data class LatLong(val lat: Double, val lng: Double) {

    companion object {
        const val earthRadiusKm: Double = 6372.8
        const val equalsDelta = 0.001

        fun fromGoogle(latLng: LatLng): LatLong {
            return LatLong(latLng.latitude, latLng.longitude)
        }
    }

    /**
     * SOURCE: https://gist.github.com/jferrao/cb44d09da234698a7feee68ca895f491
     *
     * Haversine formula. Giving great-circle distances between two points on a sphere from their longitudes and latitudes.
     * It is a special case of a more general formula in spherical trigonometry, the law of haversines, relating the
     * sides and angles of spherical "triangles".
     *
     * https://rosettacode.org/wiki/Haversine_formula#Java
     *
     * @return Distance in kilometers
     */
    fun haversine(destination: LatLong): Double {
        val dLat = Math.toRadians(destination.lat - this.lat)
        val dLon = Math.toRadians(destination.lng - this.lng)
        val originLat = Math.toRadians(this.lat)
        val destinationLat = Math.toRadians(destination.lat)

        val a = sin(dLat / 2).pow(2.toDouble()) + sin(dLon / 2).pow(2.toDouble()) * cos(originLat) * cos(destinationLat)
        val c = 2 * asin(sqrt(a))
        return earthRadiusKm * c
    }

    fun toGoogle(): LatLng {
        return LatLng(lat, lng)
    }

    override fun toString(): String {
        return "Latitude: $lat, longitude: $lng"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LatLong

        return abs(lat - other.lat) < equalsDelta && abs(lng - other.lng) < equalsDelta
    }

    override fun hashCode(): Int {
        var result = lat.hashCode()
        result = 31 * result + lng.hashCode()
        return result
    }
}
