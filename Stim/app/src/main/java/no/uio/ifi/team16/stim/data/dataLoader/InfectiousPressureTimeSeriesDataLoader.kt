package no.uio.ifi.team16.stim.data.dataLoader

import no.uio.ifi.team16.stim.data.InfectiousPressureTimeSeries
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.util.FloatArray2D
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate
import ucar.ma2.ArrayFloat
import ucar.nc2.Variable
import java.util.*
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
class InfectiousPressureTimeSeriesDataLoader : THREDDSDataLoader() {
    private val TAG = "InfectiousPressureTimeSeriesDataLoader"

    val baseUrl = "http://thredds.nodc.no:8080/thredds/fileServer/smittepress_new2018/agg_OPR_"
    val radius = 1 //amount of grid cells around the specified one to collect data from.

    /**
     * return the year and week of the given data in yyyy_w format
     */
    fun yearAndWeek(date: Date, weeksFromNow: Int): String {
        //TODO: this MIGHT be wrong, datasets are made on wednesdays, but published... some time after that?
        //TODO: wrong if weeksfromnow is larger than weeks, it does not consider year!
        val week = ((date.getTime() - Date(
            date.year,
            0,
            0
        ).getTime()) / 1000 / 60 / 60 / 24 / 7) - weeksFromNow
        return date.year.toString() +
                "_" +
                (if (week == 0L) 52 else week).toString()
    }

    /**
     * return the current date
     */
    fun currentDate(): Date {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        return Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
    }

    /**
     *
     */
    fun load(
        site: Site,
        weeksRange: IntProgression
    ): InfectiousPressureTimeSeries? {
        return null
    }

    /**
     *
     *
     * @see THREDDSDataLoader.THREDDSLoad()
     */
    fun load(
        site: Site,
        weeksFromNow: Int
    ): InfectiousPressureTimeSeries? {
        var out: MutableList<Pair<Int, FloatArray2D>> = mutableListOf()
        var dx: Float = 0f
        var dy: Float = 0f
        var shape: Pair<Int, Int> = Pair(0, 0)
        val latLng = site.latLong
        for (week in 0 until weeksFromNow) {
            THREDDSLoad(baseUrl + yearAndWeek(currentDate(), week) + ".nc") { ncfile ->
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

                //take out the arrayfloat
                out.add(
                    Pair(
                        ncfile.findGlobalAttribute("weeknumber")!!.numericValue.toInt(), //global attribute week always exists
                        ((concentrations.read("0,${minY}:${maxY},${minX}:${maxX}")
                            .reduce(0) as ArrayFloat).to2DFloatArray())
                    )
                )
            }
        }
        return InfectiousPressureTimeSeries(
            site.id,
            out.toTypedArray().reversedArray(),
            shape,
            dx,
            dy
        )
    }
}