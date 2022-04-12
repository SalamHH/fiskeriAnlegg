package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import no.uio.ifi.team16.stim.data.InfectiousPressureTimeSeries
import no.uio.ifi.team16.stim.data.Site
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate
import ucar.ma2.ArrayFloat
import ucar.nc2.Variable
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

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

    val radius = 1 //amount of grid cells around the specified one to collect data from.

    /**
     * return the infectious pressure at a given site over a given amount of weeks,
     * always starting from the most recent week and backwards.
     *
     * @param site: the site to load infectiousPressure at
     * @param weeksFromNow: the amount of weeks from the most recent one and backwards
     * @returns InfectiousPressureTimeSeres
     */
    suspend fun load(
        site: Site,
        weeksRange: IntProgression
    ): InfectiousPressureTimeSeries? {
        var dx: Float = 0f
        var dy: Float = 0f
        var shape: Pair<Int, Int> = Pair(0, 0)
        val latLng = site.latLong
        //load name of all entries in catalog
        val catalogEntries = loadEntryUrls()?.toList() ?: run {
            Log.e(TAG, "Failed to open thredds catalog")
            return null
        }

        //open the first file, which is used to get the data common for all datasets
        return THREDDSLoad(catalogEntries.firstOrNull() ?: return null) { firstncfile ->
            //get common data
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

            //project latlng to etaxi
            val (y, x) = ProjCoordinate().let { p ->
                latLngToStereo.transform(ProjCoordinate(latLng.lng, latLng.lat), p)
            }.let { p ->
                Pair(round(p.y / dy), round(p.x / dx))
            }

            //all common data parsed, now open each dataset. Note the mapasync which uses coroutines
            catalogEntries.takeRange(weeksRange)?.mapAsync { entryUrl ->
                THREDDSLoad(entryUrl) { ncfile ->
                    //concentration is unique to each dataset
                    val concentrations: Variable = ncfile.findVariable("C10")
                        ?: run {
                            Log.e(TAG, "Failed to read variable <C10> from infectiousPressure")
                            return@THREDDSLoad null //specidy scope of lambda to allow return
                        }

                    //make valid range around the point, minimum 0, not larger than bounds
                    //TODO: can be moved to first dataset, somehow
                    val minX = round(max(x - radius, 0.0)).toInt()
                    val maxX =
                        round(
                            min(
                                max(x + radius, 0.0),
                                concentrations.getShape(2).toDouble()
                            )
                        ).toInt()
                    val minY = round(max(y - radius, 0.0)).toInt()
                    val maxY =
                        round(
                            min(
                                max(y + radius, 0.0),
                                concentrations.getShape(1).toDouble()
                            )
                        ).toInt()
                    shape = Pair(concentrations.getShape(1), concentrations.getShape(2))

                    //take out the arrayfloat(s) for this dataset, return (weeknumber, concnetration)
                    Pair(
                        ncfile.findGlobalAttribute("weeknumber")!!.numericValue.toInt(), //global attribute week always exists
                        ((concentrations.read("0,${minY}:${maxY},${minX}:${maxX}")
                            .reduce(0) as ArrayFloat).to2DFloatArray())
                    ) //end threddsload
                } //end mapping
            }?.filterNotNull()?.let { data -> //wrap the data in infectiousPressureTimeSeries
                InfectiousPressureTimeSeries(site.id, data.toTypedArray(), shape, dx, dy)
            }
        }
    }

    /**
     * let one coroutine handle each entry, then join
     */
    private suspend fun <T, U> List<T>.mapAsync(f: (T) -> U): List<U> = map { t ->
        CoroutineScope(Dispatchers.IO).async(Dispatchers.IO) {
            f(t)
        }
    }.map { deferred ->
        deferred.await()
    }
}