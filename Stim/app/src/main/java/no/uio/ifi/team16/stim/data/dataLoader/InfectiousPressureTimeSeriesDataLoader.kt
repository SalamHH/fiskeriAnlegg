package no.uio.ifi.team16.stim.data.dataLoader

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
        weeksFromNow: Int
    ): InfectiousPressureTimeSeries? {
        //var out: MutableList<Pair<Int, FloatArray2D>> = mutableListOf()
        var dx: Float = 0f
        var dy: Float = 0f
        var shape: Pair<Int, Int> = Pair(0, 0)
        val latLng = site.latLong
        //load name of all entries in catalog
        val catalogEntries = loadEntryUrls()
        //fow the first (weeksfromnow) entries, open and parse
        return catalogEntries?.take(weeksFromNow)?.map { entryUrl ->
            THREDDSLoad(entryUrl) { ncfile ->
                //lets make some infectious pressure
                //Variables are data that are NOT READ YET. findVariable() is not null-safe
                //TODO: move outside for loop since only needs to be done once
                val concentrations: Variable = ncfile.findVariable("C10")
                    ?: throw NullPointerException("Failed to read variable <C10> from infectiousPressure")
                val time: Variable = ncfile.findVariable("time")
                    ?: throw NullPointerException("Failed to read variable <time> from infectiousPressure")
                val gridMapping: Variable = ncfile.findVariable("grid_mapping")
                    ?: throw NullPointerException("Failed to read variable <gridMapping> from infectiousPressure")
                dx = gridMapping.findAttribute("dx")?.numericValue?.toFloat()
                    ?: throw NullPointerException("Failed to read attribute <dx> from <gridMapping> from infectiousPressure")
                dy = dx
                shape = Pair(concentrations.getShape(1), concentrations.getShape(2))

                //make the projection
                val crsFactory = CRSFactory()
                val stereoCRT = crsFactory.createFromParameters(
                    null,
                    gridMapping.findAttribute("proj4string")?.stringValue
                        ?: throw NullPointerException("Failed to read attribute <proj4string> from <gridMapping> from infectiousPressure")
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

                //make valid range around the point, minimum 0, not larger than bounds
                val minX = round(max(x - radius, 0.0)).toInt()
                val maxX =
                    round(min(max(x + radius, 0.0), concentrations.getShape(2).toDouble())).toInt()
                val minY = round(max(y - radius, 0.0)).toInt()
                val maxY =
                    round(min(max(y + radius, 0.0), concentrations.getShape(1).toDouble())).toInt()

                //take out the arrayfloat(s)
                Pair(
                    ncfile.findGlobalAttribute("weeknumber")!!.numericValue.toInt(), //global attribute week always exists
                    ((concentrations.read("0,${minY}:${maxY},${minX}:${maxX}")
                        .reduce(0) as ArrayFloat).to2DFloatArray())
                )
            }
        }?.filterNotNull()?.let { data -> //wrap the data in infectiousPressureTimeSeries
            InfectiousPressureTimeSeries(site.id, data.toList().toTypedArray(), shape, dx, dy)
        }
    }
}