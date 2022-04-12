package no.uio.ifi.team16.stim.data.dataLoader

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
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
open class InfectiousPressureDataLoader : THREDDSDataLoader() {
    private val TAG = "InfectiousPressureDataLoader"

    /*the catalogentries are the datasets in the catalog. There are a lot so it should be loaded only
    once per run. However the catalog updates at wednesdays, so if the application is started 5 min before
    and caches the result, the cache will be incorrect when the catalog updates and will be unable to
    check the catalog.
    * */
    protected var dirtyCatalogCache = true
    protected var catalogCache: Sequence<String> = sequenceOf()

    protected val baseUrl = "http://thredds.nodc.no:8080/thredds/fileServer/smittepress_new2018/"

    protected val catalogUrl =
        "http://thredds.nodc.no:8080/thredds/catalog/smittepress_new2018/catalog.html"

    /**
     * load the default dataset
     */
    suspend fun loadDefault(): InfectiousPressure? =
        load(
            fromClosedRange(0, 901, Options.infectiousPressureStepX),
            fromClosedRange(0, 2601, Options.infectiousPressureStepY)
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
     * return the current date
     */
    fun currentDate(): Date {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        return Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
    }

    /**
     * return data between latitude from/to, and latitude from/to, with given resolution.
     * Uses minimum of given and possible resolution.
     * crops to dataset if latitudes or longitudes exceed the dataset.
     *
     * @param TODO
     * @param TODO
     * @return data of infectious pressure in the prescribed data range.
     *
     * @see THREDDSDataLoader.THREDDSLoad()
     */
    suspend fun load(
        xRange: IntProgression,
        yRange: IntProgression
    ): InfectiousPressure? {
        val catalogEntries = loadEntryUrls() //fills catalogCache, blocks the current coroutine
        return THREDDSLoad(catalogEntries?.firstOrNull() ?: return null) { ncfile ->
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
            val range2 = "${reformatIntProgression(xRange)},${reformatIntProgression(yRange)}"
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
    }

    /**
     * return data between latitude from/to, and latitude from/to, with given resolution.
     * Uses minimum of given and possible resolution.
     * crops to dataset if latitudes or longitudes exceed the dataset.
     *
     * @param latLongUpperLeft latlong of upper left corner in a box
     * @param latLongLowerRight latlong of lower right corner in a box
     * @param xStride stride between x coordinates
     * @param yStride stride between y coordinates
     * @return data of infectious pressure in the prescribed data range.
     *
     * @see THREDDSDataLoader.THREDDSLoad()
     */
    suspend fun load(
        latLongUpperLeft: LatLong,
        latLongLowerRight: LatLong,
        xStride: Int,
        yStride: Int
    ): InfectiousPressure? {
        val catalogEntries = loadEntryUrls() //fills catalogCache, blocks the current coroutine
        return THREDDSLoad(catalogEntries?.firstOrNull() ?: return null) { ncfile ->
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

            val (xRange, yRange) = geographicCoordinateToRange(
                latLongUpperLeft,
                latLongLowerRight,
                xStride,
                yStride,
                latLngToStereo
            )

            //make some extra ranges to access data
            val range2 = "${reformatIntProgression(xRange)},${reformatIntProgression(yRange)}"
            val range3 = "0,$range2"

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
    }

    /**
     * Make a list of all url-entries in the catalog, sorted by date(newer first)
     *
     * regex out all entries in the catalog, also parsing out year and week of the entries,
     * for each triple of year, week entryName,
     * associate year*52+week to entryName, giving a map where the keys are weeks from year 0(excluding some leap stuff, but should work(?))
     * return a list where values(catalog-entry names) are sorted by keys(weeks from year 0),
     * so that catalog-entries are sorted by time
     *
     * CACHES ON FIRST CALL
     *
     * Sorting ruins the sequencing, ie all entries are loaded greedily.
     * Probably not possible to make lazy since entries are sorted weirdly in catalog
     */
    //get the name, year and week of an entry in the catalog, used to get url.
    //group1: entry name
    //group2: entry year
    //group3: entry week
    private val catalogEntryRegex =
        Regex("""'catalog\.html\?dataset=smittepress_new2018/(agg_OPR_(.*?)_(.*?)\.nc)'""")

    protected suspend fun loadEntryUrls(): Sequence<String>? =
        if (dirtyCatalogCache) {
            try {
                Fuel.get(catalogUrl).awaitString()
            } catch (e: Exception) {
                Log.e(TAG, "Unable to retrieve smittepress catalog due to", e)
                null
            }?.let { responseStr ->
                catalogEntryRegex.findAll(responseStr)
                    .map { catalogEntryMatch -> //for every match(catalog entry)
                        //the following groups are guaranteed to exist, from the way the regex is constructed
                        val name = catalogEntryMatch.groupValues[1]
                        val year = catalogEntryMatch.groupValues[2].toInt()
                        val week = catalogEntryMatch.groupValues[3].toInt()
                        //associate weeks from year 0 to name
                        //the first entry of the year is labeled as week 52, for some reason, map to 0
                        year * 52 + (if (week == 52) 0 else week) to name
                    }.sortedByDescending { (weeksFromYear0, name) -> //sort by weeks from year 0
                        weeksFromYear0
                    }.map { (weeksFromYear0, name) -> //reduce to only urls
                        baseUrl + name
                    } //we have a list of urls, sorted by date!
            }?.let { result -> //if everything succeeded, store in cache and return it
                Log.d(TAG, "loaded catalog ${result.toList()}")
                catalogCache = result
                dirtyCatalogCache = false
                catalogCache
            } ?: run { //"catch" unsucssessfull parse
                Log.e(TAG, "Failed to get infectiouspressure catalog: $catalogUrl")
                null
            }
        } else { //if cache available
            catalogCache
        }
}