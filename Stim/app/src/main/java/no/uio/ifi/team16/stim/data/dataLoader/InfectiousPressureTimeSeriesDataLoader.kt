package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import no.uio.ifi.team16.stim.data.InfectiousPressureTimeSeries
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.util.Options
import no.uio.ifi.team16.stim.util.project
import no.uio.ifi.team16.stim.util.to2DFloatArray
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import ucar.ma2.ArrayFloat
import ucar.nc2.Variable
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
     * always starting from the most rec
     * Note that the weeksrange are weeks from now, and not specific weeks!
     * The amount of grid points around a site to get is specified in Options, through Options.siteRadius
     *
     * @param site: the site to load infectiousPressure at
     * @param weeksRange: an intprogression of weeks from now to get. fromClosedRange(2,13,3)
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

        var dx: Float
        var dy: Float
        var shape: Pair<Int, Int> = Pair(0, 0)
        val latLng = site.latLong

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

        //open the first file, which is used to get the data common for all datasets
        return THREDDSLoad(firstEntry) { firstncfile ->
            //get common data variables
            val gridMapping: Variable = firstncfile.findVariable("grid_mapping")
                ?: run {
                    Log.e(TAG, "Failed to read variable <gridMapping> from infectiousPressure")
                    return@THREDDSLoad null //specidy scope of lambda to allow return
                }
            dx = gridMapping.findAttribute("dx")?.numericValue?.toFloat()
                ?: run {
                    Log.e(
                        TAG,
                        "Failed to read attribute <dx> from <gridMapping> from infectiousPressure"
                    )
                    return@THREDDSLoad null //specidy scope of lambda to allow return
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
                        return@THREDDSLoad null //specidy scope of lambda to allow return
                    }
            )
            val latLngCRT = stereoCRT.createGeographic()
            val ctFactory = CoordinateTransformFactory()
            val latLngToStereo: CoordinateTransform =
                ctFactory.createTransform(latLngCRT, stereoCRT)

            val (y, x) = latLngToStereo.project(site.latLong).let { (xf, yf) ->
                Pair((xf / 800).roundToInt(), (yf / 800).roundToInt())
            }

            val minX = max(x - radius, 0)
            val maxX = min(max(x + radius, 0), Options.norKyst800XEnd)
            val minY = max(y - radius, 0)
            val maxY = min(max(y + radius, 0), Options.norKyst800YEnd)

            /*
            all common data parsed, now open each dataset. Note the mapasync which uses coroutines.
            this is done in the scope of the first load so as to get the variables from this load,
            we could probably just return the desired data from the first load, or store it in the enclosing scope
            but this is more convenient, albeit less readable:/ Also, this keeps the first dataset
            open throughout opening all the others, which is unnecessary
            */
            catalogEntries.takeRange(weeksRange)
                .map { entryUrl -> //use mapAsync to load asynchronously, however the servers cannot handle parallell loads!
                    THREDDSLoad(entryUrl) { ncfile ->
                        //concentration is unique to each dataset
                        val concentrations: Variable = ncfile.findVariable("C10")
                            ?: run {
                                Log.e(TAG, "Failed to read variable <C10> from infectiousPressure")
                                return@THREDDSLoad null //specidy scope of lambda to allow return
                                /*TODO: check how return works here, we actyually want to return from
                                just the lambda, and continue reading the other datasets*/
                            }

                    //TODO: can be moved to first dataset, somehow
                    shape = Pair(concentrations.getShape(1), concentrations.getShape(2))

                    //take out the arrayfloat(s) for this dataset, return (weeknumber, concnetration)
                    Pair(
                        ncfile.findGlobalAttribute("weeknumber")!!.numericValue.toInt(), //global attribute week always exists
                        ((concentrations.read("0,${minX}:${maxX},${minY}:${maxY}")
                            .reduce(0) as ArrayFloat).to2DFloatArray())
                    ) //end threddsload
                } //end async mapping
            }.filterNotNull().let { data -> //wrap the data in infectiousPressureTimeSeries
                    //unzip list of pairs to pair of lists
                    val (weeks, contamination) = data.unzip()
                    InfectiousPressureTimeSeries(
                        site.nr,
                        contamination.toTypedArray(),
                        weeks.toTypedArray(),
                        shape,
                        dx,
                        dy
                    )
            }
        }
    }
}