package no.uio.ifi.team16.stim.data.dataLoader

//import ucar.nc2.dataset.NetcdfDatasets
import android.util.Log
import no.uio.ifi.team16.stim.util.LatLong
import no.uio.ifi.team16.stim.util.project
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import ucar.ma2.InvalidRangeException
import ucar.nc2.Variable
import ucar.nc2.dataset.NetcdfDataset
import java.io.IOException
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

    /////////////
    // LOADERS //
    /////////////
    /**
     * general method for opening THREDDS file.
     * @param url: url of data to open
     * @param action: action to perform on the opened file, must return a representation of the data
     * @return the result of the action on the netcdf file, f.ex. an infectiousPressure-object
     */
    inline fun <D> THREDDSLoad(url: String, action: (NetcdfDataset) -> D?): D? {
        val TAG = "THREDDSLOADER"
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

    /**
     * from a sequence take every (stride) element
     */
    protected fun <T> Sequence<T>.takeEvery(stride: Int): Sequence<T> =
        this.filterIndexed { i, _ -> (i % stride == 0) }

    /**
     * for the given sequence, take values in the given intProgression(range with stride)
     */
    protected fun <T> Sequence<T>.takeRange(range: IntProgression): Sequence<T> =
        drop(range.first).take(range.last - range.first).takeEvery(range.step)

    /**
     * from a sequence take every (stride) eleemnt
     */
    protected fun <T> List<T>.takeEvery(stride: Int): List<T> =
        this.filterIndexed { i, _ -> (i % stride == 0) }

    /**
     * for the given sequence, take values in the given intProgression(range with stride)
     */
    protected fun <T> List<T>.takeRange(range: IntProgression): List<T> =
        drop(range.first).take(range.last - range.first).takeEvery(range.step)

    ////////////////////////
    // UTILITIES - NETCDF //
    ////////////////////////
    /**
     * for a netcdfdataset with a gridmappingvariable, read out the projection
     */
    protected fun readAndMakeProjectionFromGridMapping(gridMapping: Variable): CoordinateTransform {
        val crsFactory = CRSFactory()
        val stereoCRT = crsFactory.createFromParameters(
            null,
            gridMapping.findAttribute("proj4string")?.stringValue
                ?: throw NullPointerException("Failed to read attribute <proj4string> from <gridMapping> from infectiousPressure")
        )
        val latLngCRT = stereoCRT.createGeographic()
        val ctFactory = CoordinateTransformFactory()
        return ctFactory.createTransform(latLngCRT, stereoCRT)
    }

    /////////////////////////////
    // UTILITIES - COORDINATES //
    /////////////////////////////
    /**
     * convert a set of geographical coordinates to a range applicable to our THREDDS datasets
     *
     * @param latLongUpperLeft latlong of upper left corner in a box
     * @param latLongLowerRight latlong of lower right corner in a box
     * @param xStride stride between x coordinates
     * @param yStride stride between y coordinates
     * @param projection projection used in the dataset.
     * @return pair of ranges in x- and y-direction
     */
    fun geographicCoordinateToRange(
        latLongUpperLeft: LatLong,
        latLongLowerRight: LatLong,
        xStride: Int,
        yStride: Int,
        projection: CoordinateTransform
    ): Pair<IntProgression, IntProgression> {
        //map from and to to indexes in grid
        val (yFrom, xFrom) = projection.project(latLongUpperLeft)
        val (yTo, xTo) = projection.project(latLongLowerRight)
        //interpret as ranges
        val startX = max(min(round(xFrom).toInt(), maxX), 0) //ensure >0, <maxX
        val stopX = max(min(round(xTo).toInt(), maxX), startX) //ensure >startX <maxX
        val startY = max(min(round(yFrom).toInt(), maxY), 0) //ensure >0, <maxX
        val stopY = max(min(round(yTo).toInt(), maxY), startY) //ensure >startX <maxX
        return Pair(
            fromClosedRange(startX, stopX, xStride),
            fromClosedRange(startY, stopY, yStride)
        )
    }
}
