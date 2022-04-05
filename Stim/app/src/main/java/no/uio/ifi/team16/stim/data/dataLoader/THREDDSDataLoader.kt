package no.uio.ifi.team16.stim.data.dataLoader

//import ucar.nc2.dataset.NetcdfDatasets
import android.util.Log
import no.uio.ifi.team16.stim.util.FloatArray2D
import no.uio.ifi.team16.stim.util.project
import org.locationtech.proj4j.CoordinateTransform
import ucar.ma2.ArrayFloat
import ucar.ma2.InvalidRangeException
import ucar.nc2.dataset.NetcdfDataset
import java.io.IOException
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.ranges.IntProgression.Companion.fromClosedRange

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
        latitudeFrom: Float, latitudeTo: Float, yStride: Int,
        longitudeFrom: Float, longitudeTo: Float, xStride: Int,
        projection: CoordinateTransform
    ): Pair<String, String> {
        //map from and to to indexes in grid
        val (yFrom, xFrom) = projection.project(latitudeFrom, longitudeFrom)
        val (yTo, xTo) = projection.project(latitudeTo, longitudeTo)
        //interpret as ranges
        val startX = max(min(round(xFrom).toInt(), maxX), 0) //ensure >0, <maxX
        val stopX = max(min(round(xTo).toInt(), maxX), startX) //ensure >startX <maxX
        val stepX = xStride
        val startY = max(min(round(yFrom).toInt(), maxY), 0) //ensure >0, <maxX
        val stopY = max(min(round(yTo).toInt(), maxY), startY) //ensure >startX <maxX
        val stepY = yStride
        Log.d(
            TAG,
            "loading from ranges ${startX}:${stopX}:${stepX}\",\"${startY}:${stopY}:${stepY}"
        )
        return Pair(fromClosedRange(startX, stopX, stepX), fromClosedRange(startY, stopY, stepY))
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
    fun isUpToDate(currentDate: Date, url: String): Boolean? = THREDDSLoad(url) { ncfile ->
        ncfile.findGlobalAttribute("fromdate")?.run {
            parseDate(this.stringValue)
        } == currentDate
    }

    /**
     * general method for opening THREDDS file.
     * @param url: url of data to open
     * @param action: action to perform on the opened file, must return a representation of the data
     * @return the result of the action on the netcdf file, f.ex. an infectiousPressure-object
     */
    fun <D> THREDDSLoad(url: String, action: (NetcdfDataset) -> D?): D? {
        var ncfile: NetcdfDataset? = null
        var data: D? = null
        try {
            Log.d(TAG, "OPENING $url")
            ncfile = NetcdfDataset.openDataset(url)
            //Log.d(TAG, "OPENDAP URL OPENED")
            data = action(ncfile)
            Log.d(TAG, "DONE!")
        } catch (e: IOException) {
            Log.e("ERROR", e.toString())
        } catch (e: InvalidRangeException) {
            Log.e("ERROR", e.toString())
        } catch (e: NullPointerException) {
            Log.e(
                TAG,
                "ERROR: a Variable might be read as null, are you sure you are using the correct url/dataset?$e"
            )
            Log.e("ERROR", e.toString())
        } catch (e: Exception) {
            Log.e(TAG, "didnt catch this one! $e")
        } finally {
            ncfile?.close()
        }
        return data
    }

    fun ArrayFloat.to2DFloatArray(): FloatArray2D {
        return this.copyToNDJavaArray() as FloatArray2D
    }

    fun reformatIntProgression(p: IntProgression): String {
        return "${p.first}:${p.last}:${p.step}"
    }
}
