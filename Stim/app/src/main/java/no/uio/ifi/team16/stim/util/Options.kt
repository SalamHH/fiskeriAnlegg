package no.uio.ifi.team16.stim.util

import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.Sites

class Options {
    companion object {
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
        const val fakeMunicipality = "4615"
        val fakeSite = Site(0, "bingbong", LatLong(61.12341234, 4.23), null, 0.0, null, null)
        val fakeSites = Sites(listOf(fakeSite, fakeSite, fakeSite, fakeSite))
    }
}