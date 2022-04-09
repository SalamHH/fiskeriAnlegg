package no.uio.ifi.team16.stim.util

import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.Sites
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory

class Options {
    companion object {
        //NorKyst800
        const val useDefault = true
        const val defaultNorKyst800XStride = 40
        const val defaultNorKyst800YStride = 40
        const val defaultNorKyst800DepthStride = "8"
        const val defaultNorKyst800TimeStride = "10"
        private const val defaultProj4String =
            "+proj=stere +ellps=WGS84 +lat_0=90.0 +lat_ts=60.0 +x_0=3192800 +y_0=1784000 +lon_0=70"  //retrieved from opendap grid_mapping attribute
        val defaultProjection: () -> CoordinateTransform =
            { //functional, to avoid initialization problems with static
                CRSFactory().createFromParameters(null, defaultProj4String).let { stereoCRT ->
                    val latLngCRT = stereoCRT.createGeographic()
                    val ctFactory = CoordinateTransformFactory()
                    ctFactory.createTransform(latLngCRT, stereoCRT)
                }
            }

        //INFECTIOUSPRESSURE - DATALOADER
        const val infectiousPressureStepX =
            50 //amount of steps between data points, 1=use entire x axis
        const val infectiousPressureStepY =
            50 //amount of steps between data points, 1=use entire y axis

        //InfectiousPressureTimeseries
        const val infectiousPressureTimeSeriesSpan =
            8 //how many weeks from now to load in a timeseries

        //SITES - DATALOADER
        const val sitesRange = "0-99"

        //MAINACTIVITY
        const val initialMunicipality = "4615"
        val fakeSite = Site(420, "fakevik", LatLong(1.0, 2.0), null, 3.141592, null, null)
        val initialFavouriteSites: MutableList<Site> = mutableListOf()
    }
}