package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.uio.ifi.team16.stim.data.InfectiousPressureTimeSeries
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.util.*
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import ucar.ma2.ArrayFloat
import ucar.nc2.Variable
import ucar.nc2.dataset.NetcdfDataset
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Dataloader for infectiouspressure over time.
 *
 * Infectiouspressure is a large grid at one exact time, and will not play well with the representation
 * of infectiouspressure over a small area and several times. So we have to make a separate dataloader
 * for this format.
 *
 * Generally this type of infectiouspressure will be for a specific site, and at a site we are interested
 * in a few squares around that site, so the shape of this data will be something like time.length()x3x3
 *
 */
class InfectiousPressureTimeSeriesDataLoader : InfectiousPressureDataLoader() {
    private val TAG = "InfectiousPressureTimeSeriesDataLoader"

    private val radius =
        Options.siteRadius //amount of grid cells around the specified one to collect data from.


    /////////////
    // LOADERS //
    /////////////
    /**
     * return the infectious pressure at a given site over a given amount of weeks,
     * always starting from the most recent week
     * Note that the weeksrange are weeks from now, and not specific weeks!
     * The amount of grid points around a site to get is specified in Options, through Options.siteRadius
     *
     * @param site: the site to load infectiousPressure at
     * @param weeksRange: an intprogression of weeks from now, to get. fromClosedRange(2,13,3)
     *                    corresponds to week 2, till week 13 with a stride of 3 FROM NOW, and will
     *                    return the data from week 2, 5, 8 and 11 FROM CURRENT WEEK.
     * @returns InfectiousPressureTimeSeres in the given range at the given site.
     */
    suspend fun load(
        site: Site,
        weeksRange: IntProgression
    ): InfectiousPressureTimeSeries? {
        /*
        first, load the entries in the catalog, then use the first enty in the catalog to get the data
        that is common for all datasets. After this we open the datasets corresponding to weeksRange,
        and use these to make the infectiousPressureTimeseries.
         */


        //val latLng = site.latLong

        //load name of all entries in catalog
        val catalogEntries = loadEntryUrls()?.toList() ?: run {
            Log.e(TAG, "Failed to open thredds catalog")
            return null
        }

        //get the first entry
        val firstEntry = catalogEntries.firstOrNull() ?: run {
            Log.e(TAG, "Failed to get anything from the catalog after opening, is the url correct?")
            return null
        }

        //attributes common to all files
        var dx = 800f
        var dy = 800f
        var shape: Pair<Int, Int> = Pair(0, 0)
        var y: Int
        var x: Int
        var minX = 0
        var maxX = 0
        var minY = 0
        var maxY = 0
        var latLngToStereo: CoordinateTransform

        //open the first file, which is used to get the data common for all datasets
        ///////////////////
        // FIRST DATASET //
        ///////////////////
        val currentData = THREDDSLoad(firstEntry) { firstncfile ->
            //get common data variables
            val gridMapping: Variable = firstncfile.findVariable("grid_mapping")
                ?: run {
                    Log.e(TAG, "Failed to read variable <gridMapping> from infectiousPressure")
                    return null
                }
            dx = gridMapping.findAttribute("dx")?.numericValue?.toFloat()
                ?: run {
                    Log.e(
                        TAG,
                        "Failed to read attribute <dx> from <gridMapping> from infectiousPressure"
                    )
                    return null
                }
            dy = dx
            //make the projection
            val crsFactory = CRSFactory()
            val stereoCRT = crsFactory.createFromParameters(
                null,
                gridMapping.findAttribute("proj4string")?.stringValue
                    ?: run {
                        Log.e(
                            TAG,
                            "Failed to read attribute <proj4string> from <gridMapping> from infectiousPressure"
                        )
                        return null
                    }
            )
            val latLngCRT = stereoCRT.createGeographic()
            val ctFactory = CoordinateTransformFactory()
            latLngToStereo = ctFactory.createTransform(latLngCRT, stereoCRT)

            latLngToStereo.project(site.latLong).let { (xf, yf) ->
                y = (xf / 800).roundToInt()
                x = (yf / 800).roundToInt()
            }

            minX = max(x - radius, 0)
            maxX = min(max(x + radius, 0), Options.norKyst800XEnd)
            minY = max(y - radius, 0)
            maxY = min(max(y + radius, 0), Options.norKyst800YEnd)

            getDataFromFile(firstncfile, minX, maxX, minY, maxY)
        } ?: return null


        /*
            all common data parsed, now open each dataset. Note the mapasync which uses coroutines.
            this is done in the scope of the first load so as to get the variables from this load,
            we could probably just return the desired data from the first load, or store it in the enclosing scope
            but this is more convenient, albeit less readable:/ Also, this keeps the first dataset
            open throughout opening all the others, which is unnecessary
            */
        var historicalData = MutableLiveData<Pair<Array<Int>, FloatArray3D>>()

        GlobalScope.launch(Dispatchers.IO) {
            historicalData.postValue(
                catalogEntries
                    .takeRange(weeksRange)
                    .drop(0) //drop the first entry, already loaded
                    .mapIndexedNotNull { i, entryUrl -> //use mapAsync to load asynchronously, however the servers cannot handle parallell loads!
                        THREDDSLoad(entryUrl) { ncfile ->
                            getDataFromFile(ncfile, minX, maxX, minY, maxY)
                        }?.let { data ->
                            Pair(
                                i + 1, //weeks from current week!
                                data.second
                            )
                        }
                    }.unzip().let { (weeks, data) ->
                        Pair(
                            weeks.toTypedArray(),
                            data.toTypedArray()
                        )
                    }
            )
        }

        return InfectiousPressureTimeSeries(
            site.nr,
            currentData.first,
            currentData.second,
            historicalData,
            shape,
            dx,
            dy
        )
    }

    private fun getDataFromFile(
        ncfile: NetcdfDataset,
        minX: Int,
        maxX: Int,
        minY: Int,
        maxY: Int
    ): Pair<Int, FloatArray2D>? {
        val concentrations: Variable = ncfile.findVariable("C10")
            ?: run {
                Log.e(TAG, "Failed to read variable <C10> from infectiousPressure")
                return null
            }
        //take out the arrayfloat(s) for this dataset, return (weeknumber, concnetration)
        return Pair(
            ncfile.findGlobalAttribute("weeknumber")!!.numericValue.toInt(), //global attribute week always exists
            ((concentrations.read("0,${minX}:${maxX},${minY}:${maxY}")
                .reduce(0) as ArrayFloat).to2DFloatArray())
        )
    }
}