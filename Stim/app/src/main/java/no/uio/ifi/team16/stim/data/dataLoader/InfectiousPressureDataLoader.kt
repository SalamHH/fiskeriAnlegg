package no.uio.ifi.team16.stim.data.dataLoader

import no.uio.ifi.team16.stim.data.InfectiousPressure
import no.uio.ifi.team16.stim.util.LatLong
import no.uio.ifi.team16.stim.util.Options
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import ucar.ma2.ArrayFloat
import ucar.nc2.Variable
import java.util.*
import kotlin.math.max
import kotlin.ranges.IntProgression.Companion.fromClosedRange

/**
 * DataLoader for infectious pressure data.
 *
 * Data is loaded through load(...) and returned as an InfectiousPressure, with the
 * concentration of salmon louse represented as a grid.
 **/
class InfectiousPressureDataLoader : THREDDSDataLoader() {
    private val TAG = "InfectiousPressureDataLoader"

    val baseUrl = "http://thredds.nodc.no:8080/thredds/fileServer/smittepress_new2018/agg_OPR_"

    /**
     * load the default dataset
     */
    fun loadDefault(): InfectiousPressure? =
        load(
            fromClosedRange(0, Options.infectiousPressureStepX, 901),
            fromClosedRange(0, Options.infectiousPressureStepY, 2601)
        )

    /**
     * return the year and week of the given data in yyyy_w format
     * , fjernes med norkyst800-regexed
     */
    fun yearAndWeek(date: Date): String {
        //TODO: this MIGHT be wrong, datasets are made on wednesdays, but published... some time after that?
        val week = ((date.getTime() - Date(date.year, 0, 0).getTime()) / 1000 / 60 / 60 / 24 / 7)
        return date.year.toString() +
                "_" +
                (if (week == 0L) 52 else week).toString()
    }

    /**
     * return the current date, fjernes med norkyst800-regexed
     */
    fun currentDate(): Date {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        return Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
    }

    fun load(
        xRange: IntProgression,
        yRange: IntProgression
    ): InfectiousPressure? = THREDDSLoad(baseUrl + yearAndWeek(currentDate()) + ".nc") { ncfile ->
        //lets make some infectious pressure
        //Variables are data that are NOT READ YET. findVariable() is not null-safe
        val concentrations: Variable = ncfile.findVariable("C10")
            ?: throw NullPointerException("Failed to read variable <C10> from infectiousPressure")
        val time: Variable = ncfile.findVariable("time")
            ?: throw NullPointerException("Failed to read variable <time> from infectiousPressure")
        val gridMapping: Variable = ncfile.findVariable("grid_mapping")
            ?: throw NullPointerException("Failed to read variable <gridMapping> from infectiousPressure")
        val dx = gridMapping.findAttribute("dx")?.numericValue?.toFloat()
            ?: throw NullPointerException("Failed to read attribute <dx> from <gridMapping> from infectiousPressure")
        //make some extra ranges to access data
        val range2 = "$xRange,$yRange"
        val range3 = "0,$range2"

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

        //make the infectiousPressure
        InfectiousPressure(
            (concentrations.read(range3).reduce(0) as ArrayFloat).to2DFloatArray(),
            time.readScalarFloat(),
            latLngToStereo,
            ncfile.findGlobalAttribute("fromdate")?.run {
                parseDate(this.stringValue)
            },
            ncfile.findGlobalAttribute("todate")?.run {
                parseDate(this.stringValue)
            },
            dx * max(Options.infectiousPressureStepX, 1).toFloat(),
            dx * max(Options.infectiousPressureStepY, 1).toFloat()
        )
    }

    /**
     * return data between latitude from/to, and latitude from/to, with given resolution.
     * Uses minimum of given and possible resolution.
     * crops to dataset if latitudes or longitudes exceed the dataset.
     *
     * @param latitudeFrom smallest latitude to get data from
     * @param latitudeTo largest latitude to get data from
     * @param latitudeResolution resolution of latitude. A latitude resolution of 0.001 means that
     * the data is sampled from latitudeFrom to latitudeTo with 0.001 latitude between points
     * @param longitudeFrom smallest longitude to get data from
     * @param longitudeTo largest longitude to get data from
     * @param latitudeResolution resolution of longitude.
     * @return data of infectious pressure in the prescribed data range.
     *
     * @see THREDDSDataLoader.THREDDSLoad()
     */
    fun load(
        latLongUpperLeft: LatLong,
        latLongLowerRight: LatLong,
        latitudeResolution: Int,
        longitudeResolution: Int
    ): InfectiousPressure? = THREDDSLoad(baseUrl + yearAndWeek(currentDate()) + ".nc") { ncfile ->
        //convert parameters to ranges
        val (xRange, yRange) = geographicCoordinateToRange(
            latLongUpperLeft, latLongLowerRight, latitudeResolution, longitudeResolution
        )
        load(xRange, yRange)
    }
}