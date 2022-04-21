package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitByteArrayResult
import com.github.kittinunf.fuel.coroutines.awaitString
import com.github.kittinunf.result.getOrElse
import com.github.kittinunf.result.onError
import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.util.*
import ucar.ma2.ArrayDouble
import ucar.ma2.ArrayFloat
import ucar.nc2.NetcdfFile.openInMemory
import ucar.nc2.Variable
import ucar.nc2.dataset.NetcdfDataset
import kotlin.math.max

/**
 * DataLoader for data related tot he norkyst800 model.
 * Temperature, salinity, water velocity etc
 **/
open class NorKyst800DataLoader : THREDDSDataLoader() {
    private val TAG = "NorKyst800DataLoader"

    //the catalog url is assumed to be time-invariant. Here all entries are listed.
    private val catalogUrl =
        "https://thredds.met.no/thredds/catalog/fou-hi/norkyst800m-1h/catalog.html"

    /////////////
    // LOADERS //
    /////////////
    /**
     * Get NorKyst800 data in the given range
     *
     * @param xRange range of x-values to get from
     * @param yRange range of y-values to get from
     * @param depthRange depth as a range with format from:stride:to
     * @param timeRange time as a range with format from:stride:to
     * @return Norkyst800 data in the given range
     */
    suspend fun load(
        xRange: IntProgression,
        yRange: IntProgression,
        depthRange: IntProgression,
        timeRange: IntProgression
    ): NorKyst800? {
        /*
        * Due to the abscence of certain critical java libraries for doing requests over https with
        * netcdf java, we have to fetch the data "ourselves", with a fuel request. We then open the file in memory.
        * The downside is that we cannot stream the data, but the data can be retrieved pre-sliced
        */
        val baseUrl = loadForecastUrl() ?: run {
            Log.e(
                TAG,
                "Failed to load the forecast URL from the catalog, is the catalog URL correct?"
            )
            return@load null
        }

        val parametrizedUrl = makeParametrizedUrl(baseUrl, xRange, yRange, depthRange, timeRange)
        Log.d(TAG, parametrizedUrl)

        val responseBinary = Fuel.get(parametrizedUrl).awaitByteArrayResult().onError { error ->
            Log.e(TAG, "Failed to load norkyst800data due to:\n $error")
            return@load null
        }.getOrElse {
            Log.e(
                TAG,
                "No errors thrown, but unable to get NorKyst800 data from get request. Is the URL correct?"
            )
            return@load null
        }

        if (responseBinary.isEmpty()) {
            Log.e(TAG, "Empty response")
            return null
        }

        //open the file
        NetcdfDataset(openInMemory("NorKyst800", responseBinary)).let { ncfile ->
            ncfile.findCoordinateAxis("")
            //now, comes pre-sliced form the THREDDS server, so we merely have to wrap everything into
            //a NorkYst800-object.
            //make the projection
            val gridMapping: Variable = ncfile.findVariable("grid_mapping")
                ?: throw NullPointerException("Failed to read variable <gridMapping> from norKyst800") //caught by THREDDSLOAD
            val latLngToStereo =
                readAndMakeProjectionFromGridMapping(gridMapping) //can throw NullpointerException, caught by THREDDSLOAD
            //Variables are data that are NOT READ YET. findVariable() is not null-safe
            val depth: Variable = ncfile.findVariable("depth")
                ?: throw NullPointerException("Failed to read variable <time> from NorKyst800") //caught by THREDDSLOAD
            val time: Variable = ncfile.findVariable("time")
                ?: throw NullPointerException("Failed to read variable <depth> from NorKyst800") //caught by THREDDSLOAD
            val salinity: Variable = ncfile.findVariable("salinity")
                ?: throw NullPointerException("Failed to read variable <salinity> from NorKyst800") //caught by THREDDSLOAD
            val temperature: Variable = ncfile.findVariable("temperature")
                ?: throw NullPointerException("Failed to read variable <temperature> from NorKyst800") //caught by THREDDSLOAD
            val u: Variable = ncfile.findVariable("u")
                ?: throw NullPointerException("Failed to read variable <u> from NorKyst800") //caught by THREDDSLOAD
            val v: Variable = ncfile.findVariable("v")
                ?: throw NullPointerException("Failed to read variable <v> from NorKyst800") //caught by THREDDSLOAD
            val w: Variable = ncfile.findVariable("w")
                ?: throw NullPointerException("Failed to read variable <w> from NorKyst800") //caught by THREDDSLOAD
            //Read out the filler-value for the datasets that have those
            val salinityFillValue = ncfile.readAttributeInteger(salinity, "_FillValue", -32767).toDouble()
            val temperatureFillValue = ncfile.readAttributeInteger(temperature, "_FillValue", -32767).toDouble()
            val uFillValue = ncfile.readAttributeInteger(u, "_FillValue", -32767).toDouble()
            val vFillValue = ncfile.readAttributeInteger(v, "_FillValue", -32767).toDouble()
            val wFillValue = ncfile.readAttributeInteger(w, "_FillValue", -32767).toDouble()



            //make the infectiousPressure
            NorKyst800(
                (depth.read() as ArrayDouble).to1DDoubleArray(),
                (salinity.read() as ArrayDouble).toNullable4DDoubleArray(salinityFillValue),
                (temperature.read() as ArrayDouble).toNullable4DDoubleArray(temperatureFillValue),
                (time.read() as ArrayDouble).to1DDoubleArray(),
                Triple(
                    (u.read() as ArrayDouble).toNullable4DDoubleArray(uFillValue),
                    (v.read() as ArrayDouble).toNullable4DDoubleArray(vFillValue),
                    (w.read() as ArrayDouble).toNullable4DDoubleArray(wFillValue)
                )
            )
        }
        return null
    }

