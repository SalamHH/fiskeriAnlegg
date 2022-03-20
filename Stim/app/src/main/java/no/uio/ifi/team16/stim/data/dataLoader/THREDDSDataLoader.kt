package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import ucar.ma2.InvalidRangeException
import ucar.nc2.dataset.NetcdfDataset
//import ucar.nc2.dataset.NetcdfDatasets
import java.io.IOException
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * ABSTRACT CLASS FOR THREDDS DATALOADERS
 * should be inherited by InfectiousPressureDataLoader and NorKyst800DataLoader
 *
 * Handles common methods between the infectiouspressure and norkyst800 thredds datasources.
 *
 * Mostly used to convert coordinates to ranges, and to check if data is up to date
 */
abstract class THREDDSDataLoader {
    private val TAG = "THREDDSDataLoader"
    protected abstract val url: String //the url the data is read from, must be overriden in subclass.
    private val maxX = 901
    private val maxY = 2601
    protected val separation = 800  //separation(in meters) between points(in real life)

    //ranges of the NorKyst800(and smittepress) THREDDS dataset
    private val minLongitude = 0
    private val maxLongitude = 90
    private val longitudeDiff = maxLongitude - minLongitude
    private val minLatitude = 0
    private val maxLatitude = 90
    private val latitudeDiff = maxLatitude - minLatitude

    /**
     * convert a set of geographical coordinates to a range applicable to our THREDDS datasets
     *
     * @param latitudeFrom smallest latitude to get data from
     * @param latitudeTo largest latitude to get data from
     * @param latitudeResolution resolution of latitude. A latitude resolution of 0.001 means that
     * the data is sampled from latitudeFrom to latitudeTo with 0.001 latitude between points
     * @param longitudeFrom smallest longitude to get data from
     * @param longitudeTo largest longitude to get data from
     * @param latitudeResolution resolution of longitude.
     * @return pair of ranges in x- and y-direction
     */
    fun geographicCoordinateToRange(
        latitudeFrom: Float, latitudeTo: Float, latitudeResolution: Float,
        longitudeFrom: Float, longitudeTo: Float, longitudeResolution: Float
    ): Pair<String, String> {
        //interpret as ranges
        //TODO: if data points are INSIDE the grid, rounding is appropriate, but if it is in a corner we have to use floor or ceil.
        val startX = max(round(min(longitudeFrom - minLongitude, 0f) / longitudeDiff).toInt(), 0)
        val stopX = max(round(min(longitudeTo / maxLongitude, 1f) * maxX).toInt(), 0)
        val stepX = 500 //kotlin.math.max(latitudeResolution,1)
        val startY = max(round(min(latitudeFrom - minLatitude, 0f) / latitudeDiff).toInt(), 0)
        val stopY = max(round(min(latitudeTo / maxLatitude, 1f) * maxY).toInt(), 0)
        val stepY = 1350 //kotlin.math.max(latitudeResolution,1)
        Log.d(
            TAG,
            "loading from ranges ${startX}:${stopX}:${stepX}\",\"${startY}:${stopY}:${stepY}"
        )
        return Pair("${startX}:${stopX}:${stepX}", "${startY}:${stopY}:${stepY}")
    }

    /**
     * parses a string with format YYYY-MM-DD to a date object
     */
    fun parseDate(dateString: String): Date {
        val yyyy = dateString.substring(0..3)
        val mm = dateString.substring(5..6)
        val dd = dateString.substring(8..9)
        return Date(yyyy.toInt(), mm.toInt(), dd.toInt())
    }

    /**
     * check if the dataset is up to date with given date.
     * If true, daaset is up to date, if false it is not. If null the checking failed.
     *
     * Should work, not tested
     * @param currentDate date to check if dataset agrees with(date has minimum esolution on a day)
     * @return whether the data is up to date or not. Or null if request failed.
     */
    fun isUpToDate(currentDate: Date): Boolean? =
        NetcdfDataset.openDataset(url).let { ncfile ->
            Log.d(TAG, "checking if data is up to date")
            try {
                parseDate(ncfile.findGlobalAttribute("fromdate")!!.stringValue!!) == currentDate
            } catch (e: IOException) {
                Log.e("ERROR", e.toString())
                null
            } catch (e: InvalidRangeException) {
                Log.e("ERROR", e.toString())
                null
            } catch (e: NullPointerException) {
                Log.e("ERROR", e.toString())
                null
            } finally {
                Log.d(TAG, " load - DONE")
                ncfile.close()
            }
        }
}
