package no.uio.ifi.team16.stim.data

import android.util.Log
import no.uio.ifi.team16.stim.util.LatLong
import ucar.ma2.*

/**
 * data from the NorKyst800 model. Mostly we will use stream data.
 */
data class NorKyst800(
    val depth: ArrayDouble,
    val longitude: ArrayDouble,
    val latitude: ArrayDouble,
    val salinity: ArrayInt,
    val temperature: ArrayInt,
    val time: ArrayDouble,
    val u: ArrayInt,
    val v: ArrayInt,
    val w: ArrayInt
) {
    val TAG = "NORKYTS800"
    var latLonShape = Pair(latitude.shape[0], latitude.shape[1])
    val tdll = u.shape //tdll = time, depth, latitude, longitude
    var idxt = Index1D(time.shape) //index for time
    var idxd = Index1D(depth.shape) //index for depth
    var idll = Index2D(latitude.shape) //index for latitude-longitude
    var idx4: Index4D = Index4D(tdll) //index for all


    //////////////////////
    // GETTER FUNCTIONS //
    //////////////////////
    /**
     * Get salinity closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     */
    fun getSalinity(latLong: LatLong, time: Int, depth: Int): Double {
        /*find the concentrationgrid closest to our latlongpoint,
        we use euclidean distance, or technically L1, to measure distance between latlngs.*/
        Log.d(TAG, "finding closest point to " + latLong.toString())
        val index = getClosestIndex(latLong)
        return salinity.get(time, depth, index.first, index.second) //TODO: WRONG! NOT SCALED
    }

    //wrapper, get at "smallest" time and at surface
    fun getSalinity(latLong: LatLong) = getSalinity(latLong, 0, 0)

    /**
     * Get temperature closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     */
    fun getTemperature(latLong: LatLong, time: Int, depth: Int): Double {
        /*find the concentrationgrid closest to our latlongpoint,
        we use euclidean distance, or technically L1, to measure distance between latlngs.*/
        val index = getClosestIndex(latLong)
        return temperature.get(time, depth, index.first, index.second) //TODO: WRONG! NOT SCALED
    }

    //wrapper, get at "smallest" time and at surface
    fun getTemperature(latLong: LatLong) = getTemperature(latLong, 0, 0)

    /**
     * Get velocity in all three directions(xyz) closest to given coordinates at given time and depth.
     * See time and depth explanation in class definition
     */
    fun getVelocity(latLong: LatLong, time: Int, depth: Int): Triple<Double, Double, Double> {
        /*find the concentrationgrid closest to our latlongpoint,
        we use euclidean distance, or technically L1, to measure distance between latlngs.*/
        val index = getClosestIndex(latLong)
        return Triple(
            u.get(time, depth, index.first, index.second),
            v.get(time, depth, index.first, index.second),
            w.get(time, depth, index.first, index.second)
        )
    }

    //wrapper, get at "smallest" time and at surface
    fun getVelocity(latLong: LatLong) = getVelocity(latLong, 0, 0)

    //////////////////////
    // HELPER FUNCTIONS //
    //////////////////////
    //extend arrayFloat with a getter since thiers is very impractical
    private fun ArrayDouble.get(row: Int, column: Int): Double =
        this.getDouble(idll.set(row, column))

    private fun ArrayDouble.get(time: Int, depth: Int, row: Int, column: Int): Double =
        this.getDouble(idx4.set(time, depth, row, column))

    private fun ArrayInt.get(time: Int, depth: Int, row: Int, column: Int): Double =
        this.getDouble(idx4.set(time, depth, row, column))

    private fun getClosestIndex(latLong: LatLong): Pair<Int, Int> {
        var row = 0
        var column = 0
        var minDistance = 1000000.0
        var distance: Double
        //find row from latitude
        for (i in 0 until latLonShape.first) {
            for (j in 0 until latLonShape.second) {
                distance = latLong.haversine(
                    LatLong(
                        latitude.get(i, j).toDouble(),
                        longitude.get(i, j).toDouble()
                    )
                )
                Log.d(
                    TAG,
                    "d(" + latLong.toString() + ",LatLng(${latitude.get(i, j)}, ${
                        longitude.get(
                            i,
                            j
                        )
                    }) = $distance"
                )
                if (distance < minDistance) {
                    row = i
                    column = j
                    minDistance = distance
                }
            }
        }
        return Pair(row, column)
    }
}