    /**
     * return data in a box specified by the given coordinates.
     *
     * @param latLongUpperLeft upper left coordinate in a box
     * @param latLongLowerRight lower right coordinate in a box
     * @param xStride stride between x-values in grid. 1=max resolution
     * @param yStride stride between y-values in grid. 1=max resolution
     * @param depthRange depth as a range with format from:stride:to
     * @param timeRange time as a range with format from:stride:to
     * @return NorKyst800 data in the prescribed data range
     */
    suspend fun load(
        latLongUpperLeft: LatLong,
        latLongLowerRight: LatLong,
        xStride: Int,
        yStride: Int,
        depthRange: IntProgression,
        timeRange: IntProgression
    ): NorKyst800? {
        //convert parameters to ranges
        val (xRange, yRange) = geographicCoordinateToRange(
            latLongUpperLeft,
            latLongLowerRight,
            xStride,
            yStride,
            Options.defaultProjection() //TODO: cannot be read from file in .ascii solution, but MIGHT be wrong
        )
        return load(xRange, yRange, depthRange, timeRange)
    }

    //load with default parameters(as specified in Options)
    suspend fun loadDefault(): NorKyst800? = load(
        Options.defaultNorKyst800XRange,
        Options.defaultNorKyst800YRange,
        Options.defaultNorKyst800DepthRange,
        Options.defaultNorKyst800TimeRange
    )

    ///////////////
    // UTILITIES //
    ///////////////
    protected fun makeParametrizedUrl(
        baseUrl: String,
        xRange: IntProgression,
        yRange: IntProgression,
        depthRange: IntProgression,
        timeRange: IntProgression
    ): String {
        val xyString =
            "[${reformatIntProgressionFSL(xRange)}][${reformatIntProgressionFSL(yRange)}]"
        val dString = "[${reformatIntProgressionFSL(depthRange)}]"
        val tString = "[${reformatIntProgressionFSL(timeRange)}]"
        val dtxyString = dString + tString + xyString
        return baseUrl +
                "depth$dString," +
                "lat$xyString," +
                "lon$xyString," +
                "salinity$dtxyString," +
                "temperature$dtxyString," +
                "time$tString," +
                "u$dtxyString," +
                "v$dtxyString," +
                "w$dtxyString"
    }

    /**
     * load the thredds catalog for the norkyst800 dataset, then return the URL
     * for the forecast data(which changes periodically)
     */
    private val forecastUrlRegex =
        Regex("""'catalog\.html\?dataset=norkyst800m_1h_files/(.*?\.fc\..*?)'""")

    protected suspend fun loadForecastUrl(): String? = try {
        Fuel.get(catalogUrl).awaitString()
    } catch (e: Exception) {
        Log.e(TAG, "Unable to retrieve NorKyst800 catalog due to", e)
        null
    }?.let { responseStr -> //regex out the url with .fc. in it
        forecastUrlRegex.find(responseStr)?.let { match ->
            "https://thredds.met.no/thredds/dodsC/fou-hi/norkyst800m-1h/" +
                    match.groupValues[1] + //if there is a match, the group (entry[1]) is guaranteed to exist
                    ".dods?"
        } ?: run { //"catch" unsucssessfull parse
            Log.e(
                TAG,
                "Failed to parse out the forecast URL from\n ${responseStr}\n with regex $forecastUrlRegex,\n returning null"
            )
            null
        }
    }
}