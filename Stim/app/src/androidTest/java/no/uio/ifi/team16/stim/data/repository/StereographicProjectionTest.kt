package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.InfectiousPressure
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


    /**
     * These tests are not valid after commit 886a6e71d1bcb3d1de21ff5af674a0f4f61de112,
     * since from then on we rely solely on projection.
     */
    /*
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
    */

}