package no.uio.ifi.team16.stim.data.repository

import android.util.Log
import no.uio.ifi.team16.stim.data.InfectiousPressure
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
            //eta
            ArrayFloat.factory(
                floatArrayOf(
                    0f, 400000f
                )
            ).reshape(intArrayOf(2)) as ArrayFloat,
            //xi
            ArrayFloat.factory(
                floatArrayOf(
                    0f, 1080000f
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
            Date(2022, 1, 4)
        )
    )
    //extend arrayFloat with a getter since thiers is very impractical
    /*private fun ArrayFloat.get(row: Int, column: Int): Float =
        this.getFloat(idx.set(0, row, column))*/

    @Test
    fun testProjection() {
        val infectiousPressure: InfectiousPressure =
            repository.getSomeData()!! //returns the above mock data from cache
        /*Log.d(TAG, infectiousPressure?.getLatitude(4,4).toString())
        val lat = infectiousPressure!!.getLatitude(4,4)
        val lng = infectiousPressure!!.getLongitude(4,4)
        Log.d(TAG, infectiousPressure?.getEtaRho(4,4).toString())
        Log.d(TAG, infectiousPressure?.getXiRho(4,4).toString())
        Log.d(TAG, project(lat, lng).toString())*/
        Log.d("", infectiousPressure.projection.toString())
        fun checkAtIndex(i: Int, j: Int) {
            var lat = infectiousPressure.getLatitude(i, j)
            var lng = infectiousPressure.getLongitude(i, j)
            var etaxi = infectiousPressure.project(lat, lng)
            /*assertEquals(
                Pair(infectiousPressure.getEtaRho(i), infectiousPressure.getXiRho(j)),
                etaxi
            )*/

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

    /*fun project(lat: Float, lng: Float) : Pair<Float, Float> {
        val xi = 2*atan(tan(
            (1/4*PI+1/2*lat)*
                    ((1-exp(1f)*sin(lat))/(1+exp(1f)*sin(lat))).pow(exp(1f)/2)
            )) -PI/2
        val Re = 6378000f
        val R  = Re*cos(lat)/(1-exp(2f)*sin(lat)*sin(lat))*cos(xi)
        val k  = 2*R/(1+lat0*sin(lat)+cos(lat0)*cos(lat)*cos(lng-lng0))

        val x = k*cos(lat)*sin(lng-lng0)
        val y = k*(cos(lat0)*sin(lat)-sin(lat0)*cos(lat)*cos(lng-lng0))
        return Pair(x.toFloat(),y.toFloat())
    }*/
}