package no.uio.ifi.team16.stim.data.repository

import android.util.Log
import no.uio.ifi.team16.stim.data.InfectiousPressure
import no.uio.ifi.team16.stim.util.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransformFactory
import ucar.ma2.ArrayFloat
import java.util.*

class stereographicProjectionTest {
    val TAG = "stereographicProjectionTest"

    //a mock infectiousPressureRepository, does not load form the internett.
    //a single time, two depths and a two times two grid
    private val repository = InfectiousPressureRepository(
        InfectiousPressure(
            //concentration
            ArrayFloat.factory(
                floatArrayOf(
                    0.1f, 0.2f,
                    0.3f, 0.4f,
                )
            ).reshape(intArrayOf(1, 2, 2)) as ArrayFloat,
            //xi, projection x
            ArrayFloat.factory(
                floatArrayOf(
                    0f, 1080000f
                )
            ).reshape(intArrayOf(2)) as ArrayFloat,
            //eta, projection y
            ArrayFloat.factory(
                floatArrayOf(
                    0f, 400000f
                )
            ).reshape(intArrayOf(2)) as ArrayFloat,
            //latitude
            ArrayFloat.factory(
                floatArrayOf(
                    55.90837f, 63.908974f,
                    57.47694f, 66.10339f
                )
            ).reshape(intArrayOf(2, 2)) as ArrayFloat,
            //longitude
            ArrayFloat.factory(
                floatArrayOf(
                    9.194591f, 20.176983f,
                    3.4355416f, 13.227f
                )
            ).reshape(intArrayOf(2, 2)) as ArrayFloat,
            //time
            987654321f,
            CRSFactory().createFromParameters(
                null,
                "+proj=stere +ellps=WGS84 +lat_0=90.0 +lat_ts=60.0 +x_0=3192800 +y_0=1784000 +lon_0=70"
            ).let { stereoCRT ->
                CoordinateTransformFactory().createTransform(
                    stereoCRT.createGeographic(), //from latlong
                    stereoCRT                    //to stereo
                )
            },
            Date(1900, 10, 1),
            Date(2022, 1, 4),
            1080000f,
            400000f
        )
    )

    @Test
    fun testProjection() {
        val infectiousPressure: InfectiousPressure =
            repository.getSomeData()!! //returns the above mock data from cache, guaranteed
        Log.d("", infectiousPressure.projection.toString())
        fun checkAtIndex(i: Int, j: Int) {
            var lat = infectiousPressure.getLatitude(i, j)
            var lng = infectiousPressure.getLongitude(i, j)
            var etaxi = infectiousPressure.project(lat, lng)

            assertEquals(
                infectiousPressure.getEtaRho(i),
                etaxi.first,
                1f
            )
            assertEquals(
                infectiousPressure.getXiRho(j),
                etaxi.second,
                1f
            )
        }

        checkAtIndex(0, 0)
        checkAtIndex(0, 1)
        checkAtIndex(1, 0)
        checkAtIndex(1, 1)
    }

    /**
     * test if the O(1) algorithm using projection agrees wth the O(n^2) one using brute-force
     */
    @Test
    fun testProjectionAgreesWithBruteForce() {
        val infectiousPressure: InfectiousPressure =
            repository.getSomeData()!! //returns the above mock data from cache

        fun checkAtIndex(i: Int, j: Int) {
            var lat = infectiousPressure.getLatitude(i, j).toDouble()
            var lng = infectiousPressure.getLongitude(i, j).toDouble()
            //brute-force the indexes: O(n^2)
            val etaxiBrute = infectiousPressure.getClosestIndex(LatLng(lat, lng))
            //use projection: O(1)
            val etaxiProjection = infectiousPressure.getClosestIndexWithProjection(LatLng(lat, lng))
            assertEquals(
                etaxiProjection,
                etaxiBrute,
            )
        }

        checkAtIndex(0, 0)
        checkAtIndex(0, 1)
        checkAtIndex(1, 0)
        checkAtIndex(1, 1)
    }
}