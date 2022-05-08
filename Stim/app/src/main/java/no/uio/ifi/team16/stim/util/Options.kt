package no.uio.ifi.team16.stim.util

import no.uio.ifi.team16.stim.data.Site
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import kotlin.ranges.IntProgression.Companion.fromClosedRange

class Options {
    companion object {
        //NorKyst800
        var defaultNorKyst800XStride = 1
        var defaultNorKyst800YStride = 1
        const val defaultNorKyst800DepthStride = 1
        const val defaultNorKyst800TimeStride = 1
        const val defaultNorKyst800DepthEnd = 0 //15
        const val defaultNorKyst800TimeEnd = 0 //16
        const val norKyst800XEnd = 901
        const val norKyst800YEnd = 2601
        var defaultNorKyst800XRange = fromClosedRange(0, norKyst800XEnd, defaultNorKyst800XStride)
        var defaultNorKyst800YRange = fromClosedRange(0, norKyst800YEnd, defaultNorKyst800YStride)
        val defaultNorKyst800DepthRange =
            fromClosedRange(0, defaultNorKyst800DepthEnd, defaultNorKyst800DepthStride)
        val defaultNorKyst800TimeRange =
            fromClosedRange(0, defaultNorKyst800TimeEnd, defaultNorKyst800TimeStride)
        const val defaultProj4String =
            "+proj=stere +ellps=WGS84 +lat_0=90.0 +lat_ts=60.0 +x_0=3192800 +y_0=1784000 +lon_0=70"  //retrieved from opendap grid_mapping attribute
        val defaultProjection: () -> CoordinateTransform =
            { //functional, to avoid initialization problems with static
                CRSFactory().createFromParameters(null, defaultProj4String).let { stereoCRT ->
                    val latLngCRT = stereoCRT.createGeographic()
                    val ctFactory = CoordinateTransformFactory()
                    ctFactory.createTransform(latLngCRT, stereoCRT)
                }
            }

        const val norKyst800MaxRadius =
            1 //the largest radius of circle around a site to search for non-null values


        const val norKyst800AtSiteRadius = 2
        val norKyst800AtSiteDepthRange = fromClosedRange(0, 1, 1)
        val norKyst800AtSiteTimeRange = fromClosedRange(0, 23, 1)

        //INFECTIOUSPRESSURE - DATALOADER
        const val infectiousPressureStepX =
            50 //amount of steps between data points, 1=use entire x axis
        const val infectiousPressureStepY =
            50 //amount of steps between data points, 1=use entire y axis

        //InfectiousPressureTimeseries
        const val infectiousPressureTimeSeriesSpan =
            8 //how many weeks from now to load in a timeseries
        const val siteRadius = 1 //the amount of grids around the site to use in timeseriesdata,
        //a value of 1 corresponds to using a 3x3 grid(the site grid, and 1 grid point all around)
        //and a value of 2 to a 5x5 grid (the site grid, and 2 grid points all around).

        //SITES - DATALOADER
        const val sitesRange = "0-99"

        //MAINACTIVITY
        val fakeSite = Site(420, "fakevik", LatLong(59.910073, 10.743205), null, 3.141592, null, null)

        //INFECTIONFRAGMENT OPTIONS
        val high = 5
        val infectionExists = 1
        val increase = 0.5
        val decrease = 0.5

        // Key for favourites in SharedPreferences
        const val FAVOURITES = "Favorites"
        const val SHARED_PREFERENCES_KEY = "prefrences"
    }
}