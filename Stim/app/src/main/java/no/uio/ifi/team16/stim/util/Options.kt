package no.uio.ifi.team16.stim.util

import no.uio.ifi.team16.stim.data.Site
import kotlin.ranges.IntProgression.Companion.fromClosedRange

class Options {
    companion object {
        //NorKyst800
        var defaultNorKyst800XStride = 1
        var defaultNorKyst800YStride = 1
        var defaultNorKyst800DepthStride = 1
        var defaultNorKyst800TimeStride = 1

        const val defaultNorKyst800DepthEnd = 0 //15
        const val defaultNorKyst800TimeEnd = 0 //16
        const val norKyst800XEnd = 901
        const val norKyst800YEnd = 2601
        var defaultNorKyst800XRange = fromClosedRange(0, norKyst800XEnd, defaultNorKyst800XStride)
        var defaultNorKyst800YRange = fromClosedRange(0, norKyst800YEnd, defaultNorKyst800YStride)
        var defaultNorKyst800DepthRange =
            fromClosedRange(0, defaultNorKyst800DepthEnd, defaultNorKyst800DepthStride)
        var defaultNorKyst800TimeRange =
            fromClosedRange(0, defaultNorKyst800TimeEnd, defaultNorKyst800TimeStride)
        const val defaultProj4String =
            "+proj=stere +ellps=WGS84 +lat_0=90.0 +lat_ts=60.0 +x_0=3192800 +y_0=1784000 +lon_0=70"  //retrieved from opendap grid_mapping attribute

        const val norKyst800MaxRadius =
            1 //the largest radius of circle around a site to search for non-null values


        const val norKyst800AtSiteRadius = 1
        val norKyst800AtSiteDepthRange = fromClosedRange(0, 1, 1)
        val norKyst800AtSiteTimeRange = fromClosedRange(0, 23, 1)

        //INFECTIOUSPRESSURE - DATALOADER
        var infectiousPressureStepX =
            1 //amount of steps between data points, 1=use entire x axis
        var infectiousPressureStepY =
            1 //amount of steps between data points, 1=use entire y axis

        //InfectiousPressureTimeseries
        const val infectiousPressureTimeSeriesSpan =
            20 //how many weeks from now to load in a timeseries
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

        //decrease resolution of the largest datasets by half
        fun decreaseDataResolution() {
            //norkyst
            defaultNorKyst800XStride *= 2
            defaultNorKyst800YStride *= 2
            defaultNorKyst800DepthStride *= 2
            defaultNorKyst800TimeStride *= 2
            //infectiousPressure
            infectiousPressureStepX *= 2
            infectiousPressureStepY *= 2

            //update the ranges that depend on the now changed strides
            defaultNorKyst800XRange = fromClosedRange(0, norKyst800XEnd, defaultNorKyst800XStride)
            defaultNorKyst800YRange = fromClosedRange(0, norKyst800YEnd, defaultNorKyst800YStride)
            defaultNorKyst800DepthRange =
                fromClosedRange(0, defaultNorKyst800DepthEnd, defaultNorKyst800DepthStride)
            defaultNorKyst800TimeRange =
                fromClosedRange(0, defaultNorKyst800TimeEnd, defaultNorKyst800TimeStride)
            //infectiouspressure is made into range in separate function
        }
    }
